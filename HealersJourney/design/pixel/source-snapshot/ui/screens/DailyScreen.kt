package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.healersjourney.app.ui.components.AppIcon as Icon
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
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.skillColor

@Composable
fun DailyScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    var addingTask by remember { mutableStateOf(false) }
    val todays = state.actionHistory.filter { it.date == Dates.todayKey() }.take(12)

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(
            title = "Today so far",
            trailing = {
                TextButton(onClick = { addingTask = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(AppLayout.icon))
                    Spacer(Modifier.width(4.dp))
                    Text("Custom")
                }
            }
        ) {
            if (todays.isEmpty()) {
                EmptyHint("Nothing logged yet today. Open an attribute tab to log something.")
            } else {
                todays.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            entry.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (entry.xp > 0) {
                            Text(
                                "+${entry.xp}",
                                style = MaterialTheme.typography.labelLarge,
                                color = skillColor(entry.skill)
                            )
                        }
                    }
                }
            }
        }
    }

    if (addingTask) {
        CustomTaskDialog(onDismiss = { addingTask = false }) { name, skill, xp ->
            viewModel.run { GameEngine.completeCustomTask(it, name, skill, xp) }
            addingTask = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTaskDialog(onDismiss: () -> Unit, onDone: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var xp by remember { mutableStateOf("20") }
    var skill by remember { mutableStateOf(Rules.SKILL_KEYS.first()) }
    var menuOpen by remember { mutableStateOf(false) }

    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape,
        onDismissRequest = onDismiss,
        title = { Text("Custom quest") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppLayout.contentGap))
                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
                    OutlinedTextField(
                        value = Rules.SKILL_LABELS[skill] ?: skill,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Attribute") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        Rules.SKILL_KEYS.forEach { key ->
                            DropdownMenuItem(
                                text = { Text(Rules.SKILL_LABELS[key] ?: key) },
                                onClick = {
                                    skill = key
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppLayout.contentGap))
                NumberField(value = xp, onValueChange = { xp = it }, label = "XP", modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onDone(name, skill, xp.toIntOrNull() ?: 0) }) { Text("Complete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
