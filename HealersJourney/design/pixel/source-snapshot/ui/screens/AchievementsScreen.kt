@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.healersjourney.app.ui.components.AppTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Achievements
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine

private enum class AchievementFilter(val label: String) {
    All("All"), Unlocked("Unlocked"), Locked("Locked")
}

@Composable
fun AchievementsScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val all = Achievements.all(state)
    val unlocked = all.count { it.unlocked }
    var filter by remember { mutableStateOf(AchievementFilter.All) }
    var adding by remember { mutableStateOf(false) }

    val grouped = Achievements.grouped(state).mapValues { (_, list) ->
        when (filter) {
            AchievementFilter.All -> list
            AchievementFilter.Unlocked -> list.filter { it.unlocked }
            AchievementFilter.Locked -> list.filter { !it.unlocked }
        }
    }.filterValues { it.isNotEmpty() }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "Achievements") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppLayout.illustration)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("$unlocked / ${all.size} unlocked", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${Achievements.GROUP_ORDER.size} built-in groups plus your own",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(AppLayout.contentGap))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppLayout.smallGap)) {
                AchievementFilter.entries.forEach { option ->
                    FilterChip(
                        selected = option == filter,
                        onClick = { filter = option },
                        label = { Text(option.label) }
                    )
                }
            }
        }

        if (grouped.isEmpty()) {
            SectionCard(title = "Nothing to show") {
                EmptyHint("No achievements match this filter.")
            }
        }

        grouped.forEach { (group, list) ->
            SectionCard(title = "$group · ${list.count { it.unlocked }}/${list.size}", collapsible = true, initiallyExpanded = false) {
                list.forEach { achievement ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (achievement.unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (achievement.unlocked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(AppLayout.icon)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(achievement.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                achievement.desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        if (achievement.custom) {
                            IconButton(onClick = {
                                viewModel.runQuiet { GameEngine.removeCustomAchievement(it, achievement.id) }
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete ${achievement.name}",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(AppLayout.icon)
                                )
                            }
                        }
                    }
                }
            }
        }

        SectionCard(title = "Your custom achievements") {
            StatLine("Defined", "${state.customAchievements.size}")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Create a custom achievement")
            }
        }
    }

    if (adding) {
        CustomAchievementDialog(
            state = state,
            onDismiss = { adding = false },
            onAdd = { name, group, metric, target ->
                viewModel.run { GameEngine.addCustomAchievement(it, name, group, metric, target) }
                adding = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAchievementDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("Overall") }
    var metric by remember { mutableStateOf("totalXp") }
    var target by remember { mutableStateOf("100") }
    var groupOpen by remember { mutableStateOf(false) }
    var metricOpen by remember { mutableStateOf(false) }
    val groups = Achievements.GROUP_ORDER + "Custom"
    val metrics = Achievements.METRIC_LABELS.keys.toList()

    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape,
        onDismissRequest = onDismiss,
        title = { Text("Custom achievement") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Achievement name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppLayout.contentGap))
                ExposedDropdownMenuBox(expanded = groupOpen, onExpandedChange = { groupOpen = it }) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = groupOpen, onDismissRequest = { groupOpen = false }) {
                        groups.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    group = option
                                    groupOpen = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppLayout.contentGap))
                ExposedDropdownMenuBox(expanded = metricOpen, onExpandedChange = { metricOpen = it }) {
                    OutlinedTextField(
                        value = Achievements.metricLabel(metric),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tracked metric") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metricOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = metricOpen, onDismissRequest = { metricOpen = false }) {
                        metrics.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(Achievements.metricLabel(option)) },
                                onClick = {
                                    metric = option
                                    metricOpen = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppLayout.contentGap))
                NumberField(
                    value = target,
                    onValueChange = { target = it },
                    label = "Target value",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current: ${Rules.formatMetricValue(Achievements.customMetricValue(state, metric))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, group, metric, target.toIntOrNull() ?: 0) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
