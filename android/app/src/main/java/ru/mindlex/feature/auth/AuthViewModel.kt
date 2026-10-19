package ru.mindlex.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mindlex.data.repository.AuthRepository
import javax.inject.Inject

/** Минимальная длина пароля, принимаемая сервером и проверяемая локально. */
private const val MIN_PASSWORD_LENGTH = 6

/**
 * Логика экрана входа и регистрации.
 *
 * <p>Перед обращением к серверу данные проверяются локально: так пользователь
 * получает мгновенный ответ без лишнего сетевого запроса.</p>
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())

    /** Текущее состояние экрана. */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Изменение адреса электронной почты. */
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    /** Изменение пароля. */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    /** Изменение отображаемого имени (режим регистрации). */
    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value, error = null) }
    }

    /** Переключение между входом и регистрацией. */
    fun toggleMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }
    }

    /** Скрытие сообщения об ошибке. */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Отправляет форму: вход или регистрация в зависимости от режима.
     *
     * @param onSuccess вызывается после успешной аутентификации
     */
    fun submit(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.isLoading) {
            return
        }

        val validationError = validate(current)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = if (current.isRegisterMode) {
                authRepository.register(current.email, current.password, current.displayName)
            } else {
                authRepository.login(current.email, current.password)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Не удалось выполнить запрос. Попробуйте ещё раз.",
                        )
                    }
                }
        }
    }

    /** Локальная проверка формы; возвращает текст ошибки или null, если данные корректны. */
    private fun validate(state: AuthUiState): String? {
        val email = state.email.trim()
        return when {
            email.isEmpty() -> "Введите e-mail"
            !email.contains("@") -> "E-mail должен содержать символ @"
            state.password.length < MIN_PASSWORD_LENGTH ->
                "Пароль должен быть не короче $MIN_PASSWORD_LENGTH символов"
            state.isRegisterMode && state.displayName.isBlank() -> "Введите имя"
            else -> null
        }
    }
}
