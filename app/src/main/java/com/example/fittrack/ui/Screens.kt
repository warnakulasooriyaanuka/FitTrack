package com.example.fittrack.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fittrack.R
import com.example.fittrack.data.dateOf
import com.example.fittrack.data.todayKey
import com.example.fittrack.data.workoutFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.fittrack.ui.theme.FitColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/* ───────────────────────────── LOGIN ───────────────────────────── */

@Composable
fun LoginScreen(vm: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    val info by vm.info.collectAsState()
    var lastInfo by remember { mutableStateOf("") }
    if (info != null) lastInfo = info.orEmpty()

    // keep the last error text around so it can fade out instead of vanishing
    var lastError by remember { mutableStateOf("") }
    if (error != null) lastError = error.orEmpty()

    // little shake on the fields whenever a new error appears
    val shake = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(error) {
        if (error != null) {
            repeat(3) {
                shake.animateTo(14f, tween(55))
                shake.animateTo(-14f, tween(55))
            }
            shake.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.5f))
        }
    }

    // soft breathing glow behind the logo
    val glowT = androidx.compose.animation.core.rememberInfiniteTransition(label = "glow")
    val glow by glowT.animateFloat(
        0.25f, 0.6f,
        androidx.compose.animation.core.infiniteRepeatable(tween(2400), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Column(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(190.dp)
                    .graphicsLayer { alpha = glow; scaleX = 0.9f + glow * 0.2f; scaleY = 0.9f + glow * 0.2f }
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(listOf(FitColors.Accent.copy(alpha = 0.35f), Color.Transparent)),
                        CircleShape
                    )
            )
            Image(
                painter = painterResource(R.drawable.fittrack_logo),
                contentDescription = "FitTrack logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(112.dp).popIn().floating()
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("FitTrack", style = MaterialTheme.typography.displaySmall, color = FitColors.TextHigh, modifier = Modifier.fadeSlideIn(160))
        Spacer(Modifier.height(4.dp))
        Text(
            "Track workouts. Stay consistent.",
            style = MaterialTheme.typography.bodyMedium,
            color = FitColors.TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.fadeSlideIn(240)
        )
        Spacer(Modifier.height(36.dp))

        Column(Modifier.graphicsLayer { translationX = shake.value }) {
            FitTextField(
                email, { email = it }, "Email",
                modifier = Modifier.fadeSlideIn(340),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )
            Spacer(Modifier.height(12.dp))
            FitTextField(
                pass, { pass = it }, "Password (min 6 chars)",
                modifier = Modifier.fadeSlideIn(420),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )
            Text(
                "Forgot password?",
                style = MaterialTheme.typography.labelLarge,
                color = FitColors.Accent,
                modifier = Modifier
                    .align(Alignment.End)
                    .fadeSlideIn(470)
                    .bounceClick(enabled = !loading) { vm.resetPassword(email) }
                    .padding(horizontal = 4.dp, vertical = 10.dp)
            )
        }

        AnimatedVisibility(
            visible = info != null,
            enter = fadeIn(tween(250)) + expandVertically(tween(300)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
        ) {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    lastInfo,
                    color = FitColors.Green,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FitColors.Green.copy(alpha = 0.12f))
                        .padding(12.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(tween(250)) + expandVertically(tween(300)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                Text(
                    lastError,
                    color = FitColors.Danger,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FitColors.Danger.copy(alpha = 0.12f))
                        .padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Box(Modifier.fadeSlideIn(520)) { GradientButton("Log in", onClick = { vm.login(email, pass) }, enabled = !loading) }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fadeSlideIn(600)) { GhostButton("Create account", onClick = { vm.register(email, pass) }, enabled = !loading) }

        AnimatedVisibility(visible = loading, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    Modifier.fillMaxWidth().clip(CircleShape),
                    color = FitColors.Accent,
                    trackColor = FitColors.Outline
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

/* ───────────────────────────── HOME (Summary) ───────────────────────────── */

@Composable
fun HomeScreen(
    email: String,
    wvm: WorkoutViewModel,
    tvm: TrackerViewModel,
    onAdd: () -> Unit,
    onProfile: () -> Unit,
    onTimer: () -> Unit,
    onAchievements: () -> Unit,
    onStartWorkout: () -> Unit,
    onBody: () -> Unit
) {
    val ctx = LocalContext.current
    val workouts by wvm.workouts.collectAsState()
    val ins by tvm.insights.collectAsState()
    val goals by tvm.goals.collectAsState()
    val water by tvm.water.collectAsState()
    val body by tvm.body.collectAsState()
    val program by tvm.program.collectAsState()
    val name by tvm.displayName.collectAsState()
    val avatar by tvm.avatarPath.collectAsState()
    var editGoal by remember { mutableStateOf(false) }
    var editWater by remember { mutableStateOf(false) }
    val todayWorkout = remember(program) { workoutFor(program, LocalDate.now()) }

    // Hardware: step counter + runtime permission (counting itself lives in TrackerViewModel)
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) }
    LaunchedEffect(granted) { if (granted) tvm.startStepTracking() }

    val today = LocalDate.now()
    val weekAgo = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
    val week = workouts.filter { it.date >= weekAgo }
    val nf = NumberFormat.getInstance()
    val todayMl = water.firstOrNull { it.day == todayKey() }?.ml ?: 0
    val exerciseMin = workouts.filter { dateOf(it.date) == today }.sumOf { it.durationMin }
    val initial = name.ifBlank { email }.trim().firstOrNull()?.uppercase() ?: "F"

    val stepsP by animateFloatAsState((ins.todaySteps.toFloat() / goals.steps).coerceIn(0f, 2f), tween(800), label = "steps")
    val exerciseP by animateFloatAsState((exerciseMin / 30f).coerceIn(0f, 2f), tween(800), label = "exercise")
    val waterP by animateFloatAsState((todayMl.toFloat() / goals.waterMl).coerceIn(0f, 2f), tween(800), label = "water")

    if (editGoal) NumberDialog("Daily step goal", goals.steps, { editGoal = false }) { tvm.setStepGoal(it) }
    if (editWater) NumberDialog("Daily water goal (ml)", goals.waterMl, { editWater = false }) { tvm.setWaterGoal(it) }

    Box(Modifier.fillMaxSize().screenBackground()) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAdd,
                    modifier = Modifier.padding(bottom = LocalBarInset.current),
                    containerColor = FitColors.Accent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, "Add workout") }
            }
        ) { pad ->
            LazyColumn(
                Modifier.padding(pad).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = LocalBarInset.current + 80.dp)
            ) {
                // large title + avatar, like the iPhone Fitness app
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())).uppercase(),
                                style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid
                            )
                            Text("Summary", style = MaterialTheme.typography.headlineLarge, color = FitColors.TextHigh)
                        }
                        AvatarCircle(initial, size = 40.dp, imagePath = avatar, onClick = onProfile)
                    }
                }
                // Activity rings card
                item {
                    FitCard {
                        Column(Modifier.padding(18.dp)) {
                            Text("Activity", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    RingStat(
                                        "Steps", nf.format(ins.todaySteps), "/${nf.format(goals.steps)}", FitColors.Accent,
                                        Modifier.clickable { if (!granted) launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) else editGoal = true }
                                    )
                                    RingStat("Exercise", "$exerciseMin", "/30 min", FitColors.Green)
                                    RingStat(
                                        "Water", nf.format(todayMl), "/${nf.format(goals.waterMl)} ml", FitColors.Cyan,
                                        Modifier.clickable { editWater = true }
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                ActivityRings(
                                    progress = listOf(stepsP, exerciseP, waterP),
                                    colors = listOf(FitColors.Accent, FitColors.Green, FitColors.Cyan)
                                )
                            }
                            val hint = when {
                                !granted && ins.todaySteps == 0 -> "Tap Steps to allow step tracking."
                                !tvm.hasStepSensor && ins.todaySteps == 0 -> "No step sensor on this device."
                                ins.todaySteps > 0 -> String.format(
                                    Locale.US, "%.1f km · %d kcal from walking · tap Steps to edit your goal",
                                    ins.todaySteps * 0.00076, (ins.todaySteps * 0.04).toInt()
                                )
                                else -> "Tap Steps to edit your goal."
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(hint, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                    }
                }
                // Today's guided workout
                item {
                    FitCard(Modifier.bounceClick(enabled = todayWorkout != null, onClick = onStartWorkout)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            EmojiIcon(todayWorkout?.emoji ?: "😴", 52.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("TODAY'S WORKOUT", style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid)
                                Text(todayWorkout?.title ?: "Rest day", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                Text(
                                    if (todayWorkout != null) "${todayWorkout.exercises.size} exercises · ~${todayWorkout.estMinutes} min" else "Stretch or take a walk",
                                    style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                                )
                            }
                            if (todayWorkout != null) {
                                Text(
                                    "Start", color = Color.Black, style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(FitColors.Accent)
                                        .padding(horizontal = 18.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
                // Water quick log
                item {
                    FitCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FtIcon(R.drawable.water, 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Water", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                    Text(
                                        "${nf.format(todayMl)} of ${nf.format(goals.waterMl)} ml",
                                        style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
                                    )
                                }
                                SmallPill("−", { tvm.addWater(-250) })
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "+250 ml", color = Color.Black, style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(FitColors.Cyan)
                                        .clickable { tvm.addWater(250) }.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { waterP.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = FitColors.Cyan, trackColor = FitColors.SurfaceHigh
                            )
                        }
                    }
                }
                // Streak + achievements + rest timer
                item {
                    val unlocked = ins.achievements.count { it.unlocked }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FitCard(Modifier.weight(1f).bounceClick(onClick = onAchievements)) {
                            Column(Modifier.padding(14.dp)) {
                                FtIcon(R.drawable.daily_streak, 36.dp)
                                Spacer(Modifier.height(8.dp))
                                Text("${ins.streak}", style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
                                Text("day streak", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                            }
                        }
                        FitCard(Modifier.weight(1f).bounceClick(onClick = onAchievements)) {
                            Column(Modifier.padding(14.dp)) {
                                FtIcon(R.drawable.awards, 36.dp)
                                Spacer(Modifier.height(8.dp))
                                Text("$unlocked/${ins.achievements.size}", style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
                                Text("awards", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                            }
                        }
                        FitCard(Modifier.weight(1f).bounceClick(onClick = onTimer)) {
                            Column(Modifier.padding(14.dp)) {
                                FtIcon(R.drawable.rest_timer, 36.dp)
                                Spacer(Modifier.height(8.dp))
                                Text("Rest", style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
                                Text("timer", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                            }
                        }
                    }
                }
                // Weekly steps chart
                item { SectionTitle("Trends", "Steps · last 7 days", Modifier.padding(top = 6.dp)) }
                item {
                    FitCard {
                        Box(Modifier.padding(16.dp)) {
                            BarChart(
                                values = ins.weekSteps.map { it.toFloat() },
                                labels = ins.weekLabels,
                                highlightIndex = ins.weekSteps.size - 1,
                                goal = goals.steps.toFloat()
                            )
                        }
                    }
                }
                // Body weight shortcut
                item {
                    val latest = body.firstOrNull()
                    FitCard(Modifier.bounceClick(onClick = onBody)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            FtIcon(R.drawable.body_weight, 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Body weight", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                Text(
                                    latest?.let { String.format(Locale.US, "%.1f kg", it.weightKg) } ?: "Tap to log your weight",
                                    style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
                                )
                            }
                            Text("›", fontSize = 26.sp, color = FitColors.TextMid)
                        }
                    }
                }
                // Weekly workout stats
                item { SectionTitle("This week", "Last 7 days", Modifier.padding(top = 6.dp)) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Workouts", "${week.size}", "💪", Modifier.weight(1f))
                        StatTile("Minutes", "${week.sumOf { it.durationMin }}", "⏱️", Modifier.weight(1f))
                        StatTile("Calories", "${week.sumOf { it.calories }}", "kcal", Modifier.weight(1f))
                    }
                }
                item { SectionTitle("History", modifier = Modifier.padding(top = 8.dp)) }
                if (workouts.isEmpty()) item {
                    EmptyState("🏋️", "No workouts yet", "Tap + to log your first one.")
                }
                items(workouts, key = { it.id }) { w ->
                    FitCard {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            EmojiIcon(emojiFor(w.type), 48.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(w.type, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                                Text(
                                    "${w.durationMin} min • ${w.calories} kcal",
                                    style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(7.dp).clip(CircleShape)
                                            .background(if (w.synced) FitColors.Success else FitColors.Warning)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(w.date)) +
                                            if (w.synced) " · Synced" else " · Offline",
                                        style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
                                    )
                                }
                            }
                            IconButton({ wvm.delete(w) }) {
                                FtIcon(R.drawable.delete, 30.dp, contentDescription = "Delete")
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    FitCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            EmojiIcon(emoji, 34.dp)
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
            Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        }
    }
}

/* ─────────────────────────── ADD WORKOUT ─────────────────────────── */

@Composable
fun AddWorkoutScreen(wvm: WorkoutViewModel, onDone: () -> Unit) {
    var type by remember { mutableStateOf(ActivityTypes[0]) }
    var minutes by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    val valid = (minutes.toIntOrNull() ?: 0) > 0

    Column(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader("Log workout", onBack = onDone)

        Spacer(Modifier.height(24.dp))
        Text("Activity type", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        ActivityTypeGrid(ActivityTypes, type) { type = it }

        Spacer(Modifier.height(28.dp))
        Text("Details", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        Spacer(Modifier.height(12.dp))
        FitTextField(
            minutes, { minutes = it.filter(Char::isDigit) }, "Duration (minutes)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60).forEach { m ->
                SelectPill("$m min", minutes == m.toString(), { minutes = m.toString() })
            }
        }
        Spacer(Modifier.height(14.dp))
        FitTextField(
            kcal, { kcal = it.filter(Char::isDigit) }, "Calories burned (optional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(32.dp))
        GradientButton(
            "Save workout",
            onClick = { wvm.add(type, minutes.toInt(), kcal.toIntOrNull() ?: 0); onDone() },
            enabled = valid
        )
        Spacer(Modifier.height(24.dp))
    }
}
