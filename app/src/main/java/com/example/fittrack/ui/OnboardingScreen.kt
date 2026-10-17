package com.example.fittrack.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fittrack.data.OnboardingAnswers
import com.example.fittrack.data.SuggestedPlan
import com.example.fittrack.data.buildPlan
import com.example.fittrack.data.focusLabel
import com.example.fittrack.ui.theme.FitColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer

private enum class QType { TEXT, CHOICE, NUMBER, DECIMAL }

private data class Question(
    val key: String,
    val coach: String,
    val title: String,
    val type: QType,
    val options: List<String> = emptyList(),
    val unit: String = "",
    val min: Float = 0f,
    val max: Float = 0f
)

/** 12 questions: enough to build a useful plan, short enough not to be annoying. */
private val Questions = listOf(
    Question("name", "Hi, I'm Coach Fit! What should I call you?", "Your name", QType.TEXT),
    Question("goal", "Nice to meet you! What's your main goal right now?", "Main goal", QType.CHOICE, listOf("Lose fat", "Build muscle", "Stay fit")),
    Question("level", "How much training experience do you have?", "Experience", QType.CHOICE, listOf("I'm just starting", "I train regularly")),
    Question("days", "How many days a week can you train?", "Days per week", QType.CHOICE, listOf("3 days", "4 days", "5 days")),
    Question("equipment", "What do you have to train with?", "Equipment", QType.CHOICE, listOf("No equipment", "Dumbbells or gym")),
    Question("limit", "Is there an area I should go easy on?", "Limitations", QType.CHOICE, listOf("None", "Knees / legs", "Lower back", "Shoulders")),
    Question("age", "How old are you?", "Age", QType.NUMBER, unit = "years", min = 13f, max = 90f),
    Question("sex", "I use this only to estimate your calories. What should I use?", "Sex", QType.CHOICE, listOf("Male", "Female", "Prefer not to say")),
    Question("height", "How tall are you?", "Height", QType.NUMBER, unit = "cm", min = 120f, max = 230f),
    Question("weight", "And your current weight?", "Weight", QType.DECIMAL, unit = "kg", min = 30f, max = 250f),
    Question("activity", "How active is a normal day for you?", "Daily activity", QType.CHOICE, listOf("Mostly sitting", "Lightly active", "Very active")),
    Question("sleep", "Last one! How much do you usually sleep?", "Sleep", QType.CHOICE, listOf("Less than 6 h", "6–7 h", "7–8 h", "8 h or more"))
)

private fun isValid(q: Question, v: String): Boolean = when (q.type) {
    QType.TEXT, QType.CHOICE -> v.isNotBlank()
    QType.NUMBER -> v.toIntOrNull()?.let { it >= q.min && it <= q.max } == true
    QType.DECIMAL -> v.toFloatOrNull()?.let { it >= q.min && it <= q.max } == true
}

private fun toAnswers(m: Map<String, String>) = OnboardingAnswers(
    name = m["name"].orEmpty().trim(),
    goal = m["goal"] ?: "Stay fit",
    level = if (m["level"] == "I train regularly") "Intermediate" else "Beginner",
    days = m["days"]?.substringBefore(" ")?.toIntOrNull() ?: 3,
    equipment = m["equipment"] ?: "Dumbbells or gym",
    limit = m["limit"] ?: "None",
    age = m["age"]?.toIntOrNull() ?: 25,
    sex = m["sex"] ?: "Prefer not to say",
    heightCm = m["height"]?.toIntOrNull() ?: 170,
    weightKg = m["weight"]?.toFloatOrNull() ?: 70f,
    activity = m["activity"] ?: "Lightly active",
    sleepNow = m["sleep"] ?: "7–8 h"
)

@Composable
fun OnboardingScreen(
    initialName: String,
    onFinish: (OnboardingAnswers, SuggestedPlan) -> Unit,
    onSkip: () -> Unit
) {
    val total = Questions.size
    var step by rememberSaveable { mutableStateOf(0) }
    var answers by rememberSaveable {
        mutableStateOf(HashMap<String, String>().apply { if (initialName.isNotBlank()) put("name", initialName) })
    }
    var talking by remember { mutableStateOf(false) }
    val keyboardUp = rememberImeVisible()
    val barProgress by animateFloatAsState((step.coerceAtMost(total - 1) + 1f) / total, tween(550, easing = FastOutSlowInEasing), label = "quizProgress")
    val coachH by animateDpAsState(if (keyboardUp) 80.dp else 170.dp, tween(300), label = "coachH")

    // the coach reacts every time the question changes: slides in from the side it came from, squashes, then jumps
    val coachPulse = remember { Animatable(1f) }
    val coachNudge = remember { Animatable(0f) }
    var prevStep by remember { mutableStateOf(step) }
    LaunchedEffect(step) {
        if (step != prevStep) {
            val dir = if (step > prevStep) 1f else -1f
            prevStep = step
            coachNudge.snapTo(40f * dir)
            launch { coachNudge.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 240f)) }
            coachPulse.animateTo(1.12f, tween(110))
            coachPulse.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 300f))
        }
    }

    fun setAnswer(key: String, value: String) { answers = HashMap(answers).apply { put(key, value) } }

    // the coach "talks" for a moment each time a new question appears
    LaunchedEffect(step) {
        talking = true
        delay(1500)
        talking = false
    }

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(horizontal = 20.dp)
    ) {
        when {
            step < total -> {
                val q = Questions[step]
                val value = answers[q.key].orEmpty()
                val valid = isValid(q, value)

                Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (step > 0) {
                        Box(
                            Modifier.size(44.dp).bounceClick { step -= 1 }.clip(CircleShape).background(FitColors.SurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FitColors.TextHigh) }
                    } else Spacer(Modifier.size(44.dp))
                    Spacer(Modifier.weight(1f))
                    SmallPill("Skip for now", onSkip)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { barProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = FitColors.Accent, trackColor = FitColors.SurfaceHigh
                )
                Spacer(Modifier.height(6.dp))
                AnimatedContent(
                    targetState = step + 1,
                    transitionSpec = { (slideInVertically(tween(300)) { it } + fadeIn(tween(250))) togetherWith (slideOutVertically(tween(250)) { -it } + fadeOut(tween(150))) },
                    label = "qCounter"
                ) { n -> Text("Question $n of $total", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid) }

                Coach3D(
                    Modifier.fillMaxWidth().height(coachH)
                        .graphicsLayer { translationX = coachNudge.value; scaleX = coachPulse.value; scaleY = coachPulse.value }
                        .popIn(),
                    talking = talking,
                    cheerKey = step
                )
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInVertically(tween(350, delayMillis = 120)) { it / 3 } + fadeIn(tween(300, delayMillis = 120))) togetherWith fadeOut(tween(120))
                    },
                    label = "coachBubble"
                ) { s ->
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(FitColors.Surface).padding(16.dp)) {
                        Text(Questions[s].coach, style = MaterialTheme.typography.bodyLarge, color = FitColors.TextHigh)
                    }
                }

                AnimatedContent(
                    targetState = step,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = {
                        if (targetState > initialState)
                            (slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut(tween(180)))
                        else
                            (slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { -it / 2 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 3 } + fadeOut(tween(180)))
                    },
                    label = "quizStep"
                ) { s ->
                val q = Questions[s]
                val value = answers[q.key].orEmpty()
                val valid = isValid(q, value)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
                    Text(q.title.uppercase(), style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid)
                    Spacer(Modifier.height(10.dp))
                    when (q.type) {
                        QType.CHOICE -> q.options.forEachIndexed { idx, opt ->
                            ChoiceRow(opt, opt == value, 80 + idx * 70) { setAnswer(q.key, opt) }
                            Spacer(Modifier.height(8.dp))
                        }
                        QType.TEXT -> FitTextField(value, { setAnswer(q.key, it.take(24)) }, "Name", Modifier.fadeSlideIn(80))
                        QType.NUMBER, QType.DECIMAL -> {
                            val decimal = q.type == QType.DECIMAL
                            FitTextField(
                                value,
                                { t -> setAnswer(q.key, if (decimal) t.filter { c -> c.isDigit() || c == '.' }.take(6) else t.filter(Char::isDigit).take(3)) },
                                "${q.title} (${q.unit})",
                                Modifier.fadeSlideIn(80),
                                keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number)
                            )
                            AnimatedVisibility(
                                visible = value.isNotBlank() && !valid,
                                enter = fadeIn(tween(200)) + androidx.compose.animation.expandVertically(tween(250)),
                                exit = fadeOut(tween(150)) + androidx.compose.animation.shrinkVertically(tween(200))
                            ) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Please enter a value between ${q.min.toInt()} and ${q.max.toInt()} ${q.unit}.",
                                        style = MaterialTheme.typography.labelMedium, color = FitColors.Danger
                                    )
                                }
                            }
                        }
                    }
                }
                }
                GradientButton(
                    if (step == total - 1) "Build my plan" else "Next",
                    onClick = { step += 1 },
                    enabled = valid
                )
                Spacer(Modifier.height(16.dp))
            }

            step == total -> {
                LaunchedEffect(Unit) { delay(1900); step = total + 1 }
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Coach3D(Modifier.fillMaxWidth().height(260.dp).popIn(), talking = true)
                    Spacer(Modifier.height(8.dp))
                    Text("Building your plan…", style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh, modifier = Modifier.fadeSlideIn(150))
                    Spacer(Modifier.height(6.dp))
                    Text("Working out your workouts and daily targets", style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid, modifier = Modifier.fadeSlideIn(280))
                    Spacer(Modifier.height(20.dp))
                    CircularProgressIndicator(color = FitColors.Accent, trackColor = FitColors.SurfaceHigh)
                }
            }

            else -> {
                val a = remember { toAnswers(answers) }
                val plan = remember { buildPlan(a) }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(12.dp))
                    Coach3D(Modifier.fillMaxWidth().height(190.dp).popIn(), cheerKey = 1)
                    Text(
                        if (a.name.isBlank()) "Your plan is ready 🎉" else "${a.name}, your plan is ready 🎉",
                        style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().fadeSlideIn(150)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Built from your answers. You can change everything later in your profile.",
                        style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))
                    SettingsGroup("Your program", 300) {
                        SettingsRow("Goal", value = plan.program.goal)
                        RowDivider()
                        SettingsRow("Level", value = plan.program.level)
                        RowDivider()
                        SettingsRow("Training", value = "${plan.program.days} days a week")
                        RowDivider()
                        SettingsRow("Equipment", value = plan.program.equipment)
                        RowDivider()
                        SettingsRow("Go easy on", value = plan.program.limit)
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsGroup("Your week", 450) {
                        (1..7).forEach { dow ->
                            if (dow > 1) RowDivider()
                            SettingsRow(DayNames[dow - 1], value = focusLabel(plan.program, dow) ?: "Rest")
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsGroup("Daily targets", 600) {
                        SettingsRow("Calories", value = "%,d kcal".format(plan.calories))
                        RowDivider()
                        SettingsRow("Protein", value = "${plan.proteinG} g")
                        RowDivider()
                        SettingsRow("Water", value = "%,d ml".format(plan.waterMl))
                        RowDivider()
                        SettingsRow("Steps", value = "%,d".format(plan.steps))
                        RowDivider()
                        SettingsRow("Sleep", value = "${plan.sleepMin / 60} h")
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsGroup("Good to know", 750) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            plan.notes.forEach { n ->
                                Text("•  $n", style = MaterialTheme.typography.bodyMedium, color = FitColors.TextHigh)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Box(Modifier.fadeSlideIn(900)) { GradientButton("Start my journey", onClick = { onFinish(a, plan) }) }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChoiceRow(text: String, selected: Boolean, delayMs: Int, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) FitColors.Accent.copy(alpha = 0.16f) else FitColors.Surface, tween(250), label = "choiceBg")
    val border by animateColorAsState(if (selected) FitColors.Accent else FitColors.Surface, tween(250), label = "choiceBorder")
    val textColor by animateColorAsState(if (selected) FitColors.AccentLight else FitColors.TextHigh, tween(250), label = "choiceText")
    Row(
        Modifier.fillMaxWidth()
            .fadeSlideIn(delayMs)
            .bounceClick(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(BorderStroke(1.5.dp, border), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text, style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(spring(dampingRatio = 0.45f, stiffness = 400f)) + fadeIn(tween(150)),
            exit = fadeOut(tween(120))
        ) { Text("✓", color = FitColors.Accent, fontWeight = FontWeight.Bold) }
    }
}
