package ru.mindlex.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mindlexDataStore: DataStore<Preferences> by preferencesDataStore(name = "mindlex_auth")

/** Состояние аутентификации на момент запуска приложения. */
enum class TokenState { UNKNOWN, LOGGED_IN, LOGGED_OUT }

/**
 * Хранилище JWT-токена.
 *
 * <p>Токен держится в памяти для сетевого перехватчика и дублируется в DataStore,
 * чтобы пользователь не входил заново после перезапуска. Сервер при этом остаётся
 * stateless — на устройстве нет ничего, кроме самого токена.</p>
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val dataStore = context.mindlexDataStore

    private val _state = MutableStateFlow(TokenState.UNKNOWN)
    val state: StateFlow<TokenState> = _state.asStateFlow()

    @Volatile
    private var cached: String? = null

    /** Токен для синхронного доступа из перехватчика. */
    fun token(): String? = cached

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val saved = runCatching { dataStore.data.first()[KEY_TOKEN] }.getOrNull()
            cached = saved
            _state.value = if (saved.isNullOrBlank()) TokenState.LOGGED_OUT else TokenState.LOGGED_IN
        }
    }

    suspend fun save(token: String) {
        cached = token
        dataStore.edit { it[KEY_TOKEN] = token }
        _state.value = TokenState.LOGGED_IN
    }

    suspend fun clear() {
        cached = null
        dataStore.edit { it.remove(KEY_TOKEN) }
        _state.value = TokenState.LOGGED_OUT
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("access_token")
    }
}
