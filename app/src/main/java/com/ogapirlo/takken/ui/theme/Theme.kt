package com.ogapirlo.takken.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TColors {
    val Green = Color(0xFF58CC02)
    val GreenDark = Color(0xFF58A700)
    val GreenLight = Color(0xFFD7FFB8)
    val Red = Color(0xFFFF4B4B)
    val RedDark = Color(0xFFEA2B2B)
    val RedLight = Color(0xFFFFDFE0)
    val Blue = Color(0xFF1CB0F6)
    val BlueDark = Color(0xFF1899D6)
    val BlueLight = Color(0xFFDDF4FF)
    val Orange = Color(0xFFFF9600)
    val Gold = Color(0xFFFFC800)
    val Purple = Color(0xFFCE82FF)
    val Gray = Color(0xFFE5E5E5)
    val GrayDark = Color(0xFFAFAFAF)
    val Text = Color(0xFF3C3C3C)
    val SubText = Color(0xFF777777)
    val Background = Color.White
}

@Composable
fun TakkenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = TColors.Green,
            onPrimary = Color.White,
            secondary = TColors.Blue,
            background = TColors.Background,
            surface = TColors.Background,
            onBackground = TColors.Text,
            onSurface = TColors.Text,
            error = TColors.Red,
        ),
        content = content,
    )
}
