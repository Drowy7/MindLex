package ru.mindlex.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mindlex.data.remote.dto.DashboardDto
import ru.mindlex.data.remote.dto.SessionDto
import ru.mindlex.ui.components.EmptyView
import ru.mindlex.ui.components.ErrorView
import ru.mindlex.ui.components.LoadingView
import ru.mindlex.ui.components.MechanicCard
import ru.mindlex.ui.components.MindLexBackground
import ru.mindlex.ui.components.MindLexCard
import ru.mindlex.ui.components.Pill
import ru.mindlex.ui.components.ProgressRing
import ru.mindlex.ui.components.SectionHeader
import ru.mindlex.ui.components.StatTile
import ru.mindlex.ui.theme.Amber
import ru.mindlex.ui.theme.MechanicGradients
import ru.mindlex.ui.theme.Mint
import ru.mindlex.ui.theme.Sky
import ru.mindlex.ui.theme.Violet

/** Сколько последних сессий показываем на главном экране. */
private const val RECENT_SESSIONS_LIMIT = 3

/**
 * Главный экран приложения: прогресс дня, статистика слов, механики тренировок,
 * рекорды и последние сессии.
 *
 * @param onMechanicClick переход к тренировке выбранной механики
 * @param onOpenWords переход к управлению словами колоды
 * @param onOpenSettings переход к настройкам
 */
@Composable
fun DashboardScreen(
    onMechanicClick: (String) -> Unit,
    onOpenWords: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboard = state.dashboard
    val error = state.error

    MindLexBackground {
        when {
            dashboard == null && state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingView(message = "Считаем ваш прогресс…")
                }
            }

            dashboard == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    ErrorView(
                        message = error ?: "Не удалось загрузить данные главного экрана",
                        onRetry = { viewModel.load() },
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DashboardHeader(
                        userName = state.userName,
                        deckName = state.deckName ?: dashboard.selectedDeckName,
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        onOpenWords = onOpenWords,
                        onOpenSettings = onOpenSettings,
                        onLogout = { viewModel.logout() },
                    )

                    if (state.isRefreshing) {
                        LoadingView(message = "Обновляем…")
                    }

                    if (error != null) {
                        ErrorView(message = error, onRetry = { viewModel.load() })
                    }

                    DayProgressCard(dashboard = dashboard)

                    StatsRow(dashboard = dashboard)

                    SectionHeader(title = "Тренировки")

                    if (dashboard.totalWords == 0L) {
                        EmptyView(
                            title = "В колоде пока нет слов",
                            message = "Добавьте первые слова, чтобы тренировки стали доступны.",
                            actionLabel = "Перейти к словам",
                            onAction = onOpenWords,
                        )
                    } else {
                        dashboard.mechanics.forEach { item ->
                            MechanicCard(
                                title = item.title,
                                description = item.description,
                                available = item.availableQuestions,
                                gradient = MechanicGradients.forMechanic(item.mechanic),
                                icon = mechanicIcon(item.mechanic),
                                onClick = { onMechanicClick(item.mechanic) },
                            )
                        }
                    }

                    SectionHeader(title = "Рекорды")
                    RecordsCard(dashboard = dashboard)

                    if (dashboard.recentSessions.isNotEmpty()) {
                        SectionHeader(title = "Последние сессии")
                        dashboard.recentSessions.take(RECENT_SESSIONS_LIMIT).forEach { session ->
                            RecentSessionCard(session = session)
                        }
                    }
                }
            }
        }
    }
}

/** Шапка: приветствие, название колоды и кнопки действий. */
@Composable
private fun DashboardHeader(
    userName: String?,
    deckName: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenWords: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Обновить",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = onOpenWords, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = "Мои слова",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Настройки",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = onLogout, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Выйти",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Text(
            text = "Привет, ${userName ?: "друг"}!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = deckName ?: "Колода не выбрана",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Крупная карточка прогресса дня: кольцо цели, серия занятий и число слов к повторению. */
@Composable
private fun DayProgressCard(
    dashboard: DashboardDto,
    modifier: Modifier = Modifier,
) {
    MindLexCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = dashboard.dailyGoalPercent / 100f,
                modifier = Modifier.size(132.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dashboard.todayAnswered.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "из ${dashboard.dailyGoal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${dashboard.visibleStreak} дн. подряд",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Повторить сегодня: ${dashboard.dueCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Pill(text = "Цель дня: ${dashboard.dailyGoalPercent}%")
            }
        }
    }
}

/** Три плитки со статистикой слов: новые, в процессе, выученные. */
@Composable
private fun StatsRow(
    dashboard: DashboardDto,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            icon = Icons.Rounded.AutoAwesome,
            value = dashboard.newWords.toString(),
            label = "Новые",
            tint = Violet,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = Icons.Rounded.TrendingUp,
            value = dashboard.learningWords.toString(),
            label = "В процессе",
            tint = Sky,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = Icons.Rounded.Verified,
            value = dashboard.knownWords.toString(),
            label = "Выучено",
            tint = Mint,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Личные рекорды пользователя. */
@Composable
private fun RecordsCard(
    dashboard: DashboardDto,
    modifier: Modifier = Modifier,
) {
    MindLexCard(modifier = modifier) {
        RecordRow(label = "Лучший результат в раунде", value = dashboard.bestRushScore.toString())
        Spacer(Modifier.height(12.dp))
        RecordRow(label = "Максимальное комбо", value = dashboard.maxComboRecord.toString())
    }
}

@Composable
private fun RecordRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Строка последней сессии: механика, дата, очки и число правильных ответов. */
@Composable
private fun RecentSessionCard(
    session: SessionDto,
    modifier: Modifier = Modifier,
) {
    MindLexCard(modifier = modifier, contentPadding = 16) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mechanicTitle(session.mechanic),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sessionDate(session.startedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.score} очков",
                    style = MaterialTheme.typography.titleMedium,
                    color = Violet,
                )
                Text(
                    text = "Правильных: ${session.correctCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Иконка карточки механики. */
private fun mechanicIcon(mechanic: String): ImageVector = when (mechanic) {
    "ACTIVE_RECALL" -> Icons.Rounded.Psychology
    "CLOZE" -> Icons.Rounded.TextFields
    "RUSH" -> Icons.Rounded.Bolt
    "SYNONYM_CHAIN" -> Icons.Rounded.Link
    else -> Icons.Rounded.Psychology
}

/** Русское название механики. */
private fun mechanicTitle(mechanic: String): String = when (mechanic) {
    "ACTIVE_RECALL" -> "Активное припоминание"
    "CLOZE" -> "Заполнение пропусков"
    "RUSH" -> "Спринт"
    "SYNONYM_CHAIN" -> "Цепочка синонимов"
    else -> mechanic
}

/** Дата сессии: от ISO-8601 берём только часть yyyy-MM-dd и показываем как есть. */
private fun sessionDate(startedAt: String?): String =
    startedAt?.take(10)?.takeIf { it.isNotBlank() } ?: "Дата неизвестна"
