package no1.payamax.cleanCompose.core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Error = Color(0xFFD92D20)
private val Error50 = Color(0xFFFEF3F2)
private val Error200 = Color(0xFFFEE4E2)
private val Error400 = Color(0xFFFECDCA)
private val Error600 = Color(0xFFFDA29B)
private val Error800 = Color(0xFF912018)
private val ErrorOpacity50 = Color(0x80F04438)
private val ErrorOpacity25 = Color(0x40F04438)
private val ErrorOpacity12 = Color(0x1FF04438)
private val ErrorOpacity8 = Color(0x14F04438)

private val Warning = Color(0xFFF79009)
private val Warning100 = Color(0xFFFEF0C7)
private val Warning200 = Color(0xFFFEDF89)
private val Warning300 = Color(0xFFFEC84B)
private val Warning600 = Color(0xFFDC6803)
private val Warning800 = Color(0xFF93370D)
private val WarningOpacity50 = Color(0x80FEDF89)
private val WarningOpacity25 = Color(0x40FEDF89)
private val WarningOpacity12 = Color(0x1FFEDF89)
private val WarningOpacity8 = Color(0x14FEDF89)

private val Success = Color(0xFF31B83D)
private val Success100 = Color(0xFFE8F8E9)
private val Success200 = Color(0xFFA6F4AC)
private val Success300 = Color(0xFF6CE976)
private val Success700 = Color(0xFF027A38)
private val Success800 = Color(0xFF00692F)
private val SuccessOpacity50 = Color(0x80FEDF89)
private val SuccessOpacity25 = Color(0x40FEDF89)
private val SuccessOpacity12 = Color(0x1FFEDF89)
private val SuccessOpacity8 = Color(0x14FEDF89)

private val Info = Color(0xFF05A3E8)
private val Info100 = Color(0xFFE1F4FC)
private val Info200 = Color(0xFFC0E8F9)
private val Info400 = Color(0xFF81D0F3)
private val Info600 = Color(0xFF4FBFEE)
private val Info800 = Color(0xFF0084C6)
private val InfoOpacity50 = Color(0x8005A3E8)
private val InfoOpacity25 = Color(0x4005A3E8)
private val InfoOpacity12 = Color(0x1F05A3E8)
private val InfoOpacity8 = Color(0x1405A3E8)

private val Gray25 = Color(0xFFF7F8FA)
private val Gray50 = Color(0xFFF5F5F9)
private val Gray100 = Color(0xFFE6E7EF)
private val Gray200 = Color(0xFFD3D6E0)
private val Gray300 = Color(0xFF9DA1B1)
private val Gray400 = Color(0xFF6F7285)
private val Gray500 = Color(0xFF484B62)
private val Gray600 = Color(0xFF292A33)
private val Gray700 = Color(0xFF292A33) // TODO: Correct color code with new design
private val Gray800 = Color(0xFF212226)

private val TintWhite = Color(0xFFFFFFFF)
private val TintOpacity85 = Color(0xD9FFFFFF)
private val TintOpacity70 = Color(0xB3FFFFFF)
private val TintOpacity50 = Color(0x80FFFFFF)
private val TintOpacity25 = Color(0x40FFFFFF)
private val TintOpacity12 = Color(0x1FFFFFFF)
private val TintOpacity8 = Color(0x14FFFFFF)
private val TintOpacity4 = Color(0x0AFFFFFF)

private val ShadeBlack = Color(0xFF16171A)
private val ShadeOpacity85 = Color(0xD90B0C0D)
private val ShadeOpacity70 = Color(0xB30B0C0D)
private val ShadeOpacity50 = Color(0x800B0C0D)
private val ShadeOpacity25 = Color(0x400B0C0D)
private val ShadeOpacity12 = Color(0x1F0B0C0D)
private val ShadeOpacity8 = Color(0x140B0C0D)
private val ShadeOpacity4 = Color(0x0A0B0C0D)

private val Primary = Color(0xFF540FDF)
private val Primary25 = Color(0xFFFCFAFF)
private val Primary50 = Color(0xFFF9F5FF)
private val Primary100 = Color(0xFFF4EBFF)
private val Primary200 = Color(0xFFE9D7FE)
private val Primary300 = Color(0xFFD6BBFB)
private val Primary400 = Color(0xFFB692F6)
private val Primary500 = Color(0xFF9E77ED)
private val Primary800 = Color(0xFF53389E)
private val Primary900 = Color(0xFF42307D)
private val PrimaryOpacity85 = Color(0xD942307D)
private val PrimaryOpacity70 = Color(0xB342307D)
private val PrimaryOpacity50 = Color(0x8042307D)
private val PrimaryOpacity25 = Color(0x4042307D)
private val PrimaryOpacity12 = Color(0x1F42307D)
private val PrimaryOpacity8 = Color(0x1442307D)
private val PrimaryOpacity4 = Color(0x0A42307D)

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)
private val Transparent = Color(0x00000000)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = White,
    error = Error,
)

val DarkColorScheme = darkColorScheme(
    primary = Primary,
    background = ShadeBlack,
    error = Error,
)

object ColorSchemeExtension {

    // Theme
    val ColorScheme.onBackgroundContainer: Color @Composable get() = if (isSystemInDarkTheme()) Gray800 else Gray50
    val ColorScheme.onBackgroundStroke: Color @Composable get() = if (isSystemInDarkTheme()) Gray600 else Gray200
    val ColorScheme.onBackgroundText: Color @Composable get() = if (isSystemInDarkTheme()) Gray100 else Gray600
    val ColorScheme.surfaceSelectedStroke: Color @Composable get() = if (isSystemInDarkTheme()) Gray500 else Gray300
    val ColorScheme.trueContainer: Color @Composable get() = if (isSystemInDarkTheme()) SuccessOpacity8 else Success100
    val ColorScheme.falseContainer: Color @Composable get() = if (isSystemInDarkTheme()) ErrorOpacity8 else Error200
    val ColorScheme.warningContainer: Color @Composable get() = if (isSystemInDarkTheme()) WarningOpacity8 else Warning100

    val ColorScheme.trueContainerStroke: Color @Composable get() = if (isSystemInDarkTheme()) Success700 else Success
    val ColorScheme.falseContainerStroke: Color @Composable get() = if (isSystemInDarkTheme()) Error else Error
    val ColorScheme.warningContainerStroke: Color @Composable get() = if (isSystemInDarkTheme()) WarningOpacity50 else Warning

    // Stroke
    val ColorScheme.primaryStroke: Color @Composable get() = Primary800
    val ColorScheme.successStroke: Color @Composable get() = Success700
    val ColorScheme.errorStroke: Color @Composable get() = Error800
    val ColorScheme.warningStroke: Color @Composable get() = Warning600
    val ColorScheme.infoStroke: Color @Composable get() = Info600

    // Button
    val ColorScheme.buttonText: Color @Composable get() = TintWhite
    val ColorScheme.disableButtonText: Color @Composable get() = Gray300

    // Card
    val ColorScheme.cardStroke: Color @Composable get() = if (isSystemInDarkTheme()) Gray600 else Gray100

    val ColorScheme.errorOpacity8: Color @Composable get() = ErrorOpacity8

    val ColorScheme.warning: Color @Composable get() = Warning
    val ColorScheme.warningOpacity8: Color @Composable get() = WarningOpacity8

    val ColorScheme.success: Color @Composable get() = Success
    val ColorScheme.successOpacity8: Color @Composable get() = SuccessOpacity8

    val ColorScheme.info: Color @Composable get() = Info
    val ColorScheme.infoOpacity25: Color @Composable get() = InfoOpacity25
    val ColorScheme.infoOpacity8: Color @Composable get() = InfoOpacity8

    val ColorScheme.gray200: Color @Composable get() = Gray200
    val ColorScheme.gray300: Color @Composable get() = Gray300

    val ColorScheme.primary300: Color @Composable get() = Primary300
    val ColorScheme.primary50: Color @Composable get() = Primary50
    val ColorScheme.primary500: Color @Composable get() = Primary500
    val ColorScheme.primary800: Color @Composable get() = Primary800

    val ColorScheme.staticWhite: Color @Composable get() = White
    val ColorScheme.staticBlack: Color @Composable get() = Black

    val ColorScheme.transparent: Color @Composable get() = Transparent
}
