package com.example.ui

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val isLight: Boolean,
    val accentColor: Color,
    val cardBgColor: Color,
    val cardBorderColor: Color,
    val textColorPrimary: Color,
    val textColorSecondary: Color
) {
    companion object {
        fun fromTheme(theme: String): ThemeColors {
            return when (theme) {
                "LIGHT" -> ThemeColors(
                    isLight = true,
                    accentColor = Color(0xFF8B5CF6),
                    cardBgColor = Color.White,
                    cardBorderColor = Color(0xFFE2E8F0),
                    textColorPrimary = Color(0xFF0F172A),
                    textColorSecondary = Color(0xFF475569)
                )
                "COSMIC_FANTASY" -> ThemeColors(
                    isLight = false,
                    accentColor = Color(0xFFBD93F9), // Lavender purple
                    cardBgColor = Color(0xFF150F24), // Cosmic deep purple-black
                    cardBorderColor = Color(0xFF2E1C4E), // Celestial deep border
                    textColorPrimary = Color.White,
                    textColorSecondary = Color(0xFFC7B9E3)
                )
                "CYBER_NEON" -> ThemeColors(
                    isLight = false,
                    accentColor = Color(0xFFFF2D55), // Neon hot pink/red
                    cardBgColor = Color(0xFF0D0E15), // Cyber dark gray-black
                    cardBorderColor = Color(0xFF321424), // Neon cyber pinkish-red border
                    textColorPrimary = Color.White,
                    textColorSecondary = Color(0xFFE48EA0)
                )
                "ROYAL_GOLD" -> ThemeColors(
                    isLight = false,
                    accentColor = Color(0xFFD4AF37), // Royal gold
                    cardBgColor = Color(0xFF14120F), // Deep royal onyx
                    cardBorderColor = Color(0xFF2C2417), // Rich gold-accented border
                    textColorPrimary = Color.White,
                    textColorSecondary = Color(0xFFDFD1B3)
                )
                else -> ThemeColors( // "DEFAULT"
                    isLight = false,
                    accentColor = Color(0xFF00E5FF), // Cyber/Neon Cyan
                    cardBgColor = Color(0xFF121829), // Deep slate black
                    cardBorderColor = Color(0xFF1E293B), // Cold steel border
                    textColorPrimary = Color.White,
                    textColorSecondary = Color(0xFF94A3B8)
                )
            }
        }
    }
}
