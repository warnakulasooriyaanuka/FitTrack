package com.example.fittrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitDisplay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun kg(v: Float) = String.format(Locale.US, "%.1f", v)

@Composable
fun BodyScreen(tvm: TrackerViewModel, onBack: () -> Unit) {
    val body by tvm.body.collectAsState() // newest first
    var logging by remember { mutableStateOf(false) }
    val latest = body.firstOrNull()
    val first = body.lastOrNull()
    val change = if (body.size >= 2 && latest != null && first != null) latest.weightKg - first.weightKg else null

    if (logging) {
        DecimalDialog("Log today's weight", latest?.let { kg(it.weightKg) } ?: "", "Weight (kg)", { logging = false }) { tvm.addWeight(it) }
    }

    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()) {
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Body weight", onBack = onBack) }
            item {
                HeroCard(Modifier.padding(top = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("CURRENT WEIGHT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (latest != null) "${kg(latest.weightKg)} kg" else "—",
                            fontSize = 46.sp, fontFamily = FitDisplay, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        Text(
                            when {
                                change == null -> "Log your weight to start tracking"
                                change == 0f -> "No change since your first entry"
                                else -> (if (change > 0) "+" else "−") + kg(kotlin.math.abs(change)) + " kg since your first entry"
                            },
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            item { GradientButton("Log today's weight", onClick = { logging = true }) }
            item { SectionTitle("Trend", "Last ${minOf(body.size, 10)} entries", Modifier.padding(top = 6.dp)) }
            item {
                FitCard {
                    Box(Modifier.padding(16.dp)) { LineChart(body.take(10).reversed().map { it.weightKg }) }
                }
            }
            item { SectionTitle("History", modifier = Modifier.padding(top = 6.dp)) }
            if (body.isEmpty()) item { EmptyState("⚖️", "No entries yet", "Tap “Log today's weight” to add your first.") }
            items(body.take(60), key = { it.id }) { b ->
                val label = runCatching { LocalDate.parse(b.day).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())) }.getOrDefault(b.day)
                FitCard {
                    Row(Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${kg(b.weightKg)} kg", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                            Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                        }
                        IconButton({ tvm.deleteBody(b) }) { FtIcon(R.drawable.delete, 30.dp, contentDescription = "Delete") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
