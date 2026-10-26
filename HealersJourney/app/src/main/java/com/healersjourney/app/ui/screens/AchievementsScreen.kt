@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.healersjourney.app.ui.screens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.healersjourney.app.ui.components.AchievementMedal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator

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
    var category by remember { mutableStateOf("Overall") }

    val grouped = Achievements.grouped(state).mapValues { (_, list) ->
        when (filter) {
            AchievementFilter.All -> list
            AchievementFilter.Unlocked -> list.filter { it.unlocked }
            AchievementFilter.Locked -> list.filter { !it.unlocked }
        }
    }.filterValues { it.isNotEmpty() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("$unlocked / ${all.size} achievements earned", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (Achievements.GROUP_ORDER + "Custom").forEach { group ->
                FilterChip(selected = category == group, onClick = { category = group }, label = { Text(group) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AchievementFilter.entries.forEach { option -> FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.label) }) }
        }
        val rows = all.filter { (if(category == "Custom") it.custom else it.group == category) && when(filter) {
            AchievementFilter.All -> true; AchievementFilter.Unlocked -> it.unlocked; AchievementFilter.Locked -> !it.unlocked
        } }
        if(rows.isEmpty()) EmptyHint("No achievements match this filter.")
        rows.forEach { achievement ->
            val tier = all.filter { it.group == achievement.group }.indexOf(achievement).coerceAtLeast(0) % 5
            Row(Modifier.fillMaxWidth().heightIn(min=96.dp).padding(vertical=12.dp), verticalAlignment=Alignment.CenterVertically) {
                AchievementMedal(achievement,tier)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Text(achievement.name,style=MaterialTheme.typography.titleSmall)
                    Text(achievement.desc,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${Rules.formatMetricValue(achievement.current.coerceAtMost(achievement.target))} / ${Rules.formatMetricValue(achievement.target)}" + if(achievement.unlocked) " · Earned" else "",
                        style=MaterialTheme.typography.labelSmall,color=if(achievement.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    if(!achievement.unlocked) LinearProgressIndicator(progress={ (achievement.current / achievement.target.coerceAtLeast(1.0)).toFloat().coerceIn(0f,1f) }, modifier=Modifier.fillMaxWidth().height(3.dp))
                }
                if(achievement.custom) IconButton(onClick={viewModel.runQuiet { GameEngine.removeCustomAchievement(it,achievement.id) }}) {
                    Icon(Icons.Filled.Delete,contentDescription="Delete ${achievement.name}",modifier=Modifier.size(20.dp))
                }
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))
        }
        TextButton(onClick={adding=true}) { Text("Create achievement") }
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

    AlertDialog(
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
