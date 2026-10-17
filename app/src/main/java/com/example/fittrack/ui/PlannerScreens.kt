package com.example.fittrack.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.data.PlannedWorkout
import com.example.fittrack.data.Workout
import com.example.fittrack.data.ExerciseLibrary
import com.example.fittrack.data.dateOf
import com.example.fittrack.data.workoutFor
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import java.time.DayOfWeek
import java.time.LocalDate

private fun weekDate(dow: Int): LocalDate = LocalDate.now().with(DayOfWeek.MONDAY).plusDays((dow - 1).toLong())

private fun isDone(p: PlannedWorkout, workouts: List<Workout>): Boolean {
    val d = weekDate(p.dayOfWeek)
    return workouts.any { it.type == p.type && dateOf(it.date) == d }
}

@Composable
fun PlannerScreen(
    tvm: TrackerViewModel,
    onAdd: (Int) -> Unit,
    onTimer: () -> Unit,
    onStartWorkout: () -> Unit,
    onProgram: () -> Unit,
    onLibrary: () -> Unit
) {
    val plans by tvm.plans.collectAsState()
    val workouts by tvm.workouts.collectAsState()
    val program by tvm.program.collectAsState()
    val todayWorkout = remember(program) { workoutFor(program, LocalDate.now()) }
    val todayDow = LocalDate.now().dayOfWeek.value
    var selected by remember { mutableStateOf(todayDow) }

    val doneCount = plans.count { isDone(it, workouts) }
    val dayPlans = plans.filter { it.dayOfWeek == selected }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = LocalBarInset.current + 8.dp)) {
            item { ScreenHeader("Workout planner") }
            // today's guided workout, generated from your program
            item {
                HeroCard {
                    Column(Modifier.weight(1f)) {
                        Text("TODAY'S WORKOUT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        if (todayWorkout != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EmojiIcon(todayWorkout.emoji, 40.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(todayWorkout.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            }
                            Text(
                                "${todayWorkout.exercises.size} exercises · ${todayWorkout.totalSets} sets · ~${todayWorkout.estMinutes} min",
                                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Start workout", color = Color.Black, style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(FitColors.Accent)
                                    .clickable(onClick = onStartWorkout).padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FtIcon(R.drawable.restday, 40.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Rest day", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            }
                            Text(
                                "Recovery is when you get stronger. Stretch lightly or go for a walk.",
                                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FitCard(Modifier.weight(1f).bounceClick(onClick = onProgram)) {
                        Column(Modifier.padding(14.dp)) {
                            FtIcon(R.drawable.exercises, 36.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(program.goal, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Text("${program.level} · ${program.days} days/wk", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                    }
                    FitCard(Modifier.weight(1f).bounceClick(onClick = onLibrary)) {
                        Column(Modifier.padding(14.dp)) {
                            FtIcon(R.drawable.exercises, 36.dp)
                            Spacer(Modifier.height(8.dp))
                            Text("Exercises", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Text("${ExerciseLibrary.size} moves", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                    }
                }
            }
            item { SectionTitle("My plan", modifier = Modifier.padding(top = 6.dp)) }
            // weekly progress
            item {
                HeroCard {
                    Column(Modifier.weight(1f)) {
                        Text("THIS WEEK", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (plans.isEmpty()) "No plan yet" else "$doneCount of ${plans.size} done",
                            style = MaterialTheme.typography.headlineMedium, color = Color.White
                        )
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { if (plans.isEmpty()) 0f else doneCount.toFloat() / plans.size },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = FitColors.Green,
                            trackColor = FitColors.Green.copy(alpha = 0.22f)
                        )
                    }
                }
            }
            // day selector
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { dow ->
                        val sel = dow == selected
                        val hasPlan = plans.any { it.dayOfWeek == dow }
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (sel) FitColors.Accent.copy(alpha = 0.18f) else FitColors.Surface)
                                .border(
                                    BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) FitColors.Accent else FitColors.Outline),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selected = dow }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(DayNames[dow - 1], style = MaterialTheme.typography.labelSmall,
                                color = if (sel) FitColors.AccentLight else FitColors.TextMid, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Text("${weekDate(dow).dayOfMonth}", style = MaterialTheme.typography.titleMedium,
                                color = if (dow == todayDow) FitColors.Accent else FitColors.TextHigh)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.size(6.dp).clip(CircleShape)
                                    .background(if (hasPlan) FitColors.Accent else Color.Transparent)
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(
                    if (selected == todayDow) "Today" else DayNames[selected - 1],
                    "${dayPlans.size} planned", Modifier.padding(top = 4.dp)
                )
            }
            if (dayPlans.isEmpty()) item { EmptyState("😴", "Rest day", "Nothing planned. Add a workout or enjoy the recovery.") }
            items(dayPlans, key = { it.id }) { p ->
                val done = isDone(p, workouts)
                FitCard {
                    Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiIcon(emojiFor(p.type), 48.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.type, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Text(
                                "${p.durationMin} min" + if (p.note.isNotBlank()) " · ${p.note}" else "",
                                style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                            )
                        }
                        when {
                            done -> Text("✓ Done", color = FitColors.Success, style = MaterialTheme.typography.labelLarge)
                            selected == todayDow -> Text(
                                "Mark done", color = Color.Black, style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(AccentBrush)
                                    .clickable { tvm.completePlan(p) }.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                        IconButton({ tvm.deletePlan(p) }) { FtIcon(R.drawable.delete, 30.dp, contentDescription = "Delete") }
                    }
                }
            }
            item { GradientButton("Add to ${DayNames[selected - 1]}", onClick = { onAdd(selected) }) }
            item {
                FitCard(Modifier.bounceClick(onClick = onTimer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        FtIcon(R.drawable.rest_timer, 40.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Rest timer", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Text("Time your rest between sets", style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
                        }
                        Text("›", fontSize = 26.sp, color = FitColors.TextMid)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AddPlanScreen(initialDow: Int, tvm: TrackerViewModel, onDone: () -> Unit) {
    var dow by remember { mutableStateOf(initialDow.coerceIn(1, 7)) }
    var type by remember { mutableStateOf(ActivityTypes[0]) }
    var minutes by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val valid = (minutes.toIntOrNull() ?: 0) > 0

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader("Plan a workout", onBack = onDone)
        Spacer(Modifier.height(24.dp))
        Text("Day", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(DayNames, DayNames[dow - 1]) { dow = DayNames.indexOf(it) + 1 }

        Spacer(Modifier.height(24.dp))
        Text("Activity type", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        ActivityTypeGrid(ActivityTypes, type) { type = it }

        Spacer(Modifier.height(24.dp))
        Text("Details", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        FitTextField(
            minutes, { minutes = it.filter(Char::isDigit) }, "Duration (minutes)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(20, 30, 45, 60).forEach { m -> SelectPill("$m min", minutes == m.toString(), { minutes = m.toString() }) }
        }
        Spacer(Modifier.height(14.dp))
        FitTextField(note, { note = it }, "Note (optional) — e.g. Leg day")

        Spacer(Modifier.height(32.dp))
        GradientButton(
            "Add to plan",
            onClick = { tvm.addPlan(dow, type, minutes.toInt(), note); onDone() },
            enabled = valid
        )
        Spacer(Modifier.height(24.dp))
    }
}
