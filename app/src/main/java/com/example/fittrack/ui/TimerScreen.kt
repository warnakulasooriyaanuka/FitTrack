package com.example.fittrack.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import kotlinx.coroutines.delay

@Composable
fun RestTimerScreen(onBack: () -> Unit) {
    var totalSec by remember { mutableStateOf(90) }
    var remainingMs by remember { mutableStateOf(90_000L) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var endAt by remember { mutableStateOf(0L) }
    val view = LocalView.current

    // keep the screen awake while counting down
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    // re-keyed on endAt so +/-15 s also works while the timer is running
    LaunchedEffect(running, endAt) {
        if (!running) return@LaunchedEffect
        while (true) {
            val left = endAt - System.currentTimeMillis()
            if (left <= 0) break
            remainingMs = left
            delay(50)
        }
        remainingMs = 0
        running = false
        finished = true
    }

    // beep once when the countdown finishes (separate effect so it isn't cancelled with the timer loop)
    LaunchedEffect(finished) {
        if (!finished) return@LaunchedEffect
        val tone = runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90) }.getOrNull()
        try {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            delay(900)
        } finally {
            tone?.release()
        }
    }

    fun pick(sec: Int) {
        totalSec = sec; remainingMs = sec * 1000L; running = false; finished = false
    }

    fun adjust(deltaMs: Long) {
        finished = false
        val newLeft = (remainingMs + deltaMs).coerceAtLeast(5_000L)
        if (running) endAt += (newLeft - remainingMs)
        remainingMs = newLeft
        totalSec = maxOf(totalSec, (newLeft / 1000).toInt())
    }

    val seconds = ((remainingMs + 999) / 1000).toInt()
    val progress = if (totalSec > 0) remainingMs / (totalSec * 1000f) else 0f

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth()) { ScreenHeader("Rest timer", onBack = onBack) }
        Spacer(Modifier.height(36.dp))

        RingProgress(
            progress = progress,
            size = 260.dp,
            stroke = 14.dp,
            trackColor = FitColors.Outline,
            color = FitColors.Accent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "%d:%02d".format(seconds / 60, seconds % 60),
                    fontSize = 56.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = FitColors.TextHigh
                )
                Text(
                    when {
                        finished -> "Go! 💪"
                        running -> "Resting…"
                        else -> "Ready"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (finished) FitColors.AccentLight else FitColors.TextMid
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60, 90, 120, 180).forEach { s ->
                SelectPill(if (s >= 60) "${s / 60}:${"%02d".format(s % 60)}" else "0:$s", totalSec == s, { pick(s) })
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallPill("−15 s", { adjust(-15_000L) })
            SmallPill("+15 s", { adjust(15_000L) })
        }

        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GhostButton("Reset", onClick = { pick(totalSec) }, modifier = Modifier.weight(1f))
            GradientButton(
                if (running) "Pause" else if (finished) "Again" else "Start",
                onClick = {
                    if (running) running = false
                    else {
                        if (finished || remainingMs <= 0) remainingMs = totalSec * 1000L
                        finished = false
                        endAt = System.currentTimeMillis() + remainingMs
                        running = true
                    }
                },
                modifier = Modifier.weight(1.4f)
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
