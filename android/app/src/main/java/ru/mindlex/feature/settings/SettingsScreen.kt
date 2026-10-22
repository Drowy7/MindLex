package ru.mindlex.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mindlex.BuildConfig
import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.ui.components.LoadingView
import ru.mindlex.ui.components.MindLexBackground
import ru.mindlex.ui.components.MindLexCard
import ru.mindlex.ui.components.Pill
import ru.mindlex.ui.components.PrimaryButton
import ru.mindlex.ui.components.ScreenHeader
import ru.mindlex.ui.components.SecondaryButton
import ru.mindlex.ui.components.SectionHeader

/**
 * Экран настроек: параметры тренировок, выбор колоды, язык, напоминания
 * и достижения пользователя.
 *
 * @param onBack возврат на предыдущий экран.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindLexBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Настройки",
                subtitle = "Параметры тренировок",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            if (state.isLoading) {
                MindLexCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LoadingView(message = "Загружаем настройки…")
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (state.error != null) {
                    Text(
                        text = state.error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }

                SettingsCard(title = "Дневная цель") {
                    Slider(
                        value = state.dailyGoal.toFloat(),
                        onValueChange = { viewModel.onDailyGoalChange(it.toInt()) },
                        valueRange = SettingsUiState.MIN_DAILY_GOAL.toFloat()..
                            SettingsUiState.MAX_DAILY_GOAL.toFloat(),
                        steps = dailyGoalSteps(),
                    )
                    Text(
                        text = "${state.dailyGoal} слов в день",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SettingsCard(title = "Колода для тренировок") {
                    if (state.decks.isEmpty()) {
                        Text(
                            text = "Колод пока нет — создайте их в разделе «Мои слова»",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.decks.forEach { deck ->
                                DeckOption(
                                    deck = deck,
                                    selected = deck.id == state.selectedDeckId,
                                    onClick = { viewModel.onDeckSelected(deck.id) },
                                )
                            }
                        }
                    }
                }

                SettingsCard(title = "Скоростной раунд") {
                    Slider(
                        value = state.rushSeconds.toFloat(),
                        onValueChange = { viewModel.onRushSecondsChange(it.toInt()) },
                        valueRange = SettingsUiState.MIN_RUSH_SECONDS.toFloat()..
                            SettingsUiState.MAX_RUSH_SECONDS.toFloat(),
                        steps = rushSteps(),
                    )
                    Text(
                        text = "${state.rushSeconds} секунд на раунд",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SettingsCard(title = "Контекстные пропуски") {
                    Slider(
                        value = state.clozeSeconds.toFloat(),
                        onValueChange = { viewModel.onClozeSecondsChange(it.toInt()) },
                        valueRange = SettingsUiState.MIN_CLOZE_SECONDS.toFloat()..
                            SettingsUiState.MAX_CLOZE_SECONDS.toFloat(),
                        steps = clozeSteps(),
                    )
                    Text(
                        text = "${state.clozeSeconds} секунд на задание",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SettingsCard(title = "Язык") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SettingsUiState.LANGUAGES.forEach { (code, _) ->
                            val selected = code == state.language
                            Pill(
                                text = "${SettingsUiState.languageTitle(code)} · $code",
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.clickable { viewModel.onLanguageChange(code) },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Напоминания о занятиях",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Уведомление в выбранное время дня",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = viewModel::onNotificationsChange,
                        )
                    }
                }

                SettingsCard(title = "Достижения") {
                    val settings = state.settings
                    if (settings == null) {
                        Text(
                            text = "Данные достижений недоступны",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        AchievementRow("Серия занятий", "${settings.visibleStreak} дн.")
                        AchievementRow("Рекорд раунда", settings.bestRushScore.toString())
                        AchievementRow("Максимальное комбо", settings.maxComboRecord.toString())
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrimaryButton(
                        text = "Сохранить",
                        onClick = viewModel::save,
                        loading = state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.savedMessage != null) {
                        Spacer(Modifier.width(12.dp))
                        Pill(
                            text = "Сохранено",
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                SecondaryButton(text = "Выйти из аккаунта", onClick = viewModel::logout)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Адрес API: ${BuildConfig.API_BASE_URL}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Карточка раздела настроек с заголовком и отступами между блоками. */
@Composable
private fun SettingsCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    MindLexCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionHeader(title = title, trailing = trailing)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** Вариант выбора колоды в виде чипа. */
@Composable
private fun DeckOption(
    deck: DeckDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Pill(
        text = if (selected) "✓ ${deck.name}" else deck.name,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** Строка достижения: подпись слева, значение справа. */
@Composable
private fun AchievementRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Количество промежуточных шагов ползунка дневной цели. */
private fun dailyGoalSteps(): Int =
    ((SettingsUiState.MAX_DAILY_GOAL - SettingsUiState.MIN_DAILY_GOAL) /
        SettingsUiState.DAILY_GOAL_STEP) - 1

/** Количество промежуточных шагов ползунка скоростного раунда. */
private fun rushSteps(): Int =
    (SettingsUiState.MAX_RUSH_SECONDS - SettingsUiState.MIN_RUSH_SECONDS) - 1

/** Количество промежуточных шагов ползунка контекстных пропусков. */
private fun clozeSteps(): Int =
    (SettingsUiState.MAX_CLOZE_SECONDS - SettingsUiState.MIN_CLOZE_SECONDS) - 1
