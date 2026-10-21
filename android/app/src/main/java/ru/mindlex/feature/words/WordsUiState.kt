package ru.mindlex.feature.words

import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.WordDto

/**
 * Состояние экрана «Мои слова».
 *
 * <p>Экран совмещает три задачи: выбор колоды, просмотр её слов и редактирование
 * отдельного слова. Последнее описано набором плоских полей — так состояние
 * остаётся одним неизменяемым объектом, который удобно обновлять через
 * [kotlinx.coroutines.flow.MutableStateFlow.update].</p>
 *
 * @property isLoading идёт первичная загрузка колод или слов.
 * @property error текст ошибки для пользователя; `null`, если ошибок нет.
 * @property decks список доступных колод (системные и собственные).
 * @property selectedDeck выбранная колода.
 * @property words слова выбранной колоды.
 * @property isSaving идёт сохранение или удаление.
 * @property message короткое подтверждение выполненного действия.
 * @property editorVisible открыт ли редактор слова.
 * @property editingWordId идентификатор редактируемого слова; `null` — создание нового.
 * @property term слово в оригинале.
 * @property translation перевод.
 * @property transcription транскрипция.
 * @property partOfSpeech часть речи.
 * @property example пример употребления.
 * @property alternatives варианты ответа одной строкой через запятую.
 * @property editorError ошибка валидации внутри редактора.
 * @property newDeckName название новой колоды в диалоге создания.
 * @property deckDialogVisible открыт ли диалог создания колоды.
 * @property wordToDelete слово, ожидающее подтверждения удаления.
 * @property deckToDelete колода, ожидающая подтверждения удаления.
 */
data class WordsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val decks: List<DeckDto> = emptyList(),
    val selectedDeck: DeckDto? = null,
    val words: List<WordDto> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val editorVisible: Boolean = false,
    val editingWordId: String? = null,
    val term: String = "",
    val translation: String = "",
    val transcription: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val alternatives: String = "",
    val editorError: String? = null,
    val newDeckName: String = "",
    val deckDialogVisible: Boolean = false,
    val wordToDelete: WordDto? = null,
    val deckToDelete: DeckDto? = null,
) {

    /** Можно ли изменять содержимое выбранной колоды. */
    val canEdit: Boolean get() = selectedDeck?.isPublic != true
}
