package ru.mindlex.feature.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.mindlex.data.remote.dto.QuestionDto
import ru.mindlex.data.repository.PracticeRepository
import ru.mindlex.ui.navigation.Routes
import javax.inject.Inject

/**
 * Логика игрового экрана.
 *
 * <p>Проверка ответа, начисление очков и обновление интервалов повторения
 * выполняются на сервере. Клиент только ведёт таймеры, показывает вопросы
 * и передаёт ответы — это исключает подмену правил игры на устройстве.</p>
 */
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val practiceRepository: PracticeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var sessionTimer: Job? = null
    private var questionTimer: Job? = null
    private var questionStartedAt: Long = 0L
    private var summaryRequested = false

    private val mechanic: String = savedStateHandle.get<String>(Routes.ARG_MECHANIC) ?: "ACTIVE_RECALL"

    init {
        _uiState.update { it.copy(mechanic = mechanic) }
        startNewSession()
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value) }
    }

    fun showHint() {
        _uiState.update { it.copy(hintVisible = true, hintUsed = true) }
    }

    fun submit() {
        val state = _uiState.value
        val question = state.question ?: return
        if (!state.canAnswer) return
        val answer = state.input.trim()
        if (answer.isEmpty()) return

        _uiState.update { it.copy(isSubmitting = true, feedback = null) }
        questionTimer?.cancel()

        viewModelScope.launch {
            val elapsed = System.currentTimeMillis() - questionStartedAt
            practiceRepository.answer(
                sessionId = question.sessionId,
                questionId = question.questionId,
                answer = answer,
                hintUsed = state.hintUsed,
                timedOut = false,
                elapsedMs = elapsed,
            ).fold(
                onSuccess = { result -> handleAnswerResult(result.correct, result.expectedAnswer, result.quality,
                    result.scoreDelta, result.score, result.comboStreak, result.maxCombo, result.sessionFinished) },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                },
            )
        }
    }

    /** Пропуск вопроса: серия обнуляется, слово уходит на повторение позже. */
    fun skip() {
        val state = _uiState.value
        val question = state.question ?: return
        if (!state.canAnswer) return
        questionTimer?.cancel()

        viewModelScope.launch {
            practiceRepository.skip(question.sessionId, question.questionId).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            combo = result.comboStreak,
                            maxCombo = maxOf(it.maxCombo, result.maxCombo),
                            score = result.score,
                            feedback = AnswerFeedback(
                                correct = false,
                                expected = result.expectedAnswer,
                                quality = result.quality,
                                scoreDelta = 0,
                                message = "Пропущено",
                            ),
                        )
                    }
                    advanceAfterFeedback()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                },
            )
        }
    }

    fun playAgain() {
        summaryRequested = false
        startNewSession()
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        cancelTimers()
        super.onCleared()
    }

    // ------------------------------------------------------------------
    // Внутренняя логика
    // ------------------------------------------------------------------

    private fun startNewSession() {
        cancelTimers()
        summaryRequested = false
        _uiState.update {
            PracticeUiState(
                mechanic = mechanic,
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {
            practiceRepository.startSession(mechanic).fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            isLoading = false,
                            score = session.score,
                            combo = session.comboStreak,
                            maxCombo = session.maxCombo,
                            total = session.questionLimit,
                        )
                    }
                    if (session.durationSeconds > 0) {
                        startSessionTimer(session.durationSeconds)
                    }
                    loadNextQuestion()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                },
            )
        }
    }

    private fun loadNextQuestion() {
        val session = _uiState.value.session ?: return
        questionTimer?.cancel()

        viewModelScope.launch {
            practiceRepository.nextQuestion(session.id)
                .onSuccess { question ->
                    if (question == null) {
                        // Сервер вернул 204: вопросы закончились, пора подводить итоги
                        finishSession()
                    } else {
                        _uiState.update {
                            it.copy(
                                question = question,
                                input = "",
                                feedback = null,
                                hintVisible = false,
                                hintUsed = false,
                                isSubmitting = false,
                                index = question.index,
                                total = if (question.totalQuestions > 0) question.totalQuestions else it.total,
                            )
                        }
                        questionStartedAt = System.currentTimeMillis()
                        question.timeLimitSeconds?.let { limit ->
                            startQuestionTimer(limit) { submitTimedOut(question) }
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                }
        }
    }

    private fun handleAnswerResult(
        correct: Boolean,
        expected: String?,
        quality: Int,
        scoreDelta: Int,
        score: Int,
        combo: Int,
        maxCombo: Int,
        sessionFinished: Boolean,
    ) {
        val milestone = if (correct && combo in MILESTONES) combo else 0
        _uiState.update {
            it.copy(
                isSubmitting = false,
                score = score,
                combo = combo,
                maxCombo = maxOf(it.maxCombo, maxCombo),
                milestone = milestone,
                feedback = AnswerFeedback(
                    correct = correct,
                    expected = expected,
                    quality = quality,
                    scoreDelta = scoreDelta,
                    message = when {
                        correct -> "Верно!"
                        quality >= 3 -> "Почти верно"
                        else -> "Неверно"
                    },
                ),
            )
        }

        if (milestone > 0) {
            viewModelScope.launch {
                delay(700)
                _uiState.update { it.copy(milestone = 0) }
            }
        }

        if (sessionFinished) {
            viewModelScope.launch {
                delay(FEEDBACK_DELAY_MS)
                finishSession()
            }
        } else {
            advanceAfterFeedback()
        }
    }

    private fun advanceAfterFeedback() {
        if (sessionTimer?.isActive == true && _uiState.value.timeRemaining == 0) {
            finishSession()
            return
        }
        viewModelScope.launch {
            delay(FEEDBACK_DELAY_MS)
            if (!summaryRequested) {
                loadNextQuestion()
            }
        }
    }

    private fun submitTimedOut(question: QuestionDto) {
        if (_uiState.value.feedback != null) return
        viewModelScope.launch {
            practiceRepository.answer(
                sessionId = question.sessionId,
                questionId = question.questionId,
                answer = _uiState.value.input,
                hintUsed = _uiState.value.hintUsed,
                timedOut = true,
                elapsedMs = System.currentTimeMillis() - questionStartedAt,
            ).fold(
                onSuccess = { result ->
                    handleAnswerResult(false, result.expectedAnswer, result.quality, 0, result.score,
                        result.comboStreak, result.maxCombo, result.sessionFinished)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                },
            )
        }
    }

    private fun finishSession() {
        val session = _uiState.value.session ?: return
        if (summaryRequested) return
        summaryRequested = true
        cancelTimers()

        viewModelScope.launch {
            practiceRepository.finish(session.id).fold(
                onSuccess = { summary ->
                    _uiState.update {
                        it.copy(
                            summary = summary,
                            question = null,
                            isLoading = false,
                            isSubmitting = false,
                            timeRemaining = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                },
            )
        }
    }

    private fun startSessionTimer(totalSeconds: Int) {
        sessionTimer?.cancel()
        sessionTimer = viewModelScope.launch {
            var left = totalSeconds
            _uiState.update { it.copy(timeRemaining = left, timeTotal = totalSeconds) }
            while (isActive && left > 0) {
                delay(1000)
                left -= 1
                _uiState.update { it.copy(timeRemaining = left) }
            }
            if (isActive) {
                finishSession()
            }
        }
    }

    private fun startQuestionTimer(limitSeconds: Int, onExpire: () -> Unit) {
        questionTimer?.cancel()
        questionTimer = viewModelScope.launch {
            var left = limitSeconds
            _uiState.update { it.copy(timeRemaining = left, timeTotal = limitSeconds) }
            while (isActive && left > 0) {
                delay(1000)
                left -= 1
                _uiState.update { it.copy(timeRemaining = left) }
            }
            if (isActive) {
                onExpire()
            }
        }
    }

    private fun cancelTimers() {
        sessionTimer?.cancel()
        sessionTimer = null
        questionTimer?.cancel()
        questionTimer = null
    }

    private companion object {
        const val FEEDBACK_DELAY_MS = 900L
        val MILESTONES = setOf(5, 10, 15, 20)
    }
}
