package com.example.resqprototype.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmergencyRedLightDark,
    onPrimary = Color(0xFF690005),
    primaryContainer = EmergencyRedDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = CautionAmber,
    tertiary = SafetyGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedContainer,
    onPrimaryContainer = EmergencyRedOnContainer,
    secondary = CautionAmber,
    tertiary = SafetyGreen,
    background = Color(0xFFFCFCFF),
    surface = Color(0xFFFCFCFF),
    surfaceVariant = Color(0xFFF0F0F5)
)

@Composable
fun RESQPrototypeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize brand emergency red
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
