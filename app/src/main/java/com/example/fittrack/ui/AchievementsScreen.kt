package com.example.fittrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import java.text.NumberFormat

@Composable
fun AchievementsScreen(tvm: TrackerViewModel, onBack: () -> Unit) {
    val ins by tvm.insights.collectAsState()
    val nf = NumberFormat.getInstance()
    val sorted = ins.achievements.sortedByDescending { it.unlocked }
    val unlocked = ins.achievements.count { it.unlocked }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Streaks & achievements", onBack = onBack) }
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(24.dp)).background(FitColors.Surface).padding(22.dp)
                ) {
                    Text("CURRENT STREAK", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        FtIcon(R.drawable.daily_streak, 52.dp, Modifier.padding(bottom = 4.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("${ins.streak}", fontSize = 46.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (ins.streak == 1) "day" else "days", style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        "Best ${ins.bestStreak} · log a workout or hit your step goal to keep it alive",
                        style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ins.weekActive.forEachIndexed { i, on ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(34.dp).clip(CircleShape)
                                        .background(if (on) FitColors.Accent else FitColors.SurfaceHigh),
                                    contentAlignment = Alignment.Center
                                ) { if (on) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    ins.weekLabels.getOrElse(i) { "" }.take(3),
                                    style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Workouts", "${ins.totalWorkouts}", Modifier.weight(1f))
                    MiniStat("Minutes", nf.format(ins.totalMinutes), Modifier.weight(1f))
                    MiniStat("Burned", nf.format(ins.totalBurned), Modifier.weight(1f))
                }
            }
            item { SectionTitle("Achievements", "$unlocked / ${ins.achievements.size} unlocked", Modifier.padding(top = 6.dp)) }
            items(sorted, key = { it.id }) { a ->
                FitCard {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiIcon(a.emoji, 52.dp, alpha = if (a.unlocked) 1f else 0.35f)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(a.title, style = MaterialTheme.typography.titleMedium,
                                    color = if (a.unlocked) FitColors.TextHigh else FitColors.TextMid, modifier = Modifier.weight(1f))
                                if (a.unlocked) Text("✓ Unlocked", style = MaterialTheme.typography.labelMedium, color = FitColors.Success)
                            }
                            Text(a.desc, style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
                            if (!a.unlocked) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (a.progress.toFloat() / a.target).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = FitColors.Accent, trackColor = FitColors.Outline
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${nf.format(a.progress)} / ${nf.format(a.target)}", style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid)
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
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    FitCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
            Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        }
    }
}
