package ru.mindlex.feature.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.WordDto
import ru.mindlex.data.repository.LearningRepository
import javax.inject.Inject

/**
 * Логика экрана «Мои слова»: выбор колоды и CRUD над колодами и словами.
 *
 * <p>Все запросы идут через [LearningRepository], а результат складывается
 * в единственный поток [uiState]. Ошибка сервера показывается пользователю
 * текстом, который подготовил репозиторий.</p>
 */
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordsUiState())

    /** Текущее состояние экрана. */
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /**
     * Загружает колоды и автоматически открывает первую из них.
     * Вызывается при создании модели и по кнопке «Повторить».
     */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }

            learningRepository.decks()
                .onSuccess { decks ->
                    // Список колод обязательно попадает в состояние: по нему работает выбор колоды
                    _uiState.update { it.copy(decks = decks, isLoading = false, error = null) }
                    val first = decks.firstOrNull()
                    if (first == null) {
                        _uiState.update {
                            it.copy(
                                decks = emptyList(),
                                selectedDeck = null,
                                words = emptyList(),
                                isLoading = false,
                            )
                        }
                    } else {
                        selectDeck(first.id)
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = failure.message ?: "Не удалось загрузить колоды",
                        )
                    }
                }
        }
    }

    /** Выбирает колоду и загружает её слова. */
    fun selectDeck(deckId: String) {
        val deck = _uiState.value.decks.firstOrNull { it.id == deckId } ?: return
        _uiState.update {
            it.copy(selectedDeck = deck, words = emptyList(), error = null, isLoading = true)
        }
        loadWords(deckId)
    }

    /** Открывает диалог создания колоды. */
    fun openDeckDialog() {
        _uiState.update { it.copy(deckDialogVisible = true, newDeckName = "", message = null) }
    }

    /** Закрывает диалог создания колоды. */
    fun closeDeckDialog() {
        _uiState.update { it.copy(deckDialogVisible = false, newDeckName = "") }
    }

    /** Меняет название новой колоды. */
    fun onNewDeckNameChange(value: String) {
        _uiState.update { it.copy(newDeckName = value) }
    }

    /**
     * Создаёт колоду и сразу переключается на неё.
     * Пустое название отклоняется до запроса к серверу.
     */
    fun createDeck(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(error = "Введите название колоды") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            learningRepository.createDeck(name = trimmed, description = null, language = "en")
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            deckDialogVisible = false,
                            newDeckName = "",
                            message = "Колода «${created.name}» создана",
                        )
                    }
                    // Список колод перечитываем: количество слов считает сервер.
                    learningRepository.decks().onSuccess { decks ->
                        _uiState.update { it.copy(decks = decks) }
                        selectDeck(created.id)
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = failure.message ?: "Не удалось создать колоду",
                        )
                    }
                }
        }
    }

    /** Запрашивает подтверждение удаления колоды. */
    fun requestDeckDelete(deck: DeckDto) {
        if (deck.isPublic) {
            _uiState.update { it.copy(error = "Системную колоду удалить нельзя") }
            return
        }
        _uiState.update { it.copy(deckToDelete = deck) }
    }

    /** Отменяет удаление колоды. */
    fun cancelDeckDelete() {
        _uiState.update { it.copy(deckToDelete = null) }
    }

    /**
     * Удаляет колоду. Публичные системные колоды удалять запрещено — проверка
     * выполняется и здесь, чтобы правило не зависело от состояния интерфейса.
     */
    fun deleteDeck(deckId: String) {
        val deck = _uiState.value.decks.firstOrNull { it.id == deckId }
        if (deck?.isPublic == true) {
            _uiState.update { it.copy(error = "Системную колоду удалить нельзя", deckToDelete = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            learningRepository.deleteDeck(deckId)
                .onSuccess {
                    val remaining = _uiState.value.decks.filterNot { it.id == deckId }
                    val wasSelected = _uiState.value.selectedDeck?.id == deckId
                    _uiState.update {
                        it.copy(
                            decks = remaining,
                            isSaving = false,
                            deckToDelete = null,
                            message = "Колода удалена",
                            selectedDeck = if (wasSelected) null else it.selectedDeck,
                            words = if (wasSelected) emptyList() else it.words,
                        )
                    }
                    val next = remaining.firstOrNull()
                    if (wasSelected && next != null) {
                        selectDeck(next.id)
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            deckToDelete = null,
                            error = failure.message ?: "Не удалось удалить колоду",
                        )
                    }
                }
        }
    }

    /** Сбрасывает короткое подтверждение действия. */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Сбрасывает общую ошибку экрана. */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Открывает редактор слова.
     *
     * @param word редактируемое слово или `null` для создания нового.
     */
    fun openEditor(word: WordDto?) {
        _uiState.update {
            it.copy(
                editorVisible = true,
                editingWordId = word?.id,
                term = word?.term.orEmpty(),
                translation = word?.translation.orEmpty(),
                transcription = word?.transcription.orEmpty(),
                partOfSpeech = word?.partOfSpeech.orEmpty(),
                example = word?.example.orEmpty(),
                alternatives = word?.alternatives?.joinToString(", ").orEmpty(),
                editorError = null,
            )
        }
    }

    /** Закрывает редактор и очищает его поля. */
    fun closeEditor() {
        _uiState.update {
            it.copy(
                editorVisible = false,
                editingWordId = null,
                term = "",
                translation = "",
                transcription = "",
                partOfSpeech = "",
                example = "",
                alternatives = "",
                editorError = null,
            )
        }
    }

    fun onTermChange(value: String) = _uiState.update { it.copy(term = value, editorError = null) }

    fun onTranslationChange(value: String) = _uiState.update { it.copy(translation = value, editorError = null) }

    fun onTranscriptionChange(value: String) = _uiState.update { it.copy(transcription = value) }

    fun onPartOfSpeechChange(value: String) = _uiState.update { it.copy(partOfSpeech = value) }

    fun onExampleChange(value: String) = _uiState.update { it.copy(example = value) }

    fun onAlternativesChange(value: String) = _uiState.update { it.copy(alternatives = value) }

    /**
     * Сохраняет слово: создаёт новое или обновляет редактируемое.
     * Перед запросом проверяет обязательные поля, после успеха перезагружает список.
     */
    fun saveWord() {
        val current = _uiState.value
        val deck = current.selectedDeck
        if (deck == null) {
            _uiState.update { it.copy(editorError = "Сначала выберите колоду") }
            return
        }

        val term = current.term.trim()
        val translation = current.translation.trim()
        if (term.isEmpty() || translation.isEmpty()) {
            _uiState.update { it.copy(editorError = "Заполните слово и перевод") }
            return
        }
        if (deck.isPublic) {
            _uiState.update { it.copy(editorError = "Системную колоду изменять нельзя") }
            return
        }

        // Варианты ответа вводятся одной строкой через запятую.
        val alternatives = current.alternatives
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val editingId = current.editingWordId

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, editorError = null, error = null) }

            val result = if (editingId == null) {
                learningRepository.createWord(
                    deckId = deck.id,
                    term = term,
                    translation = translation,
                    transcription = current.transcription,
                    partOfSpeech = current.partOfSpeech,
                    example = current.example,
                    alternatives = alternatives,
                )
            } else {
                learningRepository.updateWord(
                    wordId = editingId,
                    term = term,
                    translation = translation,
                    transcription = current.transcription,
                    partOfSpeech = current.partOfSpeech,
                    example = current.example,
                    alternatives = alternatives,
                )
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            editorVisible = false,
                            editingWordId = null,
                            term = "",
                            translation = "",
                            transcription = "",
                            partOfSpeech = "",
                            example = "",
                            alternatives = "",
                            editorError = null,
                            message = if (editingId == null) "Слово добавлено" else "Слово обновлено",
                        )
                    }
                    loadWords(deck.id)
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            editorError = failure.message ?: "Не удалось сохранить слово",
                        )
                    }
                }
        }
    }

    /** Запрашивает подтверждение удаления слова. */
    fun requestWordDelete(word: WordDto) {
        _uiState.update { it.copy(wordToDelete = word) }
    }

    /** Отменяет удаление слова. */
    fun cancelWordDelete() {
        _uiState.update { it.copy(wordToDelete = null) }
    }

    /** Удаляет слово и перезагружает список слов текущей колоды. */
    fun deleteWord(wordId: String) {
        val deckId = _uiState.value.selectedDeck?.id
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            learningRepository.deleteWord(wordId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, wordToDelete = null, message = "Слово удалено")
                    }
                    if (deckId != null) {
                        loadWords(deckId)
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            wordToDelete = null,
                            error = failure.message ?: "Не удалось удалить слово",
                        )
                    }
                }
        }
    }

    /** Перезагружает слова колоды, сохраняя выбранную колоду. */
    private fun loadWords(deckId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            learningRepository.words(deckId)
                .onSuccess { words ->
                    _uiState.update {
                        if (it.selectedDeck?.id == deckId) {
                            it.copy(isLoading = false, words = words, error = null)
                        } else {
                            it.copy(isLoading = false)
                        }
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = failure.message ?: "Не удалось загрузить слова",
                        )
                    }
                }
        }
    }
}
