package ru.mindlex.data.repository

import ru.mindlex.data.remote.MindLexApi
import ru.mindlex.data.remote.dto.AnswerDto
import ru.mindlex.data.remote.dto.AnswerRequest
import ru.mindlex.data.remote.dto.QuestionDto
import ru.mindlex.data.remote.dto.SessionDto
import ru.mindlex.data.remote.dto.SessionSummaryDto
import ru.mindlex.data.remote.dto.SkipRequest
import ru.mindlex.data.remote.dto.StartSessionRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Игровой цикл. Вся логика механик (выбор вопроса, проверка ответа,
 * начисление очков и пересчёт интервалов повторения) выполняется на сервере.
 */
@Singleton
class PracticeRepository @Inject constructor(
    private val api: MindLexApi,
    private val apiCaller: ApiCaller,
) {

    suspend fun startSession(mechanic: String, deckId: String? = null): Result<SessionDto> =
        apiCaller.call { api.startSession(StartSessionRequest(mechanic = mechanic, deckId = deckId)) }

    /** {@code null} означает, что вопросы закончились и сессию пора завершать. */
    suspend fun nextQuestion(sessionId: String): Result<QuestionDto?> =
        apiCaller.call {
            val response = api.question(sessionId)
            if (response.isSuccessful) response.body() else null
        }

    suspend fun answer(
        sessionId: String,
        questionId: String,
        answer: String,
        hintUsed: Boolean = false,
        timedOut: Boolean = false,
        elapsedMs: Long = 0,
    ): Result<AnswerDto> = apiCaller.call {
        api.answer(
            sessionId,
            AnswerRequest(
                questionId = questionId,
                answer = answer,
                hintUsed = hintUsed,
                timedOut = timedOut,
                elapsedMs = elapsedMs,
            ),
        )
    }

    suspend fun skip(sessionId: String, questionId: String): Result<AnswerDto> =
        apiCaller.call { api.skip(sessionId, SkipRequest(questionId)) }

    suspend fun finish(sessionId: String): Result<SessionSummaryDto> =
        apiCaller.call { api.finish(sessionId) }
}
