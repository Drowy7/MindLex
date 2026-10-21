package ru.mindlex.feature.practice

import ru.mindlex.data.remote.dto.QuestionDto
import ru.mindlex.data.remote.dto.SessionDto
import ru.mindlex.data.remote.dto.SessionSummaryDto

/**
 * Результат проверки ответа — показывается пользователю между вопросами.
 */
data class AnswerFeedback(
    val correct: Boolean,
    val expected: String?,
    val quality: Int,
    val scoreDelta: Int,
    val message: String,
)

/**
 * Состояние игрового экрана. Одна и та же модель используется всеми
 * четырьмя механиками — различается только отрисовка вопроса.
 */
data class PracticeUiState(
    val mechanic: String = "ACTIVE_RECALL",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val session: SessionDto? = null,
    val question: QuestionDto? = null,
    val input: String = "",
    val feedback: AnswerFeedback? = null,
    val hintVisible: Boolean = false,
    val hintUsed: Boolean = false,
    val summary: SessionSummaryDto? = null,
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val index: Int = 0,
    val total: Int = 0,
    val timeRemaining: Int? = null,
    val timeTotal: Int? = null,
    val milestone: Int = 0,
) {

    /** Можно ли принимать ответ прямо сейчас. */
    val canAnswer: Boolean
        get() = question != null && feedback == null && !isSubmitting && summary == null

    val isRush: Boolean get() = mechanic == "RUSH"
    val isChain: Boolean get() = mechanic == "SYNONYM_CHAIN"

    val timeFraction: Float?
        get() {
            val left = timeRemaining ?: return null
            val total = timeTotal ?: return null
            if (total <= 0) return null
            return (left.toFloat() / total).coerceIn(0f, 1f)
        }

    companion object {
        fun titleFor(mechanic: String): String = when (mechanic) {
            "ACTIVE_RECALL" -> "Активное припоминание"
            "CLOZE" -> "Контекстные пропуски"
            "RUSH" -> "Скоростной раунд"
            "SYNONYM_CHAIN" -> "Цепочка синонимов"
            else -> "Тренировка"
        }

        fun subtitleFor(mechanic: String): String = when (mechanic) {
            "ACTIVE_RECALL" -> "Переведите слово на изучаемый язык"
            "CLOZE" -> "Вставьте пропущенное слово"
            "RUSH" -> "Успейте перевести как можно больше слов"
            "SYNONYM_CHAIN" -> "Подберите синоним к слову"
            else -> ""
        }
    }
}
