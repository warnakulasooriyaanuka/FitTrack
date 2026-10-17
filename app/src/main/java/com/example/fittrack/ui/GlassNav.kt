package com.example.fittrack.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.fittrack.ui.theme.FitColors
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Floating, fully rounded "liquid glass" tab bar (iOS 26 style):
 * translucent frosted pill, glossy edge highlight, and a glass lens that slides
 * (and stretches like liquid) to the selected tab with a springy motion.
 */
@Composable
fun FitBottomBar(items: List<TabItem>, currentRoute: String?, onSelect: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val selIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val barShape = RoundedCornerShape(36.dp)
    val lensShape = RoundedCornerShape(28.dp)

    Box(
        Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(70.dp)
                .shadow(28.dp, barShape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(barShape)
                .background(Color(0xFF1C1C1E).copy(alpha = 0.78f))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.20f))
                    ),
                    barShape
                )
                .padding(horizontal = 6.dp)
        ) {
            val tabW = maxWidth / items.size
            val tabPx = with(density) { tabW.toPx() }
            val targetPx = tabPx * selIndex
            val offsetPx by animateFloatAsState(
                targetPx, spring(dampingRatio = 0.62f, stiffness = 260f), label = "lens"
            )
            // 0 when settled, grows while the lens is travelling -> it stretches like a drop of liquid
            val travel = (abs(offsetPx - targetPx) / tabPx).coerceIn(0f, 1.5f)

            Box(
                Modifier
                    .offset { IntOffset(offsetPx.roundToInt(), 0) }
                    .width(tabW).fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 7.dp)
                    .graphicsLayer { scaleX = 1f + 0.22f * travel; scaleY = 1f - 0.06f * travel }
                    .clip(lensShape)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.08f))))
                    .background(Brush.radialGradient(listOf(FitColors.Accent.copy(alpha = 0.26f), Color.Transparent)))
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.08f))),
                        lensShape
                    )
            )

            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, item ->
                    GlassTab(item, i == selIndex) {
                        if (i != selIndex) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(item.route)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.GlassTab(item: TabItem, selected: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val tint by animateColorAsState(
        if (selected) FitColors.Accent else FitColors.TextMid, tween(260), label = "tint"
    )
    val iconScale by animateFloatAsState(
        if (pressed) 0.84f else if (selected) 1.14f else 1f,
        spring(dampingRatio = 0.45f, stiffness = 380f), label = "iconScale"
    )
    val lift by animateFloatAsState(if (selected) -2f else 0f, spring(dampingRatio = 0.6f, stiffness = 300f), label = "lift")

    Column(
        Modifier.weight(1f).fillMaxHeight()
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            item.icon, contentDescription = item.label, tint = tint,
            modifier = Modifier.size(26.dp).graphicsLayer {
                scaleX = iconScale; scaleY = iconScale; translationY = lift * density
            }
        )
        Spacer(Modifier.height(2.dp))
        Text(
            item.label, style = MaterialTheme.typography.labelSmall, color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
