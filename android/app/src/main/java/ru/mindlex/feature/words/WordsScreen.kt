package ru.mindlex.feature.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.WordDto
import ru.mindlex.ui.components.EmptyView
import ru.mindlex.ui.components.ErrorView
import ru.mindlex.ui.components.LoadingView
import ru.mindlex.ui.components.MindLexBackground
import ru.mindlex.ui.components.MindLexCard
import ru.mindlex.ui.components.MindLexTextField
import ru.mindlex.ui.components.Pill
import ru.mindlex.ui.components.PrimaryButton
import ru.mindlex.ui.components.ScreenHeader
import ru.mindlex.ui.components.SecondaryButton
import ru.mindlex.ui.theme.Coral

/**
 * Экран «Мои слова»: список колод и слов с возможностью добавлять,
 * изменять и удалять записи.
 *
 * @param onBack возврат на предыдущий экран.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsScreen(
    onBack: () -> Unit,
    viewModel: WordsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindLexBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Мои слова",
                subtitle = "Колоды и слова",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            if (state.decks.isNotEmpty()) {
                DeckRow(
                    decks = state.decks,
                    selectedDeckId = state.selectedDeck?.id,
                    onSelect = viewModel::selectDeck,
                    onAddDeck = viewModel::openDeckDialog,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
            }

            if (state.isLoading) {
                MindLexCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LoadingView(message = "Загружаем слова…")
                }
            } else if (state.error != null && state.decks.isEmpty()) {
                ErrorView(
                    message = state.error.orEmpty(),
                    onRetry = viewModel::load,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                WordList(
                    state = state,
                    onAddWord = { viewModel.openEditor(null) },
                    onEditWord = { word -> viewModel.openEditor(word) },
                    onDeleteWord = viewModel::requestWordDelete,
                    onDeleteDeck = viewModel::requestDeckDelete,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        WordEditorSheet(
            visible = state.editorVisible,
            isEditing = state.editingWordId != null,
            term = state.term,
            translation = state.translation,
            transcription = state.transcription,
            partOfSpeech = state.partOfSpeech,
            example = state.example,
            alternatives = state.alternatives,
            editorError = state.editorError,
            isSaving = state.isSaving,
            onTermChange = viewModel::onTermChange,
            onTranslationChange = viewModel::onTranslationChange,
            onTranscriptionChange = viewModel::onTranscriptionChange,
            onPartOfSpeechChange = viewModel::onPartOfSpeechChange,
            onExampleChange = viewModel::onExampleChange,
            onAlternativesChange = viewModel::onAlternativesChange,
            onSave = viewModel::saveWord,
            onDismiss = viewModel::closeEditor,
        )

        if (state.deckDialogVisible) {
            NewDeckDialog(
                name = state.newDeckName,
                isSaving = state.isSaving,
                onNameChange = viewModel::onNewDeckNameChange,
                onConfirm = { viewModel.createDeck(state.newDeckName) },
                onDismiss = viewModel::closeDeckDialog,
            )
        }

        state.wordToDelete?.let { word ->
            ConfirmDialog(
                title = "Удалить слово?",
                message = "«${word.term}» будет удалено из колоды безвозвратно.",
                confirmLabel = "Удалить",
                onConfirm = { viewModel.deleteWord(word.id) },
                onDismiss = viewModel::cancelWordDelete,
            )
        }

        state.deckToDelete?.let { deck ->
            ConfirmDialog(
                title = "Удалить колоду?",
                message = "Колода «${deck.name}» и все её слова будут удалены безвозвратно.",
                confirmLabel = "Удалить",
                onConfirm = { viewModel.deleteDeck(deck.id) },
                onDismiss = viewModel::cancelDeckDelete,
            )
        }
    }
}

/** Горизонтальная лента чипов колод с кнопкой создания новой колоды. */
@Composable
private fun DeckRow(
    decks: List<DeckDto>,
    selectedDeckId: String?,
    onSelect: (String) -> Unit,
    onAddDeck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(items = decks, key = { it.id }) { deck ->
            val selected = deck.id == selectedDeckId
            Pill(
                text = deck.name,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.clickable { onSelect(deck.id) },
            )
        }
        item(key = "add-deck") {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    .clickable(onClick = onAddDeck),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Создать колоду",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Список слов выбранной колоды вместе с шапкой и кнопкой добавления. */
@Composable
private fun WordList(
    state: WordsUiState,
    onAddWord: () -> Unit,
    onEditWord: (WordDto) -> Unit,
    onDeleteWord: (WordDto) -> Unit,
    onDeleteDeck: (DeckDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deck = state.selectedDeck

    Column(modifier = modifier.fillMaxSize()) {
        if (deck != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(10.dp))
                if (deck.isPublic) {
                    Pill(text = "Системная колода")
                } else {
                    Pill(
                        text = "${deckWordCount(deck, state)} слов",
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    IconButton(onClick = { onDeleteDeck(deck) }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Удалить колоду",
                            tint = Coral,
                        )
                    }
                }
            }
        }

        val message = state.message
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.words.isEmpty()) {
                item(key = "empty") {
                    EmptyView(
                        title = "Слов пока нет",
                        message = "Добавьте первое слово в эту колоду",
                    )
                }
            } else {
                items(items = state.words, key = { it.id }) { word ->
                    WordRow(
                        word = word,
                        canEdit = state.canEdit,
                        onClick = { onEditWord(word) },
                        onDelete = { onDeleteWord(word) },
                    )
                }
            }
        }

        if (state.canEdit && deck != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                PrimaryButton(
                    text = "Добавить слово",
                    onClick = onAddWord,
                    enabled = !state.isSaving,
                )
            }
        }
    }
}

/** Количество слов колоды: для открытой колоды берём актуальное значение из состояния. */
private fun deckWordCount(deck: DeckDto, state: WordsUiState): Int =
    if (state.selectedDeck?.id == deck.id) state.words.size else deck.wordCount.toInt()

/** Карточка одного слова: термин, перевод и дополнительные пометки. */
@Composable
private fun WordRow(
    word: WordDto,
    canEdit: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MindLexCard(
        modifier = modifier,
        contentPadding = 16,
        onClick = if (canEdit) onClick else null,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.term,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val transcription = word.transcription
                if (!transcription.isNullOrBlank()) {
                    Text(
                        text = transcription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (canEdit) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Удалить слово",
                        tint = Coral,
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = word.translation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val example = word.example
        if (!example.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = example,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!word.partOfSpeech.isNullOrBlank() || word.alternatives.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val partOfSpeech = word.partOfSpeech
                if (!partOfSpeech.isNullOrBlank()) {
                    Pill(text = partOfSpeech, color = MaterialTheme.colorScheme.secondary)
                }
                word.alternatives.forEach { alternative ->
                    Pill(
                        text = alternative,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Нижняя панель редактирования слова. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordEditorSheet(
    visible: Boolean,
    isEditing: Boolean,
    term: String,
    translation: String,
    transcription: String,
    partOfSpeech: String,
    example: String,
    alternatives: String,
    editorError: String?,
    isSaving: Boolean,
    onTermChange: (String) -> Unit,
    onTranslationChange: (String) -> Unit,
    onTranscriptionChange: (String) -> Unit,
    onPartOfSpeechChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onAlternativesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) {
        return
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = if (isEditing) "Изменить слово" else "Новое слово",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            MindLexTextField(
                value = term,
                onValueChange = onTermChange,
                label = "Слово",
            )
            Spacer(Modifier.height(12.dp))

            MindLexTextField(
                value = translation,
                onValueChange = onTranslationChange,
                label = "Перевод",
            )
            Spacer(Modifier.height(12.dp))

            MindLexTextField(
                value = transcription,
                onValueChange = onTranscriptionChange,
                label = "Транскрипция",
            )
            Spacer(Modifier.height(12.dp))

            MindLexTextField(
                value = partOfSpeech,
                onValueChange = onPartOfSpeechChange,
                label = "Часть речи",
            )
            Spacer(Modifier.height(12.dp))

            MindLexTextField(
                value = example,
                onValueChange = onExampleChange,
                label = "Пример употребления",
                singleLine = false,
                minLines = 2,
            )
            Spacer(Modifier.height(12.dp))

            MindLexTextField(
                value = alternatives,
                onValueChange = onAlternativesChange,
                label = "Варианты ответа",
                placeholder = "через запятую",
            )

            if (editorError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = editorError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = "Сохранить", onClick = onSave, loading = isSaving)
            Spacer(Modifier.height(10.dp))
            SecondaryButton(text = "Отмена", onClick = onDismiss, enabled = !isSaving)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Диалог создания новой колоды. */
@Composable
private fun NewDeckDialog(
    name: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Новая колода",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            MindLexTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Название",
                onImeAction = onConfirm,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

/** Единый диалог подтверждения удаления. */
@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel, color = Coral)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
