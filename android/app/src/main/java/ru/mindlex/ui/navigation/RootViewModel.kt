package ru.mindlex.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.mindlex.data.local.TokenState
import ru.mindlex.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Состояние аутентификации для корневого графа навигации.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<TokenState> = authRepository.authState
}
