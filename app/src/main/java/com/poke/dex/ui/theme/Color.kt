package com.poke.dex.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val PokedexRed = Color(0xFFE3350D)
val PokedexYellow = Color(0xFFFFCB05)
val PokedexBlue = Color(0xFF31A7D7)
val PokemonSlate = Color(0xFF4A4A4A)
val SurfaceDarkText = Color(0xFF1F1F1F)

// Helper function to return colors matching official element types
fun getPokemonTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Color(0xFFFFA447)
        "water" -> Color(0xFF569FFF)
        "grass" -> Color(0xFF70D090)
        "electric" -> Color(0xFFF7D02C)
        "poison" -> Color(0xFFA33EA1)
        "flying" -> Color(0xFFA98FF3)
        "bug" -> Color(0xFFA6B91A)
        "normal" -> Color(0xFFA8A77A)
        else -> Color(0xFFB7B7C7) // Fallback default grey background badge
    }
}