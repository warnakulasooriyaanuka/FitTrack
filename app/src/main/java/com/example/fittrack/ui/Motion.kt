package com.example.fittrack.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Space the floating glass tab bar takes at the bottom of tab screens (0 when it is hidden). */
val LocalBarInset = compositionLocalOf<Dp> { 0.dp }

/** Click with a springy "squish" while pressed (no ripple). Put it FIRST in the chain so the whole element scales. */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed && enabled) 0.95f else 1f,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "bounce"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

/** Fades and floats an element up the first time it appears. */
fun Modifier.fadeSlideIn(delayMs: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 420, delayMillis = delayMs, easing = FastOutSlowInEasing))
    }
    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 36.dp.toPx()
    }
}

/** Springy "pop" entrance: grows from 60% with a little overshoot while fading in. */
fun Modifier.popIn(delayMs: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 170f))
    }
    this.graphicsLayer {
        val v = progress.value
        scaleX = 0.6f + 0.4f * v
        scaleY = 0.6f + 0.4f * v
        alpha = v.coerceIn(0f, 1f)
    }
}

/** Slow, gentle up-and-down float (for logos and mascots). */
fun Modifier.floating(amplitude: Dp = 5.dp, periodMs: Int = 3200): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "float")
    val y by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )
    this.graphicsLayer { translationY = y * amplitude.toPx() }
}
