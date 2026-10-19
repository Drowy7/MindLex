package ru.mindlex.feature.auth

/**
 * Состояние экрана входа и регистрации.
 *
 * @property email введённый адрес электронной почты
 * @property password введённый пароль
 * @property displayName отображаемое имя (используется только при регистрации)
 * @property isRegisterMode true — режим регистрации, false — режим входа
 * @property isLoading true, пока выполняется запрос к серверу
 * @property error текст ошибки для показа пользователю
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
