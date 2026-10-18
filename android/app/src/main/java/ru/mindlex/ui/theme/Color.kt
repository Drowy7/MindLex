package ru.mindlex.ui.theme

import androidx.compose.ui.graphics.Color

// Тёмная палитра — основная для приложения
val NightBackground = Color(0xFF0B1020)
val NightSurface = Color(0xFF141B33)
val NightSurfaceVariant = Color(0xFF1D2545)
val NightOutline = Color(0xFF2C3661)

val Violet = Color(0xFF7C5CFF)
val VioletLight = Color(0xFFA794FF)
val Mint = Color(0xFF4ED8C0)
val DeepMint = Color(0xFF0D7F6C)
val Amber = Color(0xFFFFB86B)
val Coral = Color(0xFFFF6B81)
val Sky = Color(0xFF58A6FF)

val OnNight = Color(0xFFE9ECF8)
val OnNightMuted = Color(0xFFA7B0D0)

// Светлая палитра
val DayBackground = Color(0xFFF6F7FC)
val DaySurface = Color(0xFFFFFFFF)
val DaySurfaceVariant = Color(0xFFEDEFFA)
val DayOutline = Color(0xFFD3D8EC)
val OnDay = Color(0xFF141A2E)
val OnDayMuted = Color(0xFF5C648A)

/** Градиенты карточек механик. */
object MechanicGradients {

    val ActiveRecall = listOf(Violet, Color(0xFF5B8CFF))
    val Cloze = listOf(Sky, Mint)
    val Rush = listOf(Amber, Coral)
    val SynonymChain = listOf(Mint, Color(0xFF3FA9F5))

    fun forMechanic(mechanic: String): List<Color> = when (mechanic) {
        "ACTIVE_RECALL" -> ActiveRecall
        "CLOZE" -> Cloze
        "RUSH" -> Rush
        "SYNONYM_CHAIN" -> SynonymChain
        else -> ActiveRecall
    }
}
