package ru.mindlex.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import ru.mindlex.data.remote.dto.AnswerDto
import ru.mindlex.data.remote.dto.AnswerRequest
import ru.mindlex.data.remote.dto.AuthResponse
import ru.mindlex.data.remote.dto.DashboardDto
import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.DeckRequest
import ru.mindlex.data.remote.dto.LoginRequest
import ru.mindlex.data.remote.dto.PageDto
import ru.mindlex.data.remote.dto.QuestionDto
import ru.mindlex.data.remote.dto.RegisterRequest
import ru.mindlex.data.remote.dto.SessionDto
import ru.mindlex.data.remote.dto.SessionSummaryDto
import ru.mindlex.data.remote.dto.SettingsDto
import ru.mindlex.data.remote.dto.SettingsRequest
import ru.mindlex.data.remote.dto.SkipRequest
import ru.mindlex.data.remote.dto.StartSessionRequest
import ru.mindlex.data.remote.dto.SynonymChainDto
import ru.mindlex.data.remote.dto.UserDto
import ru.mindlex.data.remote.dto.WordDto
import ru.mindlex.data.remote.dto.WordProgressDto
import ru.mindlex.data.remote.dto.WordRequest

/**
 * Описание REST-API бэкенда MindLex.
 * Взаимодействие идёт по HTTP/1.1 и JSON — это упрощает
 * балансировку трафика и отладку (см. отчёт, разделы про 12 факторов).
 */
interface MindLexApi {

    // --- Аутентификация ---

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/v1/auth/me")
    suspend fun me(): UserDto

    // --- Главный экран ---

    @GET("api/v1/dashboard")
    suspend fun dashboard(): DashboardDto

    // --- Колоды ---

    @GET("api/v1/decks")
    suspend fun decks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PageDto<DeckDto>

    @POST("api/v1/decks")
    suspend fun createDeck(@Body body: DeckRequest): DeckDto

    @PUT("api/v1/decks/{deckId}")
    suspend fun updateDeck(@Path("deckId") deckId: String, @Body body: DeckRequest): DeckDto

    @DELETE("api/v1/decks/{deckId}")
    suspend fun deleteDeck(@Path("deckId") deckId: String): Response<Unit>

    // --- Слова ---

    @GET("api/v1/decks/{deckId}/words")
    suspend fun words(
        @Path("deckId") deckId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 200,
    ): PageDto<WordDto>

    @POST("api/v1/decks/{deckId}/words")
    suspend fun createWord(@Path("deckId") deckId: String, @Body body: WordRequest): WordDto

    @PUT("api/v1/words/{wordId}")
    suspend fun updateWord(@Path("wordId") wordId: String, @Body body: WordRequest): WordDto

    @DELETE("api/v1/words/{wordId}")
    suspend fun deleteWord(@Path("wordId") wordId: String): Response<Unit>

    // --- Прогресс ---

    @GET("api/v1/progress")
    suspend fun progress(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 200,
    ): PageDto<WordProgressDto>

    // --- Игровые сессии ---

    @POST("api/v1/sessions")
    suspend fun startSession(@Body body: StartSessionRequest): SessionDto

    @GET("api/v1/sessions/{sessionId}")
    suspend fun session(@Path("sessionId") sessionId: String): SessionDto

    @GET("api/v1/sessions")
    suspend fun sessions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageDto<SessionDto>

    /** 204 No Content означает, что вопросы в сессии закончились. */
    @GET("api/v1/sessions/{sessionId}/question")
    suspend fun question(@Path("sessionId") sessionId: String): Response<QuestionDto>

    @POST("api/v1/sessions/{sessionId}/answers")
    suspend fun answer(@Path("sessionId") sessionId: String, @Body body: AnswerRequest): AnswerDto

    @POST("api/v1/sessions/{sessionId}/skip")
    suspend fun skip(@Path("sessionId") sessionId: String, @Body body: SkipRequest): AnswerDto

    @POST("api/v1/sessions/{sessionId}/finish")
    suspend fun finish(@Path("sessionId") sessionId: String): SessionSummaryDto

    // --- Настройки ---

    @GET("api/v1/settings")
    suspend fun settings(): SettingsDto

    @PUT("api/v1/settings")
    suspend fun updateSettings(@Body body: SettingsRequest): SettingsDto

    // --- Цепочки синонимов ---

    @GET("api/v1/synonym-chains")
    suspend fun synonymChains(): List<SynonymChainDto>
}
