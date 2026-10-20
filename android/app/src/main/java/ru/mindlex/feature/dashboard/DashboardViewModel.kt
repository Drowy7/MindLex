package ru.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mindlex.data.repository.AuthRepository
import ru.mindlex.data.repository.LearningRepository
import javax.inject.Inject

/**
 * Логика главного экрана: сводка прогресса, статистика слов, механики и последние сессии.
 *
 * <p>Данные грузятся один раз при создании модели, повторная загрузка вызывается
 * вручную через [load] (например, по кнопке «Повторить») или [refresh].</p>
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())

    /** Текущее состояние экрана. */
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Первая или повторная загрузка данных без индикатора обновления. */
    fun load() {
        loadInternal(isRefresh = false)
    }

    /** Обновление данных с индикатором обновления. */
    fun refresh() {
        loadInternal(isRefresh = true)
    }

    /** Выход из аккаунта: корневой граф навигации сам покажет экран входа. */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun loadInternal(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.dashboard == null,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }

            coroutineScope {
                // Три независимых запроса выполняются параллельно.
                val dashboardDeferred = async { learningRepository.dashboard() }
                val decksDeferred = async { learningRepository.decks() }
                val profileDeferred = async { authRepository.profile() }

                val dashboardResult = dashboardDeferred.await()
                val decksResult = decksDeferred.await()
                val profileResult = profileDeferred.await()

                val dashboard = dashboardResult.getOrNull()
                if (dashboard == null) {
                    val message = dashboardResult.exceptionOrNull()?.message
                        ?: "Не удалось загрузить данные главного экрана"
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = message)
                    }
                    return@coroutineScope
                }

                // Имя колоды берём из списка колод; если его там нет — из самой сводки.
                val deckName = decksResult.getOrNull()
                    ?.firstOrNull { it.id == dashboard.selectedDeckId }
                    ?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: dashboard.selectedDeckName?.takeIf { it.isNotBlank() }

                val userName = profileResult.getOrNull()
                    ?.displayName
                    ?.takeIf { it.isNotBlank() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        dashboard = dashboard,
                        deckName = deckName,
                        userName = userName,
                    )
                }
            }
        }
    }
}
