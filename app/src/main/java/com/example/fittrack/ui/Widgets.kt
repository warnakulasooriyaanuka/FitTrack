package com.example.fittrack.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.withStyle
import com.example.fittrack.ui.theme.FitDisplay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitColors

val ActivityTypes = listOf("Running", "Cycling", "Gym", "Yoga", "Swimming", "HIIT")
val DayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

fun emojiFor(type: String) = when (type) {
    "Running" -> "🏃"
    "Cycling" -> "🚴"
    "Gym" -> "🏋️"
    "Yoga" -> "🧘"
    "Swimming" -> "🏊"
    "HIIT" -> "⚡"
    else -> "💪"
}

fun fmtDuration(min: Int) = if (min >= 60) "${min / 60}h ${min % 60}m" else "${min}m"

val AccentBrush: Brush = SolidColor(FitColors.Accent)

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(FitColors.Surface)
                    .border(BorderStroke(1.dp, FitColors.Outline), CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FitColors.TextHigh) }
            Spacer(Modifier.width(14.dp))
        }
        Text(
            title,
            style = if (onBack == null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            color = FitColors.TextHigh
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing(this)
        }
    }
}

@Composable
fun SectionTitle(text: String, side: String? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
        if (side != null) {
            Spacer(Modifier.weight(1f))
            Text(side, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
        }
    }
}

/** Small rounded outline pill used for actions like "Log out" or "Goal". */
@Composable
fun SmallPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text, color = FitColors.TextMid, style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(FitColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
fun SelectPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) FitColors.AccentLight else FitColors.TextMid,
        modifier = modifier
            .bounceClick(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(if (selected) FitColors.Accent.copy(alpha = 0.16f) else FitColors.Surface)
            .border(BorderStroke(1.dp, if (selected) FitColors.Accent else FitColors.Outline), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** Horizontally scrollable row of single-select pills. */
@Composable
fun PillRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o -> SelectPill(o, o == selected, { onSelect(o) }) }
    }
}

@Composable
fun HeroCard(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FitColors.Surface).padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun RingProgress(
    progress: Float,
    size: Dp = 84.dp,
    stroke: Dp = 9.dp,
    trackColor: Color = FitColors.Accent.copy(alpha = 0.22f),
    color: Color = FitColors.Accent,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(progress) { anim.animateTo(progress.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing)) }
    val shown = anim.value
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val inset = s / 2
            val arc = Size(this.size.width - s, this.size.height - s)
            drawArc(trackColor, -90f, 360f, false, Offset(inset, inset), arc, style = Stroke(s, cap = StrokeCap.Round))
            if (shown > 0f) {
                drawArc(color, -90f, 360f * shown, false, Offset(inset, inset), arc, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/** Simple 7-bar chart. Bars that reach [goal] (or are at [highlightIndex]) use the orange gradient. */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    highlightIndex: Int = -1,
    goal: Float? = null
) {
    val maxV = maxOf(values.maxOrNull() ?: 0f, goal ?: 0f, 1f)
    Row(
        modifier.fillMaxWidth().height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            val target = (v / maxV).coerceIn(0f, 1f)
            val fracAnim = remember { Animatable(0f) }
            LaunchedEffect(target) { fracAnim.animateTo(target, tween(700, delayMillis = i * 60, easing = FastOutSlowInEasing)) }
            val frac = fracAnim.value
            val lit = (goal != null && v >= goal) || i == highlightIndex
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    Modifier.fillMaxWidth()
                        .height((frac * 90f).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (lit) Brush.verticalGradient(listOf(FitColors.AccentLight, FitColors.AccentDeep))
                            else SolidColor(FitColors.Outline)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    labels.getOrElse(i) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == highlightIndex) FitColors.AccentLight else FitColors.TextMid,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, sub: String) {
    FitCard {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            EmojiIcon(emoji, 56.dp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid)
        }
    }
}

/** 3-column grid of activity tiles (used when logging or planning workouts). */
@Composable
fun ActivityTypeGrid(types: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        types.chunked(3).forEach { rowTypes ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTypes.forEach { t ->
                    val sel = t == selected
                    Column(
                        Modifier
                            .weight(1f)
                            .bounceClick { onSelect(t) }
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (sel) FitColors.Accent.copy(alpha = 0.16f) else FitColors.Surface)
                            .border(
                                BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) FitColors.Accent else FitColors.Outline),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmojiIcon(emojiFor(t), 52.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            t, style = MaterialTheme.typography.labelLarge,
                            color = if (sel) FitColors.AccentLight else FitColors.TextMid
                        )
                    }
                }
            }
        }
    }
}

/** − value + control. */
@Composable
fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier) {
    FitCard(modifier) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid)
                Text(value, style = MaterialTheme.typography.headlineSmall, color = FitColors.TextHigh)
            }
            StepButton("−", onMinus)
            Spacer(Modifier.width(10.dp))
            StepButton("+", onPlus)
        }
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(FitColors.SurfaceHigh)
            .border(BorderStroke(1.dp, FitColors.Outline), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(symbol, color = FitColors.AccentLight, fontSize = 22.sp, style = MaterialTheme.typography.titleMedium) }
}

@Composable
fun NumberDialog(title: String, initial: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var text by remember { mutableStateOf(initial.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitColors.SurfaceHigh,
        title = { Text(title, color = FitColors.TextHigh) },
        text = {
            FitTextField(
                text, { text = it.filter(Char::isDigit) }, "Value",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.toIntOrNull() ?: 0
                if (v > 0) onSave(v)
                onDismiss()
            }) { Text("Save", color = FitColors.Accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = FitColors.TextMid) } }
    )
}

data class TabItem(val route: String, val icon: ImageVector, val label: String)

/**
 * Apple Fitness-style activity rings: dark tracks, gradient strokes, a direction arrow on each ring head,
 * and a second lap (with a soft shadow on the head) once a goal passes 100%.
 * [progress] (1f = goal reached, up to 2f) and [colors] are outermost first.
 */
@Composable
fun ActivityRings(
    progress: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    stroke: Dp = 19.dp,
    gap: Dp = 2.dp
) {
    val anims = progress.indices.map { remember { Animatable(0f) } }
    progress.forEachIndexed { i, p ->
        LaunchedEffect(p) { anims[i].animateTo(p.coerceIn(0f, 2f), tween(1100, delayMillis = i * 140, easing = FastOutSlowInEasing)) }
    }
    Canvas(modifier.size(size)) {
        val s = stroke.toPx()
        val g = gap.toPx()
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        progress.indices.forEach { i ->
            val p = anims[i].value
            val color = colors.getOrElse(i) { FitColors.Accent }
            val dark = androidx.compose.ui.graphics.lerp(Color.Black, color, 0.5f)
            val track = androidx.compose.ui.graphics.lerp(Color.Black, color, 0.22f)
            val r = this.size.width / 2f - (i * (s + g) + s / 2f)
            val topLeft = Offset(c.x - r, c.y - r)
            val arc = Size(2f * r, 2f * r)
            val cap = Stroke(s, cap = StrokeCap.Round)

            drawCircle(track, r, c, style = Stroke(s))

            val first = p.coerceAtMost(1f)
            if (first > 0.002f) {
                val brush = Brush.sweepGradient(0f to dark, first.coerceAtLeast(0.02f) to color, 1f to color, center = c)
                rotate(-90f, c) { drawArc(brush, 0f, 360f * first, false, topLeft, arc, style = cap) }
            }
            val overflow = (p - 1f).coerceAtLeast(0f)
            if (overflow > 0.002f) {
                // soft shadow under the head of the second lap
                val a = Math.toRadians((-90f + 360f * overflow).toDouble())
                val head = Offset(c.x + r * kotlin.math.cos(a).toFloat(), c.y + r * kotlin.math.sin(a).toFloat())
                val brush = Brush.sweepGradient(0f to dark, overflow.coerceAtLeast(0.02f) to color, 1f to color, center = c)
                drawCircle(Color.Black.copy(alpha = 0.38f), s * 0.62f, head + Offset(0f, s * 0.08f))
                rotate(-90f, c) { drawArc(brush, 0f, 360f * overflow, false, topLeft, arc, style = cap) }
            }

            // direction arrow on the ring head
            val headDeg = 360f * (if (p > 1f) overflow else first)
            val a = Math.toRadians((-90f + headDeg).toDouble())
            val pos = Offset(c.x + r * kotlin.math.cos(a).toFloat(), c.y + r * kotlin.math.sin(a).toFloat())
            val k = s * 0.27f
            val ink = Color.Black.copy(alpha = 0.78f)
            val w = s * 0.14f
            withTransform({ translate(pos.x, pos.y); rotate(headDeg, Offset.Zero) }) {
                drawLine(ink, Offset(-k, 0f), Offset(k, 0f), w, StrokeCap.Round)
                drawLine(ink, Offset(k, 0f), Offset(k * 0.15f, -k * 0.85f), w, StrokeCap.Round)
                drawLine(ink, Offset(k, 0f), Offset(k * 0.15f, k * 0.85f), w, StrokeCap.Round)
            }
        }
    }
}

/** Label on top, then a big coloured value with a small caps goal/unit right after it ("461/750KCAL"). */
@Composable
fun RingStat(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = FitColors.TextHigh)
        Text(
            androidx.compose.ui.text.buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(fontSize = 24.sp, fontFamily = FitDisplay, fontWeight = FontWeight.Bold)) { append(value) }
                withStyle(androidx.compose.ui.text.SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append(unit.uppercase()) }
            },
            color = color, maxLines = 1
        )
    }
}

/** Round profile picture: shows the saved photo if there is one, otherwise the user's initial on lime. */
@Composable
fun AvatarCircle(initial: String, size: Dp = 40.dp, imagePath: String? = null, onClick: (() -> Unit)? = null) {
    val bitmap = remember(imagePath) {
        imagePath?.takeIf { it.isNotBlank() }?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    if (bitmap != null) {
        Image(
            bitmap = bitmap, contentDescription = "Profile picture", contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape).then(clickMod)
        )
    } else {
        Box(
            Modifier.size(size).clip(CircleShape).background(FitColors.Accent).then(clickMod),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.42f).sp)
        }
    }
}

/** ‹ 22:30 › in 15-minute steps. */
@Composable
fun TimeStepper(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    fun shift(delta: Int) {
        val total = (hour * 60 + minute + delta + 1440) % 1440
        onChange(total / 60, total % 60)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton("−") { shift(-15) }
        Text(
            "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        StepButton("+") { shift(15) }
    }
}

@Composable
fun TextDialog(title: String, initial: String, label: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitColors.SurfaceHigh,
        title = { Text(title, color = FitColors.TextHigh) },
        text = { FitTextField(text, { text = it.take(30) }, label) },
        confirmButton = {
            TextButton(onClick = { onSave(text.trim()); onDismiss() }) { Text("Save", color = FitColors.Accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = FitColors.TextMid) } }
    )
}

/** A labelled settings row (iOS list style). Pass [onClick] to make it tappable. */
@Composable
fun SettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    labelColor: Color = FitColors.TextHigh,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor, modifier = Modifier.weight(1f))
        if (value != null) Text(value, style = MaterialTheme.typography.bodyLarge, color = FitColors.TextMid)
        if (trailing != null) trailing()
        if (chevron) {
            Spacer(Modifier.width(8.dp))
            Text("›", fontSize = 22.sp, color = FitColors.TextMid)
        }
    }
}

@Composable
fun SettingsGroup(title: String, delayMs: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(), style = MaterialTheme.typography.labelSmall, color = FitColors.TextMid,
            modifier = Modifier.fadeSlideIn(delayMs).padding(start = 16.dp, bottom = 8.dp)
        )
        FitCard(delayMs = delayMs, content = content)
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(color = FitColors.Outline, modifier = Modifier.padding(start = 16.dp))
}

/** Rounded accent-tinted background used for emoji tiles. */
fun Modifier.accentTile(): Modifier =
    this.clip(RoundedCornerShape(14.dp)).background(FitColors.Accent.copy(alpha = 0.15f))


/** Simple line chart with dots, scaled to the min/max of [values]. */
@Composable
fun LineChart(values: List<Float>, modifier: Modifier = Modifier) {
    if (values.size < 2) {
        Text(
            "Log at least two entries to see your trend.",
            style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid, modifier = modifier
        )
        return
    }
    val minV = values.minOrNull() ?: 0f
    val maxV = values.maxOrNull() ?: 0f
    val range = (maxV - minV).coerceAtLeast(0.5f)
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        val padY = 12.dp.toPx()
        fun px(i: Int) = size.width * i / (values.size - 1)
        fun py(v: Float) = padY + (size.height - 2 * padY) * (1f - (v - minV) / range)
        val path = Path()
        values.forEachIndexed { i, v -> if (i == 0) path.moveTo(px(i), py(v)) else path.lineTo(px(i), py(v)) }
        drawPath(path, FitColors.Accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        values.forEachIndexed { i, v -> drawCircle(FitColors.AccentLight, 5.dp.toPx(), Offset(px(i), py(v))) }
    }
}

@Composable
fun DecimalDialog(title: String, initial: String, unit: String, onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitColors.SurfaceHigh,
        title = { Text(title, color = FitColors.TextHigh) },
        text = {
            FitTextField(
                text, { t -> text = t.filter { it.isDigit() || it == '.' }.take(6) }, unit,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.toFloatOrNull() ?: 0f
                if (v > 0f) onSave(v)
                onDismiss()
            }) { Text("Save", color = FitColors.Accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = FitColors.TextMid) } }
    )
}
