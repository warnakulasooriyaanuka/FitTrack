package com.example.fittrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ai.ChatMsg
import com.example.fittrack.data.CoachEngine
import com.example.fittrack.ui.theme.FitColors
import kotlinx.coroutines.delay

private val Suggestions = listOf(
    "💪" to "What's my workout today?",
    "⏱️" to "Give me a quick 15 minute workout",
    "🍎" to "What should I eat?",
    "🌙" to "How can I sleep better?",
    "🏋️" to "How do I do push-ups?",
    "📈" to "How am I doing?"
)

@Composable
fun CoachScreen(vm: CoachViewModel, tvm: TrackerViewModel) {
    val messages by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    val error by vm.error.collectAsState()
    val key by vm.apiKey.collectAsState()
    var showKey by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var talking by remember { mutableStateOf(false) }
    val keyboardUp = rememberImeVisible()
    val listState = rememberLazyListState()
    val smart = key.isNotBlank()

    fun send(text: String) {
        vm.send(text, tvm.coachContext()) { q -> CoachEngine.reply(q, tvm.coachSnapshot()) }
    }

    // the coach talks for a moment after each reply, roughly as long as the message
    LaunchedEffect(messages.size) {
        val last = messages.lastOrNull()
        if (last != null && !last.fromUser) {
            talking = true
            delay((700 + last.text.length * 26L).coerceAtMost(5000L))
            talking = false
        }
    }
    // keep the newest message in view
    LaunchedEffect(messages.size, busy, error) {
        val count = 1 + messages.size + (if (busy) 1 else 0) + (if (error != null) 1 else 0)
        listState.animateScrollToItem(count - 1)
    }

    Column(Modifier.fillMaxSize().screenBackground().statusBarsPadding().imePadding()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            ScreenHeader("Coach") {
                SmallPill(if (smart) "Smart mode" else "Smart mode off", { showKey = !showKey })
                if (messages.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    SmallPill("New chat", { vm.clear() })
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                if (smart) "Smart mode (Gemini). Falls back to the offline coach if needed."
                else "Offline coach: works without internet or an API key.",
                style = MaterialTheme.typography.labelMedium, color = FitColors.TextMid
            )
        }

        // the 3D coach: drag to turn, tap to wave
        Coach3D(Modifier.fillMaxWidth().height(if (keyboardUp) 70.dp else 150.dp), talking = talking)

        if (showKey) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                FitCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Smart mode (optional)", style = MaterialTheme.typography.titleMedium, color = FitColors.TextHigh)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "The offline coach already answers fitness questions about your plan, food, sleep and form. " +
                                "For open-ended chat, paste a free Gemini key from aistudio.google.com/apikey. It's stored on this device only.",
                            style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                        )
                        Spacer(Modifier.height(12.dp))
                        FitTextField(keyInput, { keyInput = it }, if (smart) "New API key (a key is already saved)" else "Gemini API key")
                        Spacer(Modifier.height(12.dp))
                        GradientButton(
                            "Save key",
                            onClick = { vm.saveKey(keyInput); keyInput = ""; showKey = false },
                            enabled = keyInput.isNotBlank()
                        )
                        if (smart) {
                            Spacer(Modifier.height(8.dp))
                            GhostButton("Turn off Smart mode", onClick = { vm.saveKey(""); showKey = false })
                        }
                    }
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                if (messages.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Hi! I'm Coach Fit.", style = MaterialTheme.typography.titleLarge, color = FitColors.TextHigh)
                        Text(
                            "Ask me about training, food or recovery. I can see your steps, meals, sleep, streak and plan.",
                            style = MaterialTheme.typography.bodyMedium, color = FitColors.TextMid
                        )
                        Spacer(Modifier.height(2.dp))
                        Suggestions.forEach { (emoji, text) ->
                            FitCard(Modifier.bounceClick { send(text) }) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    EmojiIcon(emoji, 28.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(text, style = MaterialTheme.typography.bodyMedium, color = FitColors.TextHigh)
                                }
                            }
                        }
                    }
                } else Spacer(Modifier.height(1.dp))
            }
            items(messages) { m -> Bubble(m) }
            if (busy) item { Bubble(ChatMsg(false, "Thinking…"), dim = true) }
            error?.let { e ->
                item {
                    Text(
                        e, color = FitColors.Danger, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(FitColors.Danger.copy(alpha = 0.12f)).padding(12.dp)
                    )
                }
            }
        }

        // input bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FitTextField(input, { input = it }, "Ask your coach…", Modifier.weight(1f))
            Spacer(Modifier.width(10.dp))
            val canSend = input.isNotBlank() && !busy
            Box(
                Modifier.size(54.dp).clip(CircleShape).alpha(if (canSend) 1f else 0.4f)
                    .background(AccentBrush)
                    .clickable(enabled = canSend) { val t = input; input = ""; send(t) },
                contentAlignment = Alignment.Center
            ) { Text("➤", color = Color.Black, fontSize = 20.sp) }
        }
        Spacer(Modifier.height(LocalBarInset.current))
    }
}

@Composable
private fun Bubble(m: ChatMsg, dim: Boolean = false) {
    val user = m.fromUser
    Box(Modifier.fillMaxWidth(), contentAlignment = if (user) Alignment.CenterEnd else Alignment.CenterStart) {
        Text(
            m.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (user) Color.Black else if (dim) FitColors.TextMid else FitColors.TextHigh,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    if (user) RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
                    else RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
                )
                .background(if (user) AccentBrush else androidx.compose.ui.graphics.SolidColor(FitColors.Surface))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
