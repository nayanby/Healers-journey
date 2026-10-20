package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.healersjourney.app.ui.components.AppButton as Button
import com.healersjourney.app.ui.components.AppTonalButton as FilledTonalButton
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.healersjourney.app.ui.components.AppTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.FreshnessRing
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.skillColor

/** Headline stats for one attribute: freshness ring, weekly progress and lifetime XP. */
@Composable
fun SkillSummaryCard(snapshot: GameSnapshot, skill: String) {
    val state = snapshot.state
    val level = GameEngine.batteryLevelForSkill(state, skill)
    val color = skillColor(skill)
    SectionCard(title = "Your momentum") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FreshnessRing(level, Modifier.size(AppLayout.ring), strokeWidth = 4.dp)
            Spacer(Modifier.width(AppLayout.contentGap))
            Column(Modifier.weight(1f)) {
                StatLine(
                    "This week",
                    "${GameEngine.weeklyXp(state, skill)} / ${Rules.WEEKLY_TARGETS[skill]}",
                    color
                )
                StatLine("Total XP", "${state.skillXp(skill)}")
            }
        }
    }
}

/** The per-attribute quest list: two headline actions plus the player's own extras. */
@Composable
fun SkillActivityCard(snapshot: GameSnapshot, viewModel: GameViewModel, skill: String) {
    val state = snapshot.state
    val color = skillColor(skill)
    val label = Rules.SKILL_LABELS[skill] ?: skill
    val major = GameEngine.defsFor(state, skill, "major")
    val other = GameEngine.defsFor(state, skill, "other")
    val totalXp = state.skillXp(skill)
    var expanded by rememberSaveable(skill) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppLayout.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(AppLayout.screenPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Art.skillIcon(skill)),
                    contentDescription = null,
                    modifier = Modifier.size(AppLayout.illustration)
                )
                Spacer(Modifier.width(AppLayout.contentGap))
                Text("Choose an activity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

            }
            Spacer(Modifier.height(AppLayout.contentGap))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                major.forEachIndexed { index, def ->
                    val (finalXp, decay) = GameEngine.activityXpPreview(state, skill, def.xp)
                    FilledTonalButton(
                        onClick = { viewModel.run { GameEngine.completeActivity(it, skill, "major", index) } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Text(
                                def.name,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                if (decay > 0) "+$finalXp (-$decay%)" else "+$finalXp",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(AppLayout.icon)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (expanded) "Hide ${other.size} more" else "More (${other.size})")
            }

            if (expanded) {
                if (other.isEmpty()) EmptyHint("No extra activities yet. Add one below.")
                other.forEachIndexed { index, def ->
                    val (finalXp, decay) = GameEngine.activityXpPreview(state, skill, def.xp)
                    val id = def.id.ifEmpty { "${skill}_other_$index" }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(def.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (decay > 0) "+$finalXp XP (-$decay%)" else "+$finalXp XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = {
                            viewModel.run { GameEngine.completeActivity(it, skill, "other", index) }
                        }) { Text("Do") }
                        IconButton(onClick = {
                            viewModel.run { GameEngine.removeActivityById(it, skill, id) }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove ${def.name}",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppLayout.icon)
                            )
                        }
                    }
                }
                AddActivityRow(skill) { name, xp ->
                    viewModel.run { GameEngine.addActivity(it, skill, name, xp) }
                }
            }
        }
    }
}

@Composable
private fun AddActivityRow(skill: String, onAdd: (String, Int) -> Unit) {
    var name by remember(skill) { mutableStateOf("") }
    var xp by remember(skill) { mutableStateOf("20") }
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("New activity name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberField(
                value = xp,
                onValueChange = { xp = it },
                label = "XP assigned",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(AppLayout.contentGap))
            Button(onClick = { onAdd(name, xp.toIntOrNull() ?: 0) }) { Text("Add") }
        }
    }
}

@Composable
fun WeeklyWorkoutCard(snapshot: GameSnapshot) {
    val state = snapshot.state
    val days = Dates.currentWeekKeys()
    val today = Dates.todayKey()
    val done = state.workoutDates.toSet()
    SectionCard(title = "Workouts this week") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEachIndexed { index, key ->
                val isDone = done.contains(key)
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .background(
                                when {
                                    isDone -> MaterialTheme.colorScheme.tertiary
                                    key == today -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                CircleShape
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(DAY_LABELS[index], style = MaterialTheme.typography.labelSmall)
                    Text(
                        if (isDone) "Done" else "--",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDone) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(AppLayout.contentGap))
        StatLine("Week total", "${days.count { done.contains(it) }} / 7")
        StatLine("Workout streak", "${GameEngine.workoutStreak(state)} days")
    }
}

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun MedicineScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SkillSummaryCard(snapshot, "medicineXp")
        SkillActivityCard(snapshot, viewModel, "medicineXp")
    }
}

@Composable
fun FitnessScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SkillSummaryCard(snapshot, "strengthXp")
        WeeklyWorkoutCard(snapshot)
        SkillActivityCard(snapshot, viewModel, "strengthXp")
    }
}

@Composable
fun SocialScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SkillSummaryCard(snapshot, "socialXp")
        SkillActivityCard(snapshot, viewModel, "socialXp")
    }
}
