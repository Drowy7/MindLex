package ru.mindlex.data.repository

import ru.mindlex.data.remote.MindLexApi
import ru.mindlex.data.remote.dto.DashboardDto
import ru.mindlex.data.remote.dto.DeckDto
import ru.mindlex.data.remote.dto.DeckRequest
import ru.mindlex.data.remote.dto.SettingsDto
import ru.mindlex.data.remote.dto.SettingsRequest
import ru.mindlex.data.remote.dto.SynonymChainDto
import ru.mindlex.data.remote.dto.WordDto
import ru.mindlex.data.remote.dto.WordProgressDto
import ru.mindlex.data.remote.dto.WordRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Учебный контент: колоды, слова, прогресс, настройки и сводка главного экрана.
 * Здесь же сосредоточены все CRUD-операции, доступные пользователю.
 */
@Singleton
class LearningRepository @Inject constructor(
    private val api: MindLexApi,
    private val apiCaller: ApiCaller,
) {

    suspend fun dashboard(): Result<DashboardDto> = apiCaller.call { api.dashboard() }

    suspend fun decks(): Result<List<DeckDto>> =
        apiCaller.call { api.decks().content }

    suspend fun createDeck(name: String, description: String?, language: String): Result<DeckDto> =
        apiCaller.call { api.createDeck(DeckRequest(name = name, language = language, description = description)) }

    suspend fun updateDeck(deckId: String, name: String, description: String?, language: String): Result<DeckDto> =
        apiCaller.call { api.updateDeck(deckId, DeckRequest(name = name, language = language, description = description)) }

    suspend fun deleteDeck(deckId: String): Result<Unit> =
        apiCaller.call { api.deleteDeck(deckId) }.map { }

    suspend fun words(deckId: String): Result<List<WordDto>> =
        apiCaller.call { api.words(deckId).content }

    suspend fun createWord(
        deckId: String,
        term: String,
        translation: String,
        transcription: String?,
        partOfSpeech: String?,
        example: String?,
        alternatives: List<String>,
    ): Result<WordDto> = apiCaller.call {
        api.createWord(
            deckId,
            WordRequest(
                term = term,
                translation = translation,
                transcription = transcription?.ifBlank { null },
                partOfSpeech = partOfSpeech?.ifBlank { null },
                example = example?.ifBlank { null },
                alternatives = alternatives,
            ),
        )
    }

    suspend fun updateWord(
        wordId: String,
        term: String,
        translation: String,
        transcription: String?,
        partOfSpeech: String?,
        example: String?,
        alternatives: List<String>,
    ): Result<WordDto> = apiCaller.call {
        api.updateWord(
            wordId,
            WordRequest(
                term = term,
                translation = translation,
                transcription = transcription?.ifBlank { null },
                partOfSpeech = partOfSpeech?.ifBlank { null },
                example = example?.ifBlank { null },
                alternatives = alternatives,
            ),
        )
    }

    suspend fun deleteWord(wordId: String): Result<Unit> =
        apiCaller.call { api.deleteWord(wordId) }.map { }

    suspend fun progress(): Result<List<WordProgressDto>> =
        apiCaller.call { api.progress().content }

    suspend fun settings(): Result<SettingsDto> = apiCaller.call { api.settings() }

    suspend fun updateSettings(request: SettingsRequest): Result<SettingsDto> =
        apiCaller.call { api.updateSettings(request) }

    suspend fun synonymChains(): Result<List<SynonymChainDto>> =
        apiCaller.call { api.synonymChains() }
}
