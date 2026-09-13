package com.isx3i.nitrokill.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NitroKillColorScheme = darkColorScheme(
    primary = NitroPurple,
    secondary = NitroBlue,
    tertiary = NitroRed,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = NitroRed
)

/**
 * سمة NitroKill: تصميم داكن ثابت بألوان متدرجة (بنفسجي/أزرق كهربائي)
 * بغض النظر عن سمة النظام، للحفاظ على هوية بصرية موحدة للتطبيق.
 */
@Composable
fun NitroKillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NitroKillColorScheme,
        typography = NitroKillTypography,
        content = content
    )
}
