package com.example.fittrack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.data.*
import com.example.fittrack.ui.theme.FitColors
import java.time.LocalDate

@Composable
fun ExerciseLibraryScreen(onBack: () -> Unit) {
    var muscle by remember { mutableStateOf("All") }
    var open by remember { mutableStateOf<String?>(null) }
    val list = if (muscle == "All") ExerciseLibrary else ExerciseLibrary.filter { it.muscle == muscle }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ScreenHeader("Exercise library", onBack = onBack) }
            item {
                Column(Modifier.padding(top = 8.dp)) {
                    Text(
                        "${ExerciseLibrary.size} moves you can do at home with bodyweight or a pair of dumbbells.",
                        style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                    )
                    Spacer(Modifier.height(12.dp))
                    PillRow(listOf("All") + Muscles, muscle) { muscle = it; open = null }
                }
            }
            items(list, key = { it.id }) { ex ->
                val expanded = open == ex.id
                FitCard(Modifier.bounceClick { open = if (expanded) null else ex.id }) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiIcon(emojiForMuscle(ex.muscle), 46.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ex.name, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                Text(
                                    "${ex.muscle} · ${ex.equipment}" + if (ex.timed) " · timed" else "",
                                    style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
                                )
                            }
                            Text(if (expanded) "−" else "+", fontSize = 22.sp, color = FitColors.AccentLight)
                        }
                        if (expanded) {
                            Spacer(Modifier.height(12.dp))
                            Text(ex.tip, style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ProgramScreen(tvm: TrackerViewModel, onBack: () -> Unit) {
    val program by tvm.program.collectAsState()
    val todayDow = LocalDate.now().dayOfWeek.value
    val blurb = when (program.goal) {
        "Lose fat" -> "Higher reps, short rests and a cardio finisher to keep your heart rate up."
        "Build muscle" -> "Heavier-feeling sets with longer rests so your muscles recover between efforts."
        else -> "Balanced reps and rest to stay strong, mobile and consistent."
    }

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader("My program", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Text("Goal", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(ProgramGoals, program.goal) { tvm.setProgram(program.copy(goal = it)) }
        Spacer(Modifier.height(8.dp))
        Text(blurb, style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)

        Spacer(Modifier.height(24.dp))
        Text("Level", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(ProgramLevels, program.level) { tvm.setProgram(program.copy(level = it)) }
        Spacer(Modifier.height(8.dp))
        Text(
            if (program.level == "Beginner") "3 sets per exercise, 5 exercises a session." else "4 sets per exercise, 6 exercises a session.",
            style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
        )

        Spacer(Modifier.height(24.dp))
        Text("Days per week", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(ProgramDays.map { "$it days" }, "${program.days} days") { label ->
            tvm.setProgram(program.copy(days = label.substringBefore(" ").toIntOrNull() ?: 3))
        }

        Spacer(Modifier.height(24.dp))
        Text("Equipment", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(ProgramEquipment, program.equipment) { tvm.setProgram(program.copy(equipment = it)) }

        Spacer(Modifier.height(24.dp))
        Text("Go easy on", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(ProgramLimits, program.limit) { tvm.setProgram(program.copy(limit = it)) }
        Spacer(Modifier.height(8.dp))
        Text(
            "Moves that put extra load on that area are left out. Stop anything that hurts, and see a doctor or physio if pain continues.",
            style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
        )

        Spacer(Modifier.height(28.dp))
        Text("Your week", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        FitCard {
            Column(Modifier.padding(vertical = 6.dp)) {
                (1..7).forEach { dow ->
                    val label = focusLabel(program, dow)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            DayNames[dow - 1], Modifier.width(56.dp), style = MaterialTheme.typography.labelLarge,
                            color = if (dow == todayDow) FitColors.Accent else FitColors.TextMid
                        )
                        Text(
                            label ?: "Rest", style = MaterialTheme.typography.bodyLarge,
                            color = if (label != null) FitColors.TextHigh else FitColors.TextMid
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
