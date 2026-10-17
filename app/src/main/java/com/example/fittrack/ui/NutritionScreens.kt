package com.example.fittrack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
private fun mealEmoji(t: String) = when (t) { "Breakfast" -> "🍳"; "Lunch" -> "🥗"; "Dinner" -> "🍽️"; else -> "🍌" }

private data class QuickFood(val name: String, val kcal: Int, val p: Int, val c: Int, val f: Int)
private val QuickFoods = listOf(
    QuickFood("Banana", 105, 1, 27, 0),
    QuickFood("Egg", 78, 6, 1, 5),
    QuickFood("Oatmeal bowl", 150, 5, 27, 3),
    QuickFood("Chicken breast", 165, 31, 0, 4),
    QuickFood("Rice (1 cup)", 205, 4, 45, 0),
    QuickFood("Protein shake", 120, 24, 3, 1),
    QuickFood("Greek yogurt", 100, 17, 6, 1),
    QuickFood("Apple", 95, 0, 25, 0)
)

@Composable
fun NutritionScreen(tvm: TrackerViewModel, onAddMeal: (String) -> Unit) {
    val meals by tvm.meals.collectAsState()
    val goals by tvm.goals.collectAsState()
    var offset by remember { mutableStateOf(0) }
    var editGoal by remember { mutableStateOf(false) }

    val date = LocalDate.now().minusDays(offset.toLong())
    val day = date.toString()
    val dayMeals = meals.filter { it.day == day }
    val kcal = dayMeals.sumOf { it.calories }
    val protein = dayMeals.sumOf { it.protein }
    val carbs = dayMeals.sumOf { it.carbs }
    val fat = dayMeals.sumOf { it.fat }
    val nf = NumberFormat.getInstance()
    val progress by animateFloatAsState((kcal.toFloat() / goals.calories).coerceIn(0f, 1f), tween(700), label = "kcal")
    val label = when (offset) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
    }

    if (editGoal) NumberDialog("Daily calorie goal", goals.calories, { editGoal = false }) { tvm.setCalorieGoal(it) }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = LocalBarInset.current + 8.dp)) {
            item { ScreenHeader("Nutrition") { SmallPill("Goal ${nf.format(goals.calories)}", { editGoal = true }) } }
            // day switcher
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("‹", fontSize = 30.sp, color = FitColors.TextHigh,
                        modifier = Modifier.clip(CircleShape).clickable { offset += 1 }.padding(horizontal = 14.dp))
                    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                        color = FitColors.TextHigh, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("›", fontSize = 30.sp, color = if (offset > 0) FitColors.TextHigh else FitColors.Outline,
                        modifier = Modifier.clip(CircleShape).clickable(enabled = offset > 0) { offset -= 1 }.padding(horizontal = 14.dp))
                }
            }
            // calories hero
            item {
                HeroCard {
                    Column(Modifier.weight(1f)) {
                        Text("CALORIES EATEN", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        Text(nf.format(kcal), fontSize = 46.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        val left = goals.calories - kcal
                        Text(
                            if (left >= 0) "${nf.format(left)} kcal left" else "${nf.format(-left)} kcal over goal",
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    RingProgress(progress, color = FitColors.Green, trackColor = FitColors.Green.copy(alpha = 0.22f)) {
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            // macros (targets: 30% protein / 45% carbs / 25% fat of the calorie goal)
            item {
                FitCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        MacroBar("Protein", protein, (goals.calories * 0.30 / 4).toInt())
                        MacroBar("Carbs", carbs, (goals.calories * 0.45 / 4).toInt())
                        MacroBar("Fat", fat, (goals.calories * 0.25 / 9).toInt())
                    }
                }
            }
            item { GradientButton("Log a meal", onClick = { onAddMeal(day) }) }

            if (dayMeals.isEmpty()) item { EmptyState("🍎", "Nothing logged", "Log what you eat to see calories and macros here.") }

            MealTypes.forEach { type ->
                val items = dayMeals.filter { it.mealType == type }
                if (items.isNotEmpty()) {
                    item(key = "hdr_$type") {
                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            FtIcon(R.drawable.food, 32.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(type, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Spacer(Modifier.weight(1f))
                            Text("${items.sumOf { it.calories }} kcal", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                    }
                    items.forEach { m ->
                        item(key = m.id) {
                            FitCard {
                                Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(m.name, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                        Text("P ${m.protein}g · C ${m.carbs}g · F ${m.fat}g", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                                    }
                                    Text("${m.calories} kcal", style = MaterialTheme.typography.labelLarge, color = FitColors.AccentLight)
                                    IconButton({ tvm.deleteMeal(m) }) { FtIcon(R.drawable.delete, 30.dp, contentDescription = "Delete") }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MacroBar(label: String, value: Int, target: Int) {
    val p by animateFloatAsState((value.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f), tween(700), label = label)
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = FitColors.TextHigh)
            Spacer(Modifier.weight(1f))
            Text("$value / $target g", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { p },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = FitColors.Accent,
            trackColor = FitColors.Outline
        )
    }
}

@Composable
fun AddMealScreen(day: String, tvm: TrackerViewModel, onDone: () -> Unit) {
    var type by remember { mutableStateOf("Breakfast") }
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && (kcal.toIntOrNull() ?: 0) > 0
    val num = KeyboardOptions(keyboardType = KeyboardType.Number)

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader("Log meal", onBack = onDone)
        Spacer(Modifier.height(24.dp))
        Text("Meal", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        PillRow(MealTypes, type) { type = it }

        Spacer(Modifier.height(24.dp))
        Text("Quick add", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickFoods.forEach { f ->
                SelectPill(f.name, name == f.name, {
                    name = f.name; kcal = f.kcal.toString(); protein = f.p.toString(); carbs = f.c.toString(); fat = f.f.toString()
                })
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Details", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        FitTextField(name, { name = it }, "What did you eat?")
        Spacer(Modifier.height(12.dp))
        FitTextField(kcal, { kcal = it.filter(Char::isDigit) }, "Calories (kcal)", keyboardOptions = num)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FitTextField(protein, { protein = it.filter(Char::isDigit) }, "Protein g", Modifier.weight(1f), keyboardOptions = num)
            FitTextField(carbs, { carbs = it.filter(Char::isDigit) }, "Carbs g", Modifier.weight(1f), keyboardOptions = num)
            FitTextField(fat, { fat = it.filter(Char::isDigit) }, "Fat g", Modifier.weight(1f), keyboardOptions = num)
        }

        Spacer(Modifier.height(32.dp))
        GradientButton(
            "Save meal",
            onClick = {
                tvm.addMeal(day, type, name, kcal.toInt(), protein.toIntOrNull() ?: 0, carbs.toIntOrNull() ?: 0, fat.toIntOrNull() ?: 0)
                onDone()
            },
            enabled = valid
        )
        Spacer(Modifier.height(24.dp))
    }
}
