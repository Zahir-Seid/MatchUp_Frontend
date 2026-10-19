package et.matchup.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MatchUpColors {
    val Background = Color(0xFF070B09)
    val BackgroundAlt = Color(0xFF0A100D)
    val Surface = Color(0xFF0E1511)
    val SurfaceElevated = Color(0xFF15201A)
    val SurfaceHigh = Color(0xFF1B2820)
    val Lime = Color(0xFFD7FF4F)
    val LimeDeep = Color(0xFFB8E62E)
    val OnLime = Color(0xFF111800)
    val Mint = Color(0xFF8DE8C5)
    val Amber = Color(0xFFFFB86B)
    val Red = Color(0xFFFF7272)
    val Blue = Color(0xFF82B7FF)
    val Text = Color(0xFFF3F7F1)
    val TextMuted = Color(0xFF9AAFA3)
    val TextFaint = Color(0xFF6E8277)
    val Outline = Color(0xFF2A3A30)
    val OutlineSoft = Color(0xFF1B2921)
    val White = Color(0xFFFFFFFF)
    val Scrim = Color(0xCC050806)
}

private val colors = darkColorScheme(
    primary = MatchUpColors.Lime,
    onPrimary = MatchUpColors.OnLime,
    primaryContainer = Color(0xFF26350D),
    onPrimaryContainer = MatchUpColors.Lime,
    secondary = MatchUpColors.Mint,
    onSecondary = Color(0xFF072018),
    secondaryContainer = Color(0xFF15352A),
    onSecondaryContainer = MatchUpColors.Mint,
    tertiary = MatchUpColors.Amber,
    onTertiary = Color(0xFF2A1600),
    tertiaryContainer = Color(0xFF3A2713),
    onTertiaryContainer = MatchUpColors.Amber,
    background = MatchUpColors.Background,
    onBackground = MatchUpColors.Text,
    surface = MatchUpColors.Surface,
    onSurface = MatchUpColors.Text,
    surfaceVariant = MatchUpColors.SurfaceElevated,
    onSurfaceVariant = MatchUpColors.TextMuted,
    outline = MatchUpColors.Outline,
    outlineVariant = MatchUpColors.OutlineSoft,
    error = MatchUpColors.Red,
    onError = Color(0xFF2B0606),
    errorContainer = Color(0xFF3A1414),
    onErrorContainer = MatchUpColors.Red,
    scrim = MatchUpColors.Scrim,
    inverseSurface = MatchUpColors.Text,
    inverseOnSurface = MatchUpColors.Background,
    inversePrimary = MatchUpColors.LimeDeep,
)

private val sans = FontFamily.SansSerif
private val display = FontFamily.SansSerif

private val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.4).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.2).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
    ),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun MatchUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
