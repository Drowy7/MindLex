package ru.mindlex.data.repository

import kotlinx.coroutines.flow.StateFlow
import ru.mindlex.data.local.TokenState
import ru.mindlex.data.local.TokenStore
import ru.mindlex.data.remote.MindLexApi
import ru.mindlex.data.remote.dto.AuthResponse
import ru.mindlex.data.remote.dto.LoginRequest
import ru.mindlex.data.remote.dto.RegisterRequest
import ru.mindlex.data.remote.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Регистрация, вход и хранение токена.
 * Сервер выдаёт JWT, клиент лишь сохраняет его и подставляет в запросы.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: MindLexApi,
    private val apiCaller: ApiCaller,
    private val tokenStore: TokenStore,
) {

    val authState: StateFlow<TokenState> = tokenStore.state

    fun isLoggedIn(): Boolean = tokenStore.token() != null

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        apiCaller.call { api.login(LoginRequest(email.trim(), password)) }
            .onSuccess { tokenStore.save(it.accessToken) }

    suspend fun register(email: String, password: String, displayName: String): Result<AuthResponse> =
        apiCaller.call {
            api.register(RegisterRequest(email.trim(), password, displayName.trim()))
        }.onSuccess { tokenStore.save(it.accessToken) }

    suspend fun profile(): Result<UserDto> = apiCaller.call { api.me() }

    suspend fun logout() {
        tokenStore.clear()
    }
}
