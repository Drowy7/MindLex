package ru.mindlex.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mindlex.data.remote.dto.SettingsRequest
import ru.mindlex.data.repository.AuthRepository
import ru.mindlex.data.repository.LearningRepository
import javax.inject.Inject

/**
 * Логика экрана настроек: загрузка параметров тренировок, их сохранение
 * и выход из аккаунта.
 *
 * <p>Значения ползунков ограничены теми же границами, что проверяет сервер,
 * поэтому некорректный запрос просто не может быть отправлен.</p>
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())

    /** Текущее состояние экрана. */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Загружает настройки и список колод, перенося значения в поля состояния. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val settingsResult = learningRepository.settings()
            val decksResult = learningRepository.decks()

            val failure = settingsResult.exceptionOrNull() ?: decksResult.exceptionOrNull()
            if (failure != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = failure.message ?: "Не удалось загрузить настройки",
                    )
                }
                return@launch
            }

            val settings = settingsResult.getOrNull()
            val decks = decksResult.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    settings = settings,
                    decks = decks,
                    dailyGoal = settings?.dailyGoal ?: it.dailyGoal,
                    rushSeconds = settings?.rushSeconds ?: it.rushSeconds,
                    clozeSeconds = settings?.clozeSeconds ?: it.clozeSeconds,
                    notificationsEnabled = settings?.notificationsEnabled ?: it.notificationsEnabled,
                    language = settings?.language ?: it.language,
                    selectedDeckId = settings?.selectedDeckId,
                )
            }
        }
    }

    /** Меняет дневную цель, удерживая значение в допустимых границах. */
    fun onDailyGoalChange(value: Int) = _uiState.update {
        it.copy(
            dailyGoal = value.coerceIn(
                SettingsUiState.MIN_DAILY_GOAL,
                SettingsUiState.MAX_DAILY_GOAL,
            ),
            savedMessage = null,
        )
    }

    /** Меняет длительность скоростного раунда. */
    fun onRushSecondsChange(value: Int) = _uiState.update {
        it.copy(
            rushSeconds = value.coerceIn(
                SettingsUiState.MIN_RUSH_SECONDS,
                SettingsUiState.MAX_RUSH_SECONDS,
            ),
            savedMessage = null,
        )
    }

    /** Меняет время на контекстное задание. */
    fun onClozeSecondsChange(value: Int) = _uiState.update {
        it.copy(
            clozeSeconds = value.coerceIn(
                SettingsUiState.MIN_CLOZE_SECONDS,
                SettingsUiState.MAX_CLOZE_SECONDS,
            ),
            savedMessage = null,
        )
    }

    /** Включает или выключает напоминания о занятиях. */
    fun onNotificationsChange(enabled: Boolean) = _uiState.update {
        it.copy(notificationsEnabled = enabled, savedMessage = null)
    }

    /** Меняет изучаемый язык. */
    fun onLanguageChange(language: String) = _uiState.update {
        it.copy(language = language, savedMessage = null)
    }

    /** Выбирает колоду для тренировок. */
    fun onDeckSelected(deckId: String) = _uiState.update {
        it.copy(selectedDeckId = deckId, savedMessage = null)
    }

    /** Сохраняет настройки на сервере. */
    fun save() {
        val current = _uiState.value
        val settings = current.settings
        if (settings == null) {
            _uiState.update { it.copy(error = "Настройки ещё не загружены") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, savedMessage = null) }

            val request = SettingsRequest(
                dailyGoal = current.dailyGoal,
                language = current.language,
                selectedDeckId = current.selectedDeckId,
                preferredTime = settings.preferredTime,
                rushSeconds = current.rushSeconds,
                clozeSeconds = current.clozeSeconds,
                notificationsEnabled = current.notificationsEnabled,
                timezone = settings.timezone,
            )

            learningRepository.updateSettings(request)
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settings = updated,
                            dailyGoal = updated.dailyGoal,
                            rushSeconds = updated.rushSeconds,
                            clozeSeconds = updated.clozeSeconds,
                            notificationsEnabled = updated.notificationsEnabled,
                            language = updated.language,
                            selectedDeckId = updated.selectedDeckId,
                            savedMessage = "Настройки сохранены",
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = failure.message ?: "Не удалось сохранить настройки",
                        )
                    }
                }
        }
    }

    /**
     * Выходит из аккаунта. Переход на экран входа выполняет корневой NavHost,
     * который следит за состоянием токена.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
