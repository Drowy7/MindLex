package ru.mindlex.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mindlex.data.remote.dto.QuestionDto
import ru.mindlex.data.remote.dto.SessionSummaryDto
import ru.mindlex.ui.components.MindLexBackground
import ru.mindlex.ui.components.MindLexCard
import ru.mindlex.ui.components.MindLexTextField
import ru.mindlex.ui.components.Pill
import ru.mindlex.ui.components.PrimaryButton
import ru.mindlex.ui.components.ProgressRing
import ru.mindlex.ui.components.ScreenHeader
import ru.mindlex.ui.components.SecondaryButton
import ru.mindlex.ui.components.SessionProgressBar
import ru.mindlex.ui.theme.Amber
import ru.mindlex.ui.theme.Coral
import ru.mindlex.ui.theme.MechanicGradients
import ru.mindlex.ui.theme.Mint

/**
 * Игровой экран. Один и тот же экран обслуживает все четыре механики:
 * различается только блок вопроса и наличие таймера.
 */
@Composable
fun PracticeScreen(
    mechanic: String,
    onExit: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindLexBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            val summary = state.summary
            if (summary != null) {
                SummaryContent(
                    summary = summary,
                    onPlayAgain = viewModel::playAgain,
                    onExit = onExit,
                )
                return@Column
            }

            ScreenHeader(
                title = PracticeUiState.titleFor(state.mechanic.ifEmpty { mechanic }),
                subtitle = state.question?.let { question ->
                    if (state.total > 0) "Вопрос ${state.index} из ${state.total}" else "Вопрос ${state.index}"
                },
                onBack = onExit,
                compact = true,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = state.score.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            TimerRow(state)

            if (state.total > 0) {
                Spacer(Modifier.height(10.dp))
                SessionProgressBar(current = state.index - 1, total = state.total)
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
            ) {
                val question = state.question
                if (question != null) {
                    QuestionBlock(state = state, question = question)

                    Spacer(Modifier.height(16.dp))

                    if (state.hintVisible && !question.hint.isNullOrBlank()) {
                        MindLexCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = Amber,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = question.hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    FeedbackBlock(state)
                } else if (state.isLoading) {
                    MindLexCard {
                        Text(
                            text = "Готовим задание…",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnswerArea(state = state, viewModel = viewModel)

            state.error?.let { message ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = viewModel::dismissError) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Скрыть",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerRow(state: PracticeUiState) {
    val remaining = state.timeRemaining ?: return
    val isRush = state.isRush
    val urgent = remaining <= 10
    val tint = when {
        urgent -> Coral
        isRush -> Amber
        else -> MaterialTheme.colorScheme.secondary
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isRush) Icons.Rounded.Bolt else Icons.Rounded.Timer,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (isRush) "Осталось $remaining с" else "На задание $remaining с",
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
        Spacer(Modifier.weight(1f))
        if (state.combo >= 2) {
            Pill(
                text = "Комбо ×${comboMultiplierLabel(state.combo)} · ${state.combo}",
                color = Mint,
            )
        }
    }
}

private fun comboMultiplierLabel(combo: Int): String = when {
    combo >= 20 -> "3.0"
    combo >= 10 -> "2.0"
    combo >= 5 -> "1.5"
    else -> "1.0"
}

@Composable
private fun QuestionBlock(state: PracticeUiState, question: QuestionDto) {
    val gradient = MechanicGradients.forMechanic(state.mechanic)

    MindLexCard(gradient = gradient, contentPadding = 24) {
        Text(
            text = when (question.promptKind) {
                "SENTENCE" -> "Заполните пропуск"
                "CHAIN_STEP" -> "Подберите синоним"
                else -> "Переведите на изучаемый язык"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(12.dp))

        when (question.promptKind) {
            "SENTENCE" -> Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )

            "CHAIN_STEP" -> {
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                question.chainStepNumber?.let { step ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Шаг $step из ${question.chainLength ?: step}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            else -> {
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                question.phonetic?.let { phonetic ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "[$phonetic]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                question.partOfSpeech?.let { part ->
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = part,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackBlock(state: PracticeUiState) {
    val feedback = state.feedback ?: return
    val accent = if (feedback.correct) Mint else Coral

    MindLexCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (feedback.correct) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feedback.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!feedback.expected.isNullOrBlank() && !feedback.correct) {
                    Text(
                        text = "Правильный ответ: ${feedback.expected}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (feedback.scoreDelta > 0) {
                Pill(text = "+${feedback.scoreDelta}", color = Amber)
            }
        }
    }
}

@Composable
private fun AnswerArea(state: PracticeUiState, viewModel: PracticeViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val question = state.question

        if (question != null && !question.hint.isNullOrBlank() && !state.hintVisible) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SecondaryButton(
                    text = "Подсказка",
                    onClick = viewModel::showHint,
                    modifier = Modifier.width(160.dp),
                    enabled = state.canAnswer,
                )
                Spacer(Modifier.weight(1f))
                if (state.combo >= 2) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        MindLexTextField(
            value = state.input,
            onValueChange = viewModel::onInputChange,
            label = if (state.isChain) "Синоним" else "Ваш ответ",
            enabled = state.canAnswer,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::submit,
            placeholder = if (state.isChain) "например, clever" else "введите слово",
        )

        Spacer(Modifier.height(12.dp))

        if (state.canAnswer) {
            PrimaryButton(
                text = "Ответить",
                onClick = viewModel::submit,
                enabled = state.input.isNotBlank(),
            )
            Spacer(Modifier.height(8.dp))
            SecondaryButton(text = "Пропустить", onClick = viewModel::skip)
        } else {
            PrimaryButton(
                text = "Проверяем…",
                onClick = {},
                loading = state.isSubmitting,
                enabled = false,
            )
        }
    }
}

@Composable
private fun SummaryContent(
    summary: SessionSummaryDto,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Раунд завершён",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = PracticeUiState.titleFor(summary.mechanic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        ProgressRing(
            progress = summary.accuracyPercent / 100f,
            modifier = Modifier.size(180.dp),
            ringColor = if (summary.accuracyPercent >= 60) Mint else Coral,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = summary.accuracyPercent.toString() + "%",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "точность",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (summary.newRecord) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = Amber,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Новый личный рекорд!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Amber,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        MindLexCard {
            SummaryRow("Очки", summary.score.toString())
            SummaryRow("Правильных ответов", summary.correctCount.toString())
            SummaryRow("Ошибок", summary.incorrectCount.toString())
            SummaryRow("Пропущено", summary.skipCount.toString())
            SummaryRow("Максимальное комбо", summary.maxCombo.toString())
            SummaryRow("Слов в минуту", summary.wordsPerMinute.toString())
            SummaryRow("Лучший результат", summary.bestScore.toString())
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(text = "Ещё раунд", onClick = onPlayAgain)
        Spacer(Modifier.height(10.dp))
        SecondaryButton(text = "Вернуться на главный", onClick = onExit)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}
