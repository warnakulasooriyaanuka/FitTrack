package com.example.fittrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fittrack.ui.theme.FitColors

/** Plain black screen background (like the iPhone Fitness app). */
fun Modifier.screenBackground(): Modifier = this.background(FitColors.Bg)

/** Solid accent button. */
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .bounceClick(enabled, onClick)
            .alpha(androidx.compose.animation.core.animateFloatAsState(if (enabled) 1f else 0.4f, androidx.compose.animation.core.tween(250), label = "btnAlpha").value)
            .clip(RoundedCornerShape(16.dp))
            .background(FitColors.Accent),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.Crossfade(text, label = "btnText") {
            Text(it, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Secondary button: dark gray fill, accent text. */
@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .bounceClick(enabled, onClick)
            .alpha(androidx.compose.animation.core.animateFloatAsState(if (enabled) 1f else 0.4f, androidx.compose.animation.core.tween(250), label = "btnAlpha").value)
            .clip(RoundedCornerShape(16.dp))
            .background(FitColors.SurfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.Crossfade(text, label = "btnText") {
            Text(it, color = FitColors.TextHigh, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun FitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitColors.Accent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = FitColors.SurfaceHigh,
            unfocusedContainerColor = FitColors.Surface,
            focusedLabelColor = FitColors.Accent,
            unfocusedLabelColor = FitColors.TextMid,
            cursorColor = FitColors.Accent,
            focusedTextColor = FitColors.TextHigh,
            unfocusedTextColor = FitColors.TextHigh,
            focusedLeadingIconColor = FitColors.Accent,
            unfocusedLeadingIconColor = FitColors.TextMid
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Flat #1C1C1E card, no border. */
@Composable
fun FitCard(modifier: Modifier = Modifier, delayMs: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fadeSlideIn(delayMs).then(modifier)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FitColors.Surface),
        content = content
    )
}
