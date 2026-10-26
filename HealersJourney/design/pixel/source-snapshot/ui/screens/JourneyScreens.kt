package com.healersjourney.app.ui.screens

import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import com.healersjourney.app.ui.components.PixelShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
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
    WorldHome(snapshot, viewModel, onOpen)
}

@Composable
private fun GrowthItems(snapshot: GameSnapshot, viewModel: GameViewModel, onOpen: (Destination) -> Unit) {
    val state = snapshot.state




    val skills = listOf(
        Triple(Destination.LearningProgress, "knowledgeXp", "Learning"),
        Triple(Destination.WorkProgress, "medicineXp", "Work"),
        Triple(Destination.FitnessProgress, "strengthXp", "Fitness"),
        Triple(Destination.SocialProgress, "socialXp", "Social"),
        Triple(Destination.CreativityProgress, "hobbiesXp", "Creativity")
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        JourneyHeading("Your growth", "See your progress across every part of life.")
        skills.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { (destination, skill, name) ->
                    val xp = GameEngine.weeklyXp(state, skill)
                    GrowthTile(name, Art.skillIcon(skill), "$xp / ${Rules.WEEKLY_TARGETS[skill]} XP this week", skillColor(skill),
                        xp.toFloat() / (Rules.WEEKLY_TARGETS[skill] ?: 1), Modifier.weight(1f)) { onOpen(destination) }
                }
                if (pair.size == 1) GrowthTile("Rest", R.drawable.pixel_sleep, "${GameEngine.numText(GameEngine.previousNightHours(state))} h last night",
                    Color(0xFFAE9ADE), null, Modifier.weight(1f)) { onOpen(Destination.RestProgress) }
            }
        }
        val series = Reports.weeklyStudySeries(state)
        JourneyAction(R.drawable.pixel_week, "Your week in focus",
            "${GameEngine.numText(series.sumOf { it.second })} learning hours · ${GameEngine.workoutStreak(state)} day workout streak",
            Color(0xFF69C9B7)) { onOpen(Destination.Logs) }
    }
}

@Composable
fun ExploreScreen(onOpen: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        JourneyHeading("Room for every part of life", "Keep the essentials close.")
        ExploreGroup("Everyday life", listOf(
            Triple(Destination.Finance, R.drawable.pixel_finance, "Money & goals"),
            Triple(Destination.Care, R.drawable.pixel_care, "Personal care")
        ), onOpen)
        ExploreGroup("Trackers", listOf(
            Triple(Destination.Study, Art.skillIcon("knowledgeXp"), "Learning sessions"),
            Triple(Destination.Hobbies, Art.skillIcon("hobbiesXp"), "Creative sessions"),
            Triple(Destination.Sleep, R.drawable.pixel_sleep, "Sleep tracker")
        ), onOpen)
        ExploreGroup("Your journey", listOf(
            Triple(Destination.Milestones, R.drawable.pixel_milestone, "Milestones"),
            Triple(Destination.Achievements, R.drawable.pixel_achievement, "Achievements"),
            Triple(Destination.Logs, R.drawable.pixel_logs, "History & insights"),
            Triple(Destination.Daily, R.drawable.pixel_today, "Custom activity")
        ), onOpen)
        ExploreGroup("App", listOf(Triple(Destination.Data, R.drawable.pixel_sync, "Backup & sync")), onOpen)
    }
}

@Composable
private fun ExploreGroup(title: String, entries: List<Triple<Destination, Int, String>>, onOpen: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(shape = PixelShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column {
                entries.forEachIndexed { index, (destination, icon, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onOpen(destination) }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Image(painterResource(icon), null, Modifier.size(36.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Image(painterResource(R.drawable.pixel_right), null, Modifier.size(16.dp))
                    }
                    if (index < entries.lastIndex) HorizontalDivider(Modifier.padding(start = 66.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GrowthTile(title: String, icon: Int, detail: String, accent: Color, progress: Float?, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = PixelShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(58.dp).clip(PixelShape(18.dp)).background(accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(46.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (progress != null) SimpleProgress(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(PixelShape(2.dp)),
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
    Surface(onClick = onClick, shape = PixelShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(PixelShape(16.dp)).background(accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(40.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Image(painterResource(R.drawable.pixel_right), null, Modifier.size(16.dp))
        }
    }
}
