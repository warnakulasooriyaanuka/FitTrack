package com.example.fittrack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.data.SleepLog
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun stars(q: Int) = "★".repeat(q.coerceIn(0, 5)) + "☆".repeat(5 - q.coerceIn(0, 5))

@Composable
fun SleepScreen(tvm: TrackerViewModel, onAdd: () -> Unit) {
    val sleep by tvm.sleep.collectAsState()
    val ins by tvm.insights.collectAsState()
    val goals by tvm.goals.collectAsState()
    var editGoal by remember { mutableStateOf(false) }

    val progress by animateFloatAsState((ins.lastSleepMin.toFloat() / goals.sleepMin).coerceIn(0f, 1f), tween(700), label = "sleep")
    val nights = ins.weekSleepMin.filter { it > 0 }
    val avg = if (nights.isEmpty()) 0 else nights.sum() / nights.size

    if (editGoal) {
        NumberDialog("Sleep goal (hours)", goals.sleepMin / 60, { editGoal = false }) { tvm.setSleepGoalMin(it * 60) }
    }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = LocalBarInset.current + 8.dp)) {
            item { ScreenHeader("Sleep") { SmallPill("Goal ${goals.sleepMin / 60}h", { editGoal = true }) } }
            item {
                HeroCard {
                    Column(Modifier.weight(1f)) {
                        Text("LAST SLEEP", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (ins.lastSleepMin > 0) fmtDuration(ins.lastSleepMin) else "—",
                            fontSize = 46.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        Text(
                            if (ins.lastSleepMin > 0) stars(ins.lastSleepQuality) else "Log your first night",
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    RingProgress(progress, color = FitColors.Indigo, trackColor = FitColors.Indigo.copy(alpha = 0.22f)) {
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            item { GradientButton("Log sleep", onClick = onAdd) }
            item { SectionTitle("Last 7 days", if (avg > 0) "Avg ${fmtDuration(avg)}" else null, Modifier.padding(top = 6.dp)) }
            item {
                FitCard {
                    Box(Modifier.padding(16.dp)) {
                        BarChart(
                            values = ins.weekSleepMin.map { it.toFloat() },
                            labels = ins.weekLabels,
                            highlightIndex = ins.weekSleepMin.size - 1,
                            goal = goals.sleepMin.toFloat()
                        )
                    }
                }
            }
            item { SectionTitle("History", modifier = Modifier.padding(top = 6.dp)) }
            if (sleep.isEmpty()) item { EmptyState("🌙", "No sleep logged", "Tap “Log sleep” to record last night.") }
            items(sleep.take(30), key = { it.id }) { s -> SleepRow(s) { tvm.deleteSleep(s) } }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SleepRow(s: SleepLog, onDelete: () -> Unit) {
    val label = runCatching { LocalDate.parse(s.day).format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())) }.getOrDefault(s.day)
    FitCard {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            FtIcon(R.drawable.sleep, 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(fmtDuration(s.durationMin), style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
            }
            Text(stars(s.quality), color = FitColors.AccentLight, fontSize = 14.sp)
            IconButton(onDelete) { FtIcon(R.drawable.delete, 30.dp, contentDescription = "Delete") }
        }
    }
}

@Composable
fun AddSleepScreen(tvm: TrackerViewModel, onDone: () -> Unit) {
    var hours by remember { mutableStateOf(7) }
    var mins by remember { mutableStateOf(30) }
    var quality by remember { mutableStateOf(4) }
    var yesterday by remember { mutableStateOf(false) }
    val total = hours * 60 + mins

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader("Log sleep", onBack = onDone)
        Spacer(Modifier.height(24.dp))
        Text("Woke up", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectPill("Today", !yesterday, { yesterday = false })
            SelectPill("Yesterday", yesterday, { yesterday = true })
        }

        Spacer(Modifier.height(24.dp))
        Text("How long did you sleep?", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        Stepper("Hours", "$hours h", { if (hours > 0) hours-- }, { if (hours < 14) hours++ })
        Spacer(Modifier.height(10.dp))
        Stepper("Minutes", "$mins min", { mins = (mins - 15 + 60) % 60 }, { mins = (mins + 15) % 60 })

        Spacer(Modifier.height(24.dp))
        Text("Sleep quality", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { i ->
                Text(
                    if (i <= quality) "★" else "☆",
                    fontSize = 38.sp,
                    color = if (i <= quality) FitColors.Accent else FitColors.Outline,
                    modifier = Modifier.clickable { quality = i }.padding(4.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        GradientButton(
            "Save sleep",
            onClick = {
                val day = LocalDate.now().minusDays(if (yesterday) 1 else 0).toString()
                tvm.addSleep(day, total, quality)
                onDone()
            },
            enabled = total > 0
        )
        Spacer(Modifier.height(24.dp))
    }
}
