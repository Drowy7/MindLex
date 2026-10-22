package ru.mindlex.feature.settings

import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.SettingsDto

/**
 * Состояние экрана настроек.
 *
 * <p>Значения из [SettingsDto] переносятся в отдельные поля: так пользователь
 * может свободно двигать ползунки и выбирать колоду, не изменяя загруженный
 * снимок настроек, пока не нажмёт «Сохранить».</p>
 *
 * @property isLoading идёт загрузка настроек и колод.
 * @property isSaving идёт сохранение или выход из аккаунта.
 * @property error текст ошибки для пользователя; `null`, если ошибок нет.
 * @property savedMessage подтверждение успешного сохранения.
 * @property settings снимок настроек, полученный с сервера.
 * @property decks список колод для выбора колоды тренировок.
 * @property dailyGoal дневная цель в словах (5..200).
 * @property rushSeconds длительность скоростного раунда (60..90).
 * @property clozeSeconds время на контекстное задание (30..45).
 * @property notificationsEnabled включены ли напоминания о занятиях.
 * @property language код изучаемого языка.
 * @property selectedDeckId идентификатор выбранной колоды; `null` — колода не выбрана.
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedMessage: String? = null,
    val settings: SettingsDto? = null,
    val decks: List<DeckDto> = emptyList(),
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
    val rushSeconds: Int = DEFAULT_RUSH_SECONDS,
    val clozeSeconds: Int = DEFAULT_CLOZE_SECONDS,
    val notificationsEnabled: Boolean = true,
    val language: String = "en",
    val selectedDeckId: String? = null,
) {

    companion object {

        /** Границы дневной цели, которые проверяет сервер. */
        const val MIN_DAILY_GOAL = 5
        const val MAX_DAILY_GOAL = 200
        const val DAILY_GOAL_STEP = 5
        const val DEFAULT_DAILY_GOAL = 15

        /** Границы длительности скоростного раунда. */
        const val MIN_RUSH_SECONDS = 60
        const val MAX_RUSH_SECONDS = 90
        const val DEFAULT_RUSH_SECONDS = 90

        /** Границы времени на контекстное задание. */
        const val MIN_CLOZE_SECONDS = 30
        const val MAX_CLOZE_SECONDS = 45
        const val DEFAULT_CLOZE_SECONDS = 35

        /** Языки, доступные для изучения. */
        val LANGUAGES = listOf(
            "en" to "Английский",
            "de" to "Немецкий",
            "fr" to "Французский",
            "es" to "Испанский",
        )

        /** Название языка по его коду. */
        fun languageTitle(code: String): String =
            LANGUAGES.firstOrNull { it.first == code }?.second ?: code.uppercase()
    }
}
