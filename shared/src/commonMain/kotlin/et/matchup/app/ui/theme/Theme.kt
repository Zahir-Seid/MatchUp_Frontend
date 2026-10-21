package et.matchup.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Platform palette. MatchUp ships two: the original near-black lime theme and
 * a warm off-white light theme. Both keep the same token names so components
 * only need [MatchUpColors].
 */
data class MatchUpPalette(
    val dark: Boolean,
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHigh: Color,
    val lime: Color,
    val limeDeep: Color,
    val onLime: Color,
    val mint: Color,
    val amber: Color,
    val red: Color,
    val blue: Color,
    val purple: Color,
    val text: Color,
    val textMuted: Color,
    val textFaint: Color,
    val outline: Color,
    val outlineSoft: Color,
    val white: Color,
    val scrim: Color,
)

private val darkPalette = MatchUpPalette(
    dark = true,
    background = Color(0xFF070B09),
    backgroundAlt = Color(0xFF0A100D),
    surface = Color(0xFF0E1511),
    surfaceElevated = Color(0xFF15201A),
    surfaceHigh = Color(0xFF1B2820),
    lime = Color(0xFFD7FF4F),
    limeDeep = Color(0xFFB8E62E),
    onLime = Color(0xFF111800),
    mint = Color(0xFF8DE8C5),
    amber = Color(0xFFFFB86B),
    red = Color(0xFFFF7272),
    blue = Color(0xFF82B7FF),
    purple = Color(0xFFC9A6FF),
    text = Color(0xFFF3F7F1),
    textMuted = Color(0xFF9AAFA3),
    textFaint = Color(0xFF6E8277),
    outline = Color(0xFF2A3A30),
    outlineSoft = Color(0xFF1B2921),
    white = Color(0xFFFFFFFF),
    scrim = Color(0xCC050806),
)

private val lightPalette = MatchUpPalette(
    dark = false,
    background = Color(0xFFF6F9F2),
    backgroundAlt = Color(0xFFEDF2E8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F5EA),
    surfaceHigh = Color(0xFFE4ECDC),
    lime = Color(0xFFD7FF4F),
    // Lime is too bright for text on white, so light mode uses a deeper lime
    // for links and accent copy while lime stays the fill color.
    limeDeep = Color(0xFF5C7A00),
    onLime = Color(0xFF111800),
    mint = Color(0xFF0E7C5A),
    amber = Color(0xFFA85C00),
    red = Color(0xFFC0392B),
    blue = Color(0xFF1D5FBF),
    purple = Color(0xFF6D28D9),
    text = Color(0xFF0C1712),
    textMuted = Color(0xFF4C6157),
    textFaint = Color(0xFF7A8C82),
    outline = Color(0xFFD3DFD5),
    outlineSoft = Color(0xFFE4EDE5),
    white = Color(0xFFFFFFFF),
    scrim = Color(0x99000000),
)

/**
 * Token accessors used across the UI.
 *
 * The active palette is snapshot state so every reader (composition, draw
 * scopes, pointer input) reacts to a system theme change without threading a
 * parameter through ~400 call sites. MatchUp renders a single app frame per
 * process, so a module-level holder is safe here.
 */
object MatchUpColors {
    private var palette by mutableStateOf(darkPalette)

    internal fun update(next: MatchUpPalette) {
        palette = next
    }

    val isDark: Boolean get() = palette.dark

    val Background: Color get() = palette.background
    val BackgroundAlt: Color get() = palette.backgroundAlt
    val Surface: Color get() = palette.surface
    val SurfaceElevated: Color get() = palette.surfaceElevated
    val SurfaceHigh: Color get() = palette.surfaceHigh
    val Lime: Color get() = palette.lime
    val LimeDeep: Color get() = palette.limeDeep
    val OnLime: Color get() = palette.onLime
    val Mint: Color get() = palette.mint
    val Amber: Color get() = palette.amber
    val Red: Color get() = palette.red
    val Blue: Color get() = palette.blue
    val Purple: Color get() = palette.purple
    val Text: Color get() = palette.text
    val TextMuted: Color get() = palette.textMuted
    val TextFaint: Color get() = palette.textFaint
    val Outline: Color get() = palette.outline
    val OutlineSoft: Color get() = palette.outlineSoft
    val White: Color get() = palette.white
    val Scrim: Color get() = palette.scrim
}

private fun MatchUpPalette.toColorScheme() = if (dark) {
    darkColorScheme(
        primary = lime,
        onPrimary = onLime,
        primaryContainer = Color(0xFF26350D),
        onPrimaryContainer = lime,
        secondary = mint,
        onSecondary = Color(0xFF072018),
        secondaryContainer = Color(0xFF15352A),
        onSecondaryContainer = mint,
        tertiary = amber,
        onTertiary = Color(0xFF2A1600),
        tertiaryContainer = Color(0xFF3A2713),
        onTertiaryContainer = amber,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textMuted,
        outline = outline,
        outlineVariant = outlineSoft,
        error = red,
        onError = Color(0xFF2B0606),
        errorContainer = Color(0xFF3A1414),
        onErrorContainer = red,
        scrim = scrim,
        inverseSurface = text,
        inverseOnSurface = background,
        inversePrimary = limeDeep,
    )
} else {
    lightColorScheme(
        primary = limeDeep,
        onPrimary = white,
        primaryContainer = Color(0xFFE9F6C2),
        onPrimaryContainer = Color(0xFF243300),
        secondary = mint,
        onSecondary = white,
        secondaryContainer = Color(0xFFD9F2E8),
        onSecondaryContainer = Color(0xFF06301F),
        tertiary = amber,
        onTertiary = white,
        tertiaryContainer = Color(0xFFFBE9D2),
        onTertiaryContainer = Color(0xFF3D2000),
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textMuted,
        outline = outline,
        outlineVariant = outlineSoft,
        error = red,
        onError = white,
        errorContainer = Color(0xFFF9DEDB),
        onErrorContainer = Color(0xFF410E09),
        scrim = scrim,
        inverseSurface = text,
        inverseOnSurface = background,
        inversePrimary = Color(0xFF7FA300),
    )
}

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
fun MatchUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) darkPalette else lightPalette
    MatchUpColors.update(palette)
    MaterialTheme(
        colorScheme = palette.toColorScheme(),
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
