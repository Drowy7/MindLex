package ru.mindlex.data.remote.dto

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Аутентификация и пользователь
// ---------------------------------------------------------------------------

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val language: String = "en",
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long = 0,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String = "ROLE_USER",
    val createdAt: String? = null,
)

// ---------------------------------------------------------------------------
// Колоды и слова
// ---------------------------------------------------------------------------

@Serializable
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
)

@Serializable
data class DeckDto(
    val id: String,
    val ownerId: String? = null,
    val name: String,
    val language: String = "en",
    val description: String? = null,
    val isPublic: Boolean = false,
    val wordCount: Long = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class DeckRequest(
    val name: String,
    val language: String = "en",
    val description: String? = null,
    val isPublic: Boolean? = null,
)

@Serializable
data class WordDto(
    val id: String,
    val deckId: String,
    val term: String,
    val translation: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val example: String? = null,
    val alternatives: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class WordRequest(
    val term: String,
    val translation: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val example: String? = null,
    val alternatives: List<String> = emptyList(),
)

// ---------------------------------------------------------------------------
// Игровые механики
// ---------------------------------------------------------------------------

@Serializable
data class StartSessionRequest(
    val mechanic: String,
    val deckId: String? = null,
)

@Serializable
data class SessionDto(
    val id: String,
    val mechanic: String,
    val status: String,
    val deckId: String? = null,
    val durationSeconds: Int = 0,
    val questionLimit: Int = 0,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skipCount: Int = 0,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val maxCombo: Int = 0,
    val timeRemainingSeconds: Int? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
)

@Serializable
data class QuestionDto(
    val sessionId: String,
    val questionId: String,
    val mechanic: String,
    val index: Int = 1,
    val totalQuestions: Int = 0,
    val promptKind: String,
    val prompt: String,
    val hint: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val example: String? = null,
    val chainStepNumber: Int? = null,
    val chainLength: Int? = null,
    val timeLimitSeconds: Int? = null,
    val hintOptions: List<String>? = null,
)

@Serializable
data class AnswerRequest(
    val questionId: String,
    val answer: String,
    val hintUsed: Boolean = false,
    val timedOut: Boolean = false,
    val elapsedMs: Long = 0,
)

@Serializable
data class SkipRequest(val questionId: String)

@Serializable
data class AnswerDto(
    val sessionId: String,
    val questionId: String,
    val correct: Boolean,
    val quality: Int,
    val expectedAnswer: String? = null,
    val scoreDelta: Int = 0,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val maxCombo: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skipCount: Int = 0,
    val timeRemainingSeconds: Int? = null,
    val sessionFinished: Boolean = false,
    val progress: WordProgressDto? = null,
)

@Serializable
data class SessionSummaryDto(
    val sessionId: String,
    val mechanic: String,
    val score: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skipCount: Int = 0,
    val answeredCount: Int = 0,
    val maxCombo: Int = 0,
    val accuracyPercent: Int = 0,
    val wordsPerMinute: Int = 0,
    val durationSeconds: Int = 0,
    val bestScore: Int = 0,
    val comboRecord: Int = 0,
    val newRecord: Boolean = false,
    val startedAt: String? = null,
    val finishedAt: String? = null,
)

@Serializable
data class WordProgressDto(
    val id: String,
    val wordId: String,
    val term: String? = null,
    val translation: String? = null,
    val status: String,
    val level: Int = 0,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val nextReviewAt: String? = null,
    val lastReviewedAt: String? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val accuracyPercent: Int = 0,
)

// ---------------------------------------------------------------------------
// Настройки и главный экран
// ---------------------------------------------------------------------------

@Serializable
data class SettingsDto(
    val userId: String,
    val dailyGoal: Int = 15,
    val language: String = "en",
    val selectedDeckId: String? = null,
    val preferredTime: String? = null,
    val rushSeconds: Int = 90,
    val clozeSeconds: Int = 35,
    val notificationsEnabled: Boolean = true,
    val currentStreak: Int = 0,
    val visibleStreak: Int = 0,
    val lastStudyDate: String? = null,
    val bestRushScore: Int = 0,
    val maxComboRecord: Int = 0,
    val timezone: String = "UTC",
    val updatedAt: String? = null,
)

@Serializable
data class SettingsRequest(
    val dailyGoal: Int? = null,
    val language: String? = null,
    val selectedDeckId: String? = null,
    val preferredTime: String? = null,
    val rushSeconds: Int? = null,
    val clozeSeconds: Int? = null,
    val notificationsEnabled: Boolean? = null,
    val timezone: String? = null,
)

@Serializable
data class MechanicSummaryDto(
    val mechanic: String,
    val title: String,
    val description: String,
    val availableQuestions: Int = 0,
)

@Serializable
data class DashboardDto(
    val visibleStreak: Int = 0,
    val dailyGoal: Int = 15,
    val todayAnswered: Int = 0,
    val todayCorrect: Int = 0,
    val dailyGoalPercent: Int = 0,
    val dueCount: Long = 0,
    val totalWords: Long = 0,
    val newWords: Long = 0,
    val learningWords: Long = 0,
    val knownWords: Long = 0,
    val bestRushScore: Int = 0,
    val maxComboRecord: Int = 0,
    val selectedDeckId: String? = null,
    val selectedDeckName: String? = null,
    val language: String = "en",
    val mechanics: List<MechanicSummaryDto> = emptyList(),
    val recentSessions: List<SessionDto> = emptyList(),
)

// ---------------------------------------------------------------------------
// Цепочки синонимов, напоминания и ошибки
// ---------------------------------------------------------------------------

@Serializable
data class SynonymChainStepDto(
    val id: String,
    val stepNumber: Int,
    val promptWord: String,
    val acceptedSynonyms: List<String> = emptyList(),
    val hint: String? = null,
)

@Serializable
data class SynonymChainDto(
    val id: String,
    val title: String,
    val language: String = "en",
    val category: String? = null,
    val steps: List<SynonymChainStepDto> = emptyList(),
)

@Serializable
data class StudyReminderDto(
    val id: String,
    val userId: String,
    val plannedAt: String,
    val title: String,
    val body: String? = null,
    val sent: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class StudyReminderRequest(
    val plannedAt: String,
    val title: String,
    val body: String? = null,
    val sent: Boolean? = null,
)

/** Единый формат ошибки бэкенда. */
@Serializable
data class ApiErrorDto(
    val status: Int = 0,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val fieldErrors: Map<String, String>? = null,
)
