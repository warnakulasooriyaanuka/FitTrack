package com.example.fittrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.fittrack.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Palette: true black, #1C1C1E cards, FitTrack lime (#A5FF00) accent, with green / cyan / indigo for the other rings. */
object FitColors {
    val Bg = Color(0xFF000000)
    val Surface = Color(0xFF1C1C1E)
    val SurfaceHigh = Color(0xFF2C2C2E)
    val Outline = Color(0xFF38383A)

    /** FitTrack lime: the app's main accent (on pure black). */
    val Accent = Color(0xFFA5FF00)
    val AccentLight = Color(0xFFC6FF4D)
    val AccentDeep = Color(0xFF7BC200)

    /** Other ring colors. */
    val Green = Color(0xFF3DDC84)   // exercise ring
    val Cyan = Color(0xFF1EEAEF)    // water / stand
    val Indigo = Color(0xFF7D7AFF)  // sleep

    val TextHigh = Color(0xFFFFFFFF)
    val TextMid = Color(0xFF8E8E93)

    val Success = Color(0xFF30D158)
    val Warning = Color(0xFFFF9F0A)
    val Danger = Color(0xFFFF453A)
}

private val FitScheme = darkColorScheme(
    primary = FitColors.Accent,
    onPrimary = Color.Black,
    primaryContainer = FitColors.AccentDeep,
    onPrimaryContainer = Color.Black,
    secondary = FitColors.AccentLight,
    onSecondary = Color.Black,
    background = FitColors.Bg,
    onBackground = FitColors.TextHigh,
    surface = FitColors.Surface,
    onSurface = FitColors.TextHigh,
    surfaceVariant = FitColors.SurfaceHigh,
    onSurfaceVariant = FitColors.TextMid,
    outline = FitColors.Outline,
    outlineVariant = FitColors.Outline,
    error = FitColors.Danger,
    onError = Color.White
)

/** Poppins for text, Outfit for titles and big numbers (both open-licence fonts bundled in res/font). */
val FitBody = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_medium, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bold, FontWeight.ExtraBold)
)
val FitDisplay = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_regular, FontWeight.Medium),
    Font(R.font.outfit_bold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_bold, FontWeight.ExtraBold)
)

private val FitTypography = Typography(
    displaySmall = TextStyle(fontFamily = FitDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.4).sp),
    headlineLarge = TextStyle(fontFamily = FitDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = FitDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontFamily = FitDisplay, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FitDisplay, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontFamily = FitBody, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.3.sp)
)


private val FitShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun FitTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FitScheme, typography = FitTypography, shapes = FitShapes, content = content)
}
