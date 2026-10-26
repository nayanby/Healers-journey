package com.healersjourney.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.healersjourney.app.ui.components.PixelEdgeShape
import com.healersjourney.app.ui.components.PixelShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healersjourney.app.R
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.*

/** A read-only journey, opened through normal app navigation from Home. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryJourneyScreen(snapshot: GameSnapshot, skill: String, viewModel: GameViewModel, onGoToday: (String) -> Unit) {
    var showEditor by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ActivityDef?>(null) }
    if (showEditor) PresetEditorDialog(skill, snapshot, viewModel, initialActivity = selected) { showEditor = false; selected = null }
    val state = snapshot.state
    val rest = skill == "sleep"
    val accent = if (rest) Color(0xFFB4A0ED) else skillColor(skill)
    val icon = if (rest) R.drawable.pixel_sleep else Art.skillIcon(skill)
    val actions = state.actionHistory.filter { it.skill == skill }
        .sortedWith(compareByDescending<ActionEntry> { it.date }.thenByDescending { it.ts })
    val days = Dates.currentWeekKeys()
    val active = days.map { day -> actions.any { it.date == day } }
    val earned = if (rest) active.count { it } else GameEngine.weeklyXp(state, skill)
    val goal = if (rest) 7 else (Rules.WEEKLY_TARGETS[skill] ?: 1).coerceAtLeast(1)
    val fraction = (earned.toFloat() / goal).coerceIn(0f, 1f)
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val progress by animateFloatAsState(if (revealed) fraction else 0f, tween(1000), label = "Journey progress")
    val invitation = when (skill) {
        "knowledgeXp" -> "A little curiosity. A wider world."
        "medicineXp" -> "Meaningful work, one step at a time."
        "strengthXp" -> "Build the energy to live fully."
        "socialXp" -> "Small connections become lasting bonds."
        "hobbiesXp" -> "Make room for what lights you up."
        else -> "Give yourself room to recharge."
    }
    val chapter = when {
        fraction >= 1f -> "A week to be proud of"
        fraction >= .5f -> "Your momentum is growing"
        earned > 0 -> "You're finding your rhythm"
        else -> "A new chapter awaits"
    }
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(Modifier.fillMaxWidth().clip(PixelShape(32.dp))
            .background(Color(0xFF223949))
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("YOUR ${if (rest) "RECOVERY" else "${Rules.SKILL_LABELS[skill]}".uppercase()} JOURNEY",
                style = MaterialTheme.typography.labelMedium, color = accent)
            Box(Modifier.size(232.dp).semantics { contentDescription = "Weekly journey ${(fraction * 100).toInt()} percent complete" }, contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val cell = size.width / 32f
                    val points = (4..27).map { it to 3 } + (4..27).map { 28 to it } + (27 downTo 4).map { it to 28 } + (27 downTo 4).map { 3 to it }
                    points.forEachIndexed { index, (x, y) ->
                        drawRect(if (index < progress * points.size) accent else Color.White.copy(alpha=.14f),
                            Offset(x * cell,y * cell), Size(cell*.8f,cell*.8f))
                    }
                }
                Image(painterResource(icon), null, Modifier.size(120.dp))
            }
            Text(chapter, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(invitation, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCCD7E9),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Surface(color = accent.copy(alpha = .16f), shape = PixelEdgeShape) {
                Text(if (rest) "$earned of 7 nights recorded this week" else "$earned of $goal XP on this week's path",
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
        AppButton(onClick = { onGoToday(skill) }, modifier = Modifier.fillMaxWidth()) { Text("Go to Today to log") }
        if (!rest) SectionCard(title = "Your activities", collapsible = true, initiallyExpanded = true) {
            Text("Hold an activity to edit or delete it.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            val definitions = listOf("major", "other").flatMap { GameEngine.defsFor(state, skill, it) }
            if (definitions.isEmpty()) EmptyHint("Add an activity to make this journey your own.")
            definitions.forEach { definition ->
                Row(Modifier.fillMaxWidth().combinedClickable(onClick = {},
                    onLongClickLabel = "Edit or delete preset", onLongClick = { selected = definition; showEditor = true })
                    .padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(icon), null, Modifier.size(32.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(Rules.activityLabel(skill, definition.name), style = MaterialTheme.typography.titleSmall)
                        Text("${definition.xp} base XP", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            AppTextButton(onClick = { selected = null; showEditor = true }) { Text("Add preset") }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your week, one step at a time", style = MaterialTheme.typography.titleLarge)
            Text(if (active.any { it }) "Every glowing step is a day you showed up." else "Your trail lights up as you show up.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            WeeklyTrail(days, active, accent, icon)
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Along the way", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                JourneyCheckpoint("First step", earned > 0, R.drawable.pixel_xp, accent, Modifier.weight(1f))
                JourneyCheckpoint("Halfway", fraction >= .5f, R.drawable.pixel_streak, accent, Modifier.weight(1f))
                JourneyCheckpoint("Week complete", fraction >= 1f, R.drawable.pixel_achievement, accent, Modifier.weight(1f))
            }
            Text(when {
                fraction >= 1f -> "You reached this week's goal. Every extra step is a bonus."
                earned == 0 -> "Start with something small. Your first step matters."
                fraction < .5f -> "You're on your way. Small efforts add up."
                else -> "Look how far you've come. Keep that rhythm going."
            }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (rest) {
            val stats = SleepStats.of(state)
            if (stats.entries > 0) JourneyMemory(R.drawable.pixel_sleep, "Time to restore",
                "${GameEngine.numText(stats.averageSleep)} hours on average across your recorded nights", accent)
        } else if (state.skillXp(skill) > 0) {
            JourneyMemory(R.drawable.pixel_achievement, "Look how far you've come",
                "${state.skillXp(skill)} XP earned on your ${Rules.SKILL_LABELS[skill]?.lowercase()} journey", accent)
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Moments that moved you forward", style = MaterialTheme.typography.titleLarge)
            if (actions.isEmpty()) {
                Surface(shape = PixelShape(24.dp), color = accent.copy(alpha = .08f)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(painterResource(icon), null, Modifier.size(48.dp))
                        Text("Your story starts with a small step", style = MaterialTheme.typography.titleMedium)
                        Text("As you record your days, your moments will collect here.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            actions.take(5).forEach { action ->
                JourneyMemory(icon, if (rest) "A night of rest" else Rules.activityLabel(skill, action.name),
                    "${Dates.prettyCareDate(action.date)} · " + if (rest) "${GameEngine.numText(action.hours)} hours of recovery" else "+${action.xp} XP on your journey", accent)
            }
        }
    }
}

@Composable
private fun WeeklyTrail(days: List<String>, active: List<Boolean>, accent: Color, icon: Int) {
    val track = MaterialTheme.colorScheme.outlineVariant
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val points = listOf(.12f to .18f, .38f to .18f, .66f to .18f, .86f to .46f, .66f to .75f, .38f to .75f, .12f to .75f)
    BoxWithConstraints(Modifier.fillMaxWidth().height(236.dp).clip(PixelShape(28.dp)).background(accent.copy(alpha = .06f))) {
        Canvas(Modifier.fillMaxSize()) {
            val path = Path()
            points.forEachIndexed { index, (x, y) ->
                if (index == 0) path.moveTo(size.width * x, size.height * y)
                else {
                    val previous = points[index - 1]
                    path.lineTo(size.width * x, size.height * previous.second)
                    path.lineTo(size.width * x, size.height * y)
                }
            }
            drawPath(path, track.copy(alpha = .65f), style = Stroke(5.dp.toPx(), cap = StrokeCap.Square))
        }
        points.forEachIndexed { index, (x, y) ->
            val today = days[index] == Dates.gameDayKey()
            Column(Modifier.offset(x = maxWidth * x - 22.dp, y = 236.dp * y - 22.dp).width(44.dp)
                .semantics { contentDescription = "${labels[index]}: ${if (active[index]) "activity recorded" else "no activity recorded"}${if (today) ", today" else ""}" },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Surface(shape = PixelEdgeShape, color = if (active[index]) accent else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(if (today) 2.dp else 1.dp, if (today) accent else track)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        if (active[index]) Image(painterResource(icon), null, Modifier.size(30.dp))
                        else Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(if (today) "Today" else labels[index], style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun JourneyCheckpoint(title: String, reached: Boolean, icon: Int, accent: Color, modifier: Modifier) {
    Column(modifier.clip(PixelShape(20.dp)).background(if (reached) accent.copy(alpha = .14f) else MaterialTheme.colorScheme.surface)
        .padding(horizontal = 8.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Image(painterResource(icon), null, Modifier.size(42.dp), alpha = if (reached) 1f else .32f)
        Text(title, style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(if (reached) "Reached" else "Ahead", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun JourneyMemory(icon: Int, title: String, detail: String, accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).clip(PixelShape(16.dp)).background(accent.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
            Image(painterResource(icon), null, Modifier.size(34.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
