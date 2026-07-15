package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Gold,
    onPrimary = LuxuryBlack,
    secondary = GoldLight,
    onSecondary = LuxuryBlack,
    background = LuxuryBlack,
    onBackground = MarbleWhite,
    surface = LuxuryDarkGray,
    onSurface = MarbleWhite,
    surfaceVariant = LuxuryMediumGray,
    onSurfaceVariant = TextGray,
    error = ErrorRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Gold,
    onPrimary = LuxuryBlack,
    secondary = GoldDark,
    onSecondary = MarbleWhite,
    background = MarbleWhite,
    onBackground = LuxuryBlack,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = LuxuryBlack,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onSurfaceVariant = LuxuryLightGray,
    error = ErrorRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Branded luxury theme: disable dynamicColor to preserve beautiful black & gold identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // Force dark elegant theme as default showroom experience, but support toggling if they want.
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
