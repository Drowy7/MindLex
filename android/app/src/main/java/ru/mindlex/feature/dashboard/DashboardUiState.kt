package ru.mindlex.feature.dashboard

import ru.mindlex.data.remote.dto.DashboardDto

/**
 * Состояние главного экрана.
 *
 * @property isLoading true при первой загрузке, когда данных ещё нет
 * @property isRefreshing true при обновлении, когда данные уже показаны
 * @property error текст ошибки для показа пользователю
 * @property dashboard сводка прогресса, статистики и механик
 * @property deckName имя выбранной колоды, найденное по списку колод
 * @property userName отображаемое имя пользователя
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val dashboard: DashboardDto? = null,
    val deckName: String? = null,
    val userName: String? = null,
)
