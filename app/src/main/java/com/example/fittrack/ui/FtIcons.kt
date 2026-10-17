package com.example.fittrack.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.R

/**
 * FitTrack professional icon pack.
 * The PNG tiles are 200x184, so we keep that ratio and round the corners slightly.
 */
@Composable
fun FtIcon(
    @DrawableRes res: Int,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alpha: Float = 1f
) {
    Image(
        painter = androidx.compose.ui.res.painterResource(res),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(width = size, height = size * 0.92f)
            .clip(RoundedCornerShape(size * 0.2f))
            .alpha(alpha)
    )
}

/** Maps the emoji keys used by the app's data to an icon from the pack (null = no matching icon). */
@DrawableRes
fun iconResFor(key: String): Int? = when (key) {
    "🏃" -> R.drawable.running
    "🚴" -> R.drawable.cycling
    "🏋️", "🦵" -> R.drawable.gym
    "🧘" -> R.drawable.yoga
    "🏊" -> R.drawable.swimming
    "⚡" -> R.drawable.hiit
    "💪" -> R.drawable.workouts
    "🔥" -> R.drawable.daily_streak
    "kcal", "🌋" -> R.drawable.calories
    "⏱️" -> R.drawable.minutes
    "💧" -> R.drawable.water
    "🏆", "🏅", "👑" -> R.drawable.awards
    "⚖️" -> R.drawable.body_weight
    "😴" -> R.drawable.restday
    "🌙" -> R.drawable.sleep
    "🎯", "📚", "🧗" -> R.drawable.exercises
    "👟" -> R.drawable.running
    "❤️" -> R.drawable.stayfit
    "🍎", "🍳", "🥗", "🍽️", "🍌" -> R.drawable.food
    "📈" -> R.drawable.growth
    "rest_timer" -> R.drawable.rest_timer
    else -> null
}

/** Shows the pack icon for [key], or falls back to the original emoji if the pack has none. */
@Composable
fun EmojiIcon(key: String, size: Dp = 28.dp, modifier: Modifier = Modifier, alpha: Float = 1f) {
    val res = iconResFor(key)
    if (res != null) FtIcon(res, size, modifier, alpha = alpha)
    else Text(key, fontSize = (size.value * 0.9f).sp, modifier = modifier.alpha(alpha))
}
