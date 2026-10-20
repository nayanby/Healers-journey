package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healersjourney.app.Destination
import com.healersjourney.app.R
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(snapshot: GameSnapshot, viewModel: GameViewModel, onOpen: (Destination) -> Unit) {
    val state = snapshot.state
    val level = Rules.level(state.totalXp)
    val title = Rules.title(level)
    val xp = state.totalXp % Rules.XP_PER_LEVEL
    val care = GameEngine.personalCareStats(state)
    val pendingCare = care.items.count { it.last != Dates.todayKey() && it.missedUnits > 0 }
    var titles by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Your next chapter.", style = MaterialTheme.typography.headlineMedium)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF6557CF), Color(0xFF34458B))))
            .padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.playerName, style = MaterialTheme.typography.titleLarge, color = Color.White,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { rename = true }, modifier = Modifier.size(36.dp)) {
                            Image(painterResource(R.drawable.color_edit), "Rename player", Modifier.size(22.dp))
                        }
                    }
                    Text("Level $level · $title", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE4DFFF))
                }
                Spacer(Modifier.width(12.dp))
                Image(painterResource(Art.badgeFor(title)), "View levels and titles",
                    Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).clickable { titles = true })
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Next level", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Text("$xp / ${Rules.XP_PER_LEVEL} XP", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
                SimpleProgress(progress = { xp.toFloat() / Rules.XP_PER_LEVEL },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFD482), trackColor = Color.White.copy(alpha = 0.18f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.color_streak), null, Modifier.size(22.dp))
                Text("${state.streak} day streak", style = MaterialTheme.typography.labelMedium, color = Color.White)
                Spacer(Modifier.weight(1f))

            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            JourneyHeading("Make a little progress", "Pick one thing to start.")
            JourneyAction(R.drawable.color_todo, "Plan your day", "Your tasks, one at a time", Color(0xFFEDB765)) { onOpen(Destination.Todo) }
            JourneyAction(R.drawable.color_study, "Find your focus", "${GameEngine.numText(GameEngine.studyHoursToday(state))} hours studied today", Color(0xFF8C9DFF)) { onOpen(Destination.Study) }
            JourneyAction(R.drawable.color_care, "Look after yourself",
                if (pendingCare > 0) "$pendingCare care routines need attention" else "Check in with your care routines", Color(0xFF69C9B7)) { onOpen(Destination.Care) }
        }
        GameEngine.visibleMilestones(state).firstOrNull { !it.completed }?.let { milestone ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JourneyHeading("On the horizon")
                JourneyAction(R.drawable.color_milestone, milestone.title,
                    "Open your milestones", Color(0xFFF18A8A)) { onOpen(Destination.Milestones) }
            }
        }
    }
    if (titles) TitleShelfDialog(level) { titles = false }
    if (rename) RenameDialog(state.playerName, { rename = false }) { name ->
        viewModel.run { GameEngine.renamePlayer(it, name) }; rename = false
    }
}

@Composable
fun GrowthScreen(snapshot: GameSnapshot, onOpen: (Destination) -> Unit) {
    val state = snapshot.state
    val skills = listOf(
        Triple(Destination.Study, "knowledgeXp", "Study"),
        Triple(Destination.Medicine, "medicineXp", "Medicine"),
        Triple(Destination.Fitness, "strengthXp", "Fitness"),
        Triple(Destination.Social, "socialXp", "Social"),
        Triple(Destination.Hobbies, "hobbiesXp", "Hobbies")
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        JourneyHeading("Build a life you love", "Small efforts grow into lasting skills.")
        skills.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { (destination, skill, name) ->
                    val xp = GameEngine.weeklyXp(state, skill)
                    GrowthTile(name, Art.skillIcon(skill), "$xp XP this week", skillColor(skill),
                        xp.toFloat() / (Rules.WEEKLY_TARGETS[skill] ?: 1), Modifier.weight(1f)) { onOpen(destination) }
                }
                if (pair.size == 1) GrowthTile("Rest", R.drawable.color_sleep, "Sleep & recovery",
                    Color(0xFFAE9ADE), null, Modifier.weight(1f)) { onOpen(Destination.Sleep) }
            }
        }
        val series = Reports.weeklyStudySeries(state)
        JourneyAction(R.drawable.color_week, "Your week in focus",
            "${GameEngine.numText(series.sumOf { it.second })} study hours · ${GameEngine.workoutStreak(state)} day workout streak",
            Color(0xFF69C9B7)) { onOpen(Destination.Logs) }
    }
}

@Composable
fun ExploreScreen(onOpen: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        JourneyHeading("Room for every part of life", "Keep the essentials close.")
        ExploreGroup("Everyday life", listOf(
            Triple(Destination.Finance, R.drawable.color_finance, "Money & goals"),
            Triple(Destination.Care, R.drawable.color_care, "Personal care")
        ), onOpen)
        ExploreGroup("Your journey", listOf(
            Triple(Destination.Milestones, R.drawable.color_milestone, "Milestones"),
            Triple(Destination.Achievements, R.drawable.color_achievement, "Achievements"),
            Triple(Destination.Logs, R.drawable.color_logs, "History & insights"),
            Triple(Destination.Daily, R.drawable.color_today, "Custom activity")
        ), onOpen)
        ExploreGroup("App", listOf(Triple(Destination.Data, R.drawable.color_sync, "Backup & sync")), onOpen)
    }
}

@Composable
private fun ExploreGroup(title: String, entries: List<Triple<Destination, Int, String>>, onOpen: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column {
                entries.forEachIndexed { index, (destination, icon, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onOpen(destination) }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Image(painterResource(icon), null, Modifier.size(36.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Image(painterResource(R.drawable.color_right), null, Modifier.size(16.dp))
                    }
                    if (index < entries.lastIndex) HorizontalDivider(Modifier.padding(start = 66.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun GrowthTile(title: String, icon: Int, detail: String, accent: Color, progress: Float?, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(46.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (progress != null) SimpleProgress(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = accent, trackColor = accent.copy(alpha = 0.12f))
            else Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun JourneyHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun JourneyAction(icon: Int, title: String, detail: String, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(40.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Image(painterResource(R.drawable.color_right), null, Modifier.size(16.dp))
        }
    }
}
