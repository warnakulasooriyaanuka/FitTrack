package com.example.fittrack.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.data.DailyWorkout
import com.example.fittrack.data.estimateKcal
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import kotlinx.coroutines.delay

/**
 * Guided workout: one exercise at a time, tap "Done set", an automatic rest countdown runs between sets,
 * and the finished session is saved to the workout log (and counts toward streaks and achievements).
 */
@Composable
fun WorkoutSessionScreen(workout: DailyWorkout, onSave: (Int) -> Unit, onBack: () -> Unit) {
    if (workout.exercises.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val view = LocalView.current
    val startedAt = remember { System.currentTimeMillis() }
    var exIndex by remember { mutableStateOf(0) }
    var setIndex by remember { mutableStateOf(0) }
    var resting by remember { mutableStateOf(false) }
    var restEnd by remember { mutableStateOf(0L) }
    var restLeftMs by remember { mutableStateOf(0L) }
    var restTotalMs by remember { mutableStateOf(1L) }
    var finished by remember { mutableStateOf(false) }
    var endedAt by remember { mutableStateOf(0L) }
    var showQuit by remember { mutableStateOf(false) }

    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90) }.getOrNull() }
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false; tone?.release() }
    }

    // rest countdown (re-keyed on restEnd so "+15 s" extends it live)
    LaunchedEffect(resting, restEnd) {
        if (!resting) return@LaunchedEffect
        while (true) {
            val left = restEnd - System.currentTimeMillis()
            if (left <= 0) break
            restLeftMs = left
            delay(50)
        }
        restLeftMs = 0
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 500)
        resting = false
    }

    val current = workout.exercises[exIndex]
    val doneSets = if (finished) workout.totalSets else workout.exercises.take(exIndex).sumOf { it.sets } + setIndex
    val progress = doneSets.toFloat() / workout.totalSets

    fun completeSet() {
        val lastSetOfExercise = setIndex >= current.sets - 1
        val lastExercise = exIndex >= workout.exercises.lastIndex
        if (lastSetOfExercise && lastExercise) {
            finished = true
            endedAt = System.currentTimeMillis()
            return
        }
        if (lastSetOfExercise) { exIndex += 1; setIndex = 0 } else setIndex += 1
        restTotalMs = current.restSec * 1000L
        restLeftMs = restTotalMs
        restEnd = System.currentTimeMillis() + restTotalMs
        resting = true
    }

    fun leave() { if (doneSets > 0 && !finished) showQuit = true else onBack() }
    BackHandler(enabled = doneSets > 0 && !finished) { showQuit = true }

    if (showQuit) {
        AlertDialog(
            onDismissRequest = { showQuit = false },
            containerColor = FitColors.SurfaceHigh,
            title = { Text("Quit workout?", color = FitColors.TextHigh) },
            text = { Text("Your progress in this session won't be saved.", color = FitColors.TextMid) },
            confirmButton = { TextButton(onClick = { showQuit = false; onBack() }) { Text("Quit", color = FitColors.Danger) } },
            dismissButton = { TextButton(onClick = { showQuit = false }) { Text("Keep going", color = FitColors.Accent) } }
        )
    }

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader(workout.title, onBack = { leave() })
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = FitColors.Accent, trackColor = FitColors.Outline
        )
        Spacer(Modifier.height(6.dp))
        Text("$doneSets of ${workout.totalSets} sets", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        Spacer(Modifier.height(20.dp))

        when {
            finished -> {
                val minutes = (((endedAt - startedAt) / 60_000L).toInt()).coerceAtLeast(1)
                val kcal = estimateKcal(workout.type, minutes)
                FitCard {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", fontSize = 56.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Workout complete!", style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
                        Text("Great work. Here's your session:", style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            SummaryStat("$minutes", "min")
                            SummaryStat("${workout.totalSets}", "sets")
                            SummaryStat("~$kcal", "kcal")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                GradientButton("Save to my log", onClick = { onSave(minutes) })
                Spacer(Modifier.height(10.dp))
                GhostButton("Discard", onClick = onBack)
            }

            resting -> {
                val seconds = ((restLeftMs + 999) / 1000).toInt()
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    RingProgress(
                        progress = restLeftMs / restTotalMs.toFloat(),
                        size = 240.dp, stroke = 14.dp,
                        trackColor = FitColors.Outline, color = FitColors.Accent
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "%d:%02d".format(seconds / 60, seconds % 60),
                                fontSize = 52.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = FitColors.TextHigh
                            )
                            Text("Rest", style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("UP NEXT", style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        EmojiIcon(emojiForMuscle(current.exercise.muscle), 30.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            current.exercise.name,
                            style = MaterialTheme.typography.titleLarge, color = FitColors.TextHigh, textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        "Set ${setIndex + 1} of ${current.sets} · ${current.reps}",
                        style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                    )
                }
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton("+15 s", onClick = { restEnd += 15_000L; restTotalMs += 15_000L }, modifier = Modifier.weight(1f))
                    GradientButton("Skip rest", onClick = { resting = false }, modifier = Modifier.weight(1.4f))
                }
            }

            else -> {
                FitCard {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SelectPill(current.exercise.muscle, true, {})
                            Spacer(Modifier.width(8.dp))
                            Text(current.exercise.equipment, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiIcon(emojiForMuscle(current.exercise.muscle), 40.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                current.exercise.name,
                                style = MaterialTheme.typography.headlineMedium, color = FitColors.TextHigh
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Set ${setIndex + 1} of ${current.sets}",
                            style = MaterialTheme.typography.titleMedium, color = FitColors.TextMid
                        )
                        Spacer(Modifier.height(18.dp))
                        Text(current.reps, fontSize = 48.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = FitColors.Accent)
                        Spacer(Modifier.height(14.dp))
                        Text(current.exercise.tip, style = MaterialTheme.typography.bodyLarge, color = FitColors.TextMid)
                    }
                }
                Spacer(Modifier.height(10.dp))
                val next = when {
                    setIndex < current.sets - 1 -> "Next: set ${setIndex + 2} · rest ${current.restSec}s"
                    exIndex < workout.exercises.lastIndex -> "Next exercise: ${workout.exercises[exIndex + 1].exercise.name}"
                    else -> "Last set. Finish strong! 💪"
                }
                Text(next, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                Spacer(Modifier.height(24.dp))
                GradientButton("Done set ✓", onClick = { completeSet() })
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = FitColors.AccentLight)
        Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
    }
}

fun emojiForMuscle(m: String) = when (m) {
    "Chest" -> "💪"
    "Back" -> "🧗"
    "Legs" -> "🦵"
    "Shoulders" -> "🏋️"
    "Arms" -> "💪"
    "Core" -> "🎯"
    else -> "❤️"
}
