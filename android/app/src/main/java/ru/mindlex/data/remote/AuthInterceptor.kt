package ru.mindlex.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import ru.mindlex.data.local.TokenStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Подставляет JWT в заголовок Authorization.
 * Сервер не хранит сессии, поэтому каждый запрос самодостаточен (фактор VI).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.token()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
