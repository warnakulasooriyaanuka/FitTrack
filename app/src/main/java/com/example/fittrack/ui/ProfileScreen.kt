package com.example.fittrack.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fittrack.reminders.ReminderScheduler
import com.example.fittrack.reminders.ReminderSettings
import com.example.fittrack.reminders.ReminderStore
import com.example.fittrack.ui.theme.FitColors

@Composable
fun ProfileScreen(
    email: String,
    tvm: TrackerViewModel,
    onBack: () -> Unit,
    onProgram: () -> Unit,
    onBody: () -> Unit,
    onLibrary: () -> Unit,
    onAchievements: () -> Unit,
    onRedoQuiz: () -> Unit,
    onLogout: () -> Unit
) {
    val ctx = LocalContext.current
    val goals by tvm.goals.collectAsState()
    val ins by tvm.insights.collectAsState()
    val program by tvm.program.collectAsState()
    val name by tvm.displayName.collectAsState()
    val avatar by tvm.avatarPath.collectAsState()
    val shownName = name.ifBlank { email.substringBefore("@").ifBlank { "Athlete" } }
    val initial = shownName.firstOrNull()?.uppercase() ?: "F"

    var dialog by remember { mutableStateOf<String?>(null) }
    var rs by remember { mutableStateOf(ReminderStore.load(ctx)) }
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Android's photo picker: no storage permission needed
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) tvm.setAvatarFromUri(uri)
    }
    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifGranted = it }

    fun update(new: ReminderSettings) {
        val turningOn = (new.waterOn && !rs.waterOn) || (new.sleepOn && !rs.sleepOn) || (new.workoutOn && !rs.workoutOn)
        if (turningOn && !notifGranted && Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        rs = new
        ReminderStore.save(ctx, new)
        ReminderScheduler.rescheduleAll(ctx)
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = FitColors.Success,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = FitColors.SurfaceHigh,
        uncheckedBorderColor = FitColors.SurfaceHigh
    )

    when (dialog) {
        "name" -> TextDialog("Display name", name, "Name", { dialog = null }) { tvm.setDisplayName(it) }
        "steps" -> NumberDialog("Daily step goal", goals.steps, { dialog = null }) { tvm.setStepGoal(it) }
        "cal" -> NumberDialog("Daily calorie goal", goals.calories, { dialog = null }) { tvm.setCalorieGoal(it) }
        "water" -> NumberDialog("Daily water goal (ml)", goals.waterMl, { dialog = null }) { tvm.setWaterGoal(it) }
        "sleep" -> NumberDialog("Sleep goal (hours)", goals.sleepMin / 60, { dialog = null }) { tvm.setSleepGoalMin(it * 60) }
        "logout" -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = FitColors.SurfaceHigh,
            title = { Text("Log out?", color = FitColors.TextHigh) },
            text = { Text("Reminders will stop until you log in again. Your data stays safe in your account.", color = FitColors.TextMid) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    ReminderScheduler.cancelAll(ctx)
                    ctx.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE).edit().remove("last_uid").apply()
                    onLogout()
                }) { Text("Log out", color = FitColors.Danger) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel", color = FitColors.Accent) } }
        )
    }

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        ScreenHeader("Profile", onBack = onBack)
        Spacer(Modifier.height(20.dp))

        // identity
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AvatarCircle(initial, size = 96.dp, imagePath = avatar, onClick = { pickPhoto() })
            Spacer(Modifier.height(12.dp))
            Text(shownName, style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
            Text(email, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallPill(if (avatar.isBlank()) "Add photo" else "Change photo", { pickPhoto() })
                SmallPill("Edit name", { dialog = "name" })
                if (avatar.isNotBlank()) SmallPill("Remove photo", { tvm.clearAvatar() })
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStat("Workouts", "${ins.totalWorkouts}", FitColors.Accent, Modifier.weight(1f))
            ProfileStat("Day streak", "${ins.streak}", FitColors.Green, Modifier.weight(1f))
            ProfileStat("Awards", "${ins.achievements.count { it.unlocked }}", FitColors.Cyan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("Daily goals") {
            SettingsRow("Steps", value = "%,d".format(goals.steps), chevron = true, onClick = { dialog = "steps" })
            RowDivider()
            SettingsRow("Calories eaten", value = "%,d kcal".format(goals.calories), chevron = true, onClick = { dialog = "cal" })
            RowDivider()
            SettingsRow("Water", value = "%,d ml".format(goals.waterMl), chevron = true, onClick = { dialog = "water" })
            RowDivider()
            SettingsRow("Sleep", value = "${goals.sleepMin / 60} h", chevron = true, onClick = { dialog = "sleep" })
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("Training") {
            SettingsRow("My program", value = program.goal, chevron = true, onClick = onProgram)
            RowDivider()
            SettingsRow("Body weight", chevron = true, onClick = onBody)
            RowDivider()
            SettingsRow("Exercise library", chevron = true, onClick = onLibrary)
            RowDivider()
            SettingsRow("Redo setup quiz", chevron = true, onClick = onRedoQuiz)
            RowDivider()
            SettingsRow("Streaks & achievements", chevron = true, onClick = onAchievements)
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("Reminders") {
            SettingsRow("Drink water", trailing = {
                Switch(rs.waterOn, { update(rs.copy(waterOn = it)) }, colors = switchColors)
            })
            if (rs.waterOn) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)) {
                    Text("Remind me every", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                    Spacer(Modifier.height(8.dp))
                    PillRow(listOf("1 h", "2 h", "3 h", "4 h"), "${rs.waterEveryH} h") {
                        update(rs.copy(waterEveryH = it.substringBefore(" ").toIntOrNull() ?: 2))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Between 08:00 and 21:00. Skipped once you've reached your water goal.",
                        style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
                    )
                }
            }
            RowDivider()
            SettingsRow("Bedtime reminder", trailing = {
                Switch(rs.sleepOn, { update(rs.copy(sleepOn = it)) }, colors = switchColors)
            })
            if (rs.sleepOn) {
                SettingsRow("Remind me at", trailing = {
                    TimeStepper(rs.sleepH, rs.sleepM) { h, m -> update(rs.copy(sleepH = h, sleepM = m)) }
                })
            }
            RowDivider()
            SettingsRow("Workout reminder", trailing = {
                Switch(rs.workoutOn, { update(rs.copy(workoutOn = it)) }, colors = switchColors)
            })
            if (rs.workoutOn) {
                SettingsRow("Remind me at", trailing = {
                    TimeStepper(rs.workoutH, rs.workoutM) { h, m -> update(rs.copy(workoutH = h, workoutM = m)) }
                })
                Text(
                    "Only on your training days.",
                    style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                )
            }
        }
        if (!notifGranted && (rs.waterOn || rs.sleepOn || rs.workoutOn)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Notifications are blocked for FitTrack, so reminders can't show.",
                    style = MaterialTheme.typography.labelMedium, color = FitColors.Warning, modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                SmallPill("Open settings", {
                    ctx.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                })
            }
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("Account") {
            SettingsRow("Display name", value = shownName, chevron = true, onClick = { dialog = "name" })
            RowDivider()
            SettingsRow("Email", value = email)
        }

        Spacer(Modifier.height(24.dp))
        FitCard(Modifier.bounceClick { dialog = "logout" }) {
            Text(
                "Log out", color = FitColors.Danger, style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "FitTrack 1.0", style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    FitCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        }
    }
}
