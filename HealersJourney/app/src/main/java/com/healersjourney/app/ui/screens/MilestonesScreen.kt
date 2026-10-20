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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppButton as Button
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Milestone
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine

@Composable
fun MilestonesScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    GameEngine.ensureMilestones(state)
    val milestones = state.milestones.toList()
    var adding by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Milestone?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(
            title = "Milestones",
            trailing = {
                TextButton(onClick = { adding = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(AppLayout.icon))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        ) {
            StatLine("Completed", "${milestones.count { it.completed }} / ${milestones.size}")
            StatLine("Shown on dashboard", "${milestones.count { it.showOnMain }}")
            Text(
                "Real-life milestones do not grant XP. Tap one to open it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (milestones.isEmpty()) {
            SectionCard(title = "Nothing yet") {
                EmptyHint("Add your first milestone to start tracking real-life goals.")
            }
        }

        milestones.forEach { milestone ->
            SectionCard(title = milestone.title) {
                Text(
                    milestone.desc.ifEmpty { "No description added." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (milestone.completed) "Completed ${Dates.formatMilestoneDate(milestone.completedDate)}"
                    else "Not completed yet",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (milestone.completed) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(AppLayout.contentGap))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Feature on Home", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = milestone.showOnMain,
                        onCheckedChange = { viewModel.run { GameEngine.toggleMilestoneMain(it, milestone.id) } }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { detail = milestone },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (milestone.completed) "View" else "Complete") }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        viewModel.runQuiet { GameEngine.deleteMilestoneById(it, milestone.id) }
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete ${milestone.title}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (adding) {
        AddMilestoneDialog(
            onDismiss = { adding = false },
            onAdd = { title, desc ->
                viewModel.run { GameEngine.addMilestone(it, title, desc) }
                adding = false
            }
        )
    }

    detail?.let { milestone ->
        MilestoneDialog(milestone, onDismiss = { detail = null }) {
            viewModel.run { GameEngine.completeMilestoneById(it, milestone.id) }
            detail = null
        }
    }
}

@Composable
private fun AddMilestoneDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New milestone") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppLayout.contentGap))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = { onAdd(title, desc) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
