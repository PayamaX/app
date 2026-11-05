package no1.payamax.cleanCompose.core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import no1.payamax.contracts.PayamakUsabilityClass

// Semantic Colors - Material 3 Expressive
private val PrimaryLight = Color(0xFF6750A4)
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFE8DEF8)
private val OnPrimaryContainerLight = Color(0xFF210F48)

private val PrimaryDark = Color(0xFFD0BCFF)
private val OnPrimaryDark = Color(0xFF381E72)
private val PrimaryContainerDark = Color(0xFF4F378B)
private val OnPrimaryContainerDark = Color(0xFFE8DEF8)

private val SurfaceLight = Color(0xFFFEF7FF)
private val SurfaceContainerLight = Color(0xFFF3EDF7)
private val SurfaceContainerHighLight = Color(0xFFECE6F0)
private val OnSurfaceLight = Color(0xFF1D1B20)
private val OnSurfaceVariantLight = Color(0xFF49454F)

private val SurfaceDark = Color(0xFF141218)
private val SurfaceContainerDark = Color(0xFF211F26)
private val SurfaceContainerHighDark = Color(0xFF2B2930)
private val OnSurfaceDark = Color(0xFFE6E1E5)
private val OnSurfaceVariantDark = Color(0xFFCAC4D0)

// Usability Class Semantic Colors
val UsabilityImportantBg = Color(0xFFE8DEF8)
val UsabilityImportantText = Color(0xFF210F48)
val UsabilityImportantStroke = Color(0xFF6750A4)

val UsabilityUsableBg = Color(0xFFE8F8E9)
val UsabilityUsableText = Color(0xFF003913)
val UsabilityUsableStroke = Color(0xFF228B22)

val UsabilitySpamBg = Color(0xFFFFDAD6)
val UsabilitySpamText = Color(0xFF410002)
val UsabilitySpamStroke = Color(0xFFBA1A1A)

val UsabilityUnknownBg = Color(0xFFFFDCC3)
val UsabilityUnknownText = Color(0xFF301400)
val UsabilityUnknownStroke = Color(0xFFE66700)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

object ColorSchemeExtension {

    @Composable
    fun getUsabilityColors(clazz: PayamakUsabilityClass): Triple<Color, Color, Color> {
        return when (clazz) {
            PayamakUsabilityClass.Important -> Triple(UsabilityImportantBg, UsabilityImportantText, UsabilityImportantStroke)
            PayamakUsabilityClass.Usable -> Triple(UsabilityUsableBg, UsabilityUsableText, UsabilityUsableStroke)
            PayamakUsabilityClass.Spam -> Triple(UsabilitySpamBg, UsabilitySpamText, UsabilitySpamStroke)
            PayamakUsabilityClass.Unknown -> Triple(UsabilityUnknownBg, UsabilityUnknownText, UsabilityUnknownStroke)
        }
    }

    val ColorScheme.onBackgroundContainer: Color @Composable get() = if (isSystemInDarkTheme()) SurfaceContainerDark else SurfaceContainerLight
    val ColorScheme.onBackgroundText: Color @Composable get() = if (isSystemInDarkTheme()) OnSurfaceVariantDark else OnSurfaceVariantLight
    val ColorScheme.primary500: Color @Composable get() = if (isSystemInDarkTheme()) PrimaryDark else PrimaryLight
    val ColorScheme.staticWhite: Color @Composable get() = Color.White
    val ColorScheme.staticBlack: Color @Composable get() = Color.Black
}

