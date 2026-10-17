package ru.mindlex.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import ru.mindlex.data.remote.dto.ApiErrorDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Ошибка, пригодная для показа пользователю. */
class ApiException(message: String) : Exception(message)

/**
 * Единая точка обработки сетевых ошибок: сервер возвращает ошибку в формате
 * {@code ApiError}, поэтому текст можно показать пользователю как есть.
 */
@Singleton
class ApiCaller @Inject constructor(
    private val json: Json,
) {

    suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (http: HttpException) {
        Result.failure(ApiException(messageOf(http)))
    } catch (io: IOException) {
        Result.failure(ApiException("Нет связи с сервером. Проверьте адрес API и запущен ли бэкенд."))
    } catch (other: Exception) {
        Result.failure(ApiException(other.message ?: "Неизвестная ошибка"))
    }

    private fun messageOf(http: HttpException): String {
        val raw = runCatching { http.response()?.errorBody()?.string() }.getOrNull()
        if (!raw.isNullOrBlank()) {
            val parsed = runCatching { json.decodeFromString(ApiErrorDto.serializer(), raw) }.getOrNull()
            val fieldText = parsed?.fieldErrors?.values?.firstOrNull()
            val text = fieldText ?: parsed?.message
            if (!text.isNullOrBlank()) {
                return text
            }
        }
        return when (http.code()) {
            401 -> "Требуется вход в приложение"
            403 -> "Недостаточно прав для этого действия"
            404 -> "Данные не найдены"
            409 -> "Конфликт данных"
            else -> "Ошибка сервера (${http.code()})"
        }
    }
}
