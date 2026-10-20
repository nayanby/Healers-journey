package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.healersjourney.app.ui.components.AppTonalButton as FilledTonalButton
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.skillColor

@Composable
fun TodoScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val view = GameEngine.getTodoListForView(state)
    val done = view.list.count { it.completed }
    val earned = view.list.filter { it.completed }.sumOf { it.xp }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = Dates.todoDisplayDate(view.key)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.runQuiet { GameEngine.showPreviousTodoDay(it) } }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous day")
                }
                Spacer(Modifier.width(4.dp))
                if (view.viewingToday) {
                    Text("Resets at 5 AM", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else FilledTonalButton(
                    onClick = { viewModel.runQuiet { GameEngine.openTodoWindow(it) } },
                    modifier = Modifier.weight(1f)
                ) { Text("Current day") }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { viewModel.runQuiet { GameEngine.showNextTodoDay(it) } }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next day")
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("$done of ${view.list.size} tasks complete · $earned XP earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (!view.viewingToday) {
                Text(
                    "You are viewing a different day. Lists are kept per day and carry over only until 5 AM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (view.list.isNotEmpty()) SectionCard(title = "Tasks") {
            if (view.list.isEmpty()) {
                EmptyHint("Nothing planned for this day yet.")
            }
            view.list.forEach { task ->
                val color = skillColor(task.skill)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = {
                            if (!task.completed) viewModel.run { GameEngine.completeTodoTask(it, task.id) }
                        }
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            task.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                            color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${Rules.SKILL_LABELS[task.skill] ?: task.skill} • +${task.xp} XP" +
                                if (task.completedAt.isNotEmpty()) " • ${task.completedAt}" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                    IconButton(onClick = { viewModel.run { GameEngine.deleteTodoTask(it, task.id) } }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete ${task.name}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        AddTodoCard(initiallyExpanded = view.list.isEmpty()) { name, skill, xp ->
            viewModel.run { GameEngine.addTodoTask(it, name, skill, xp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoCard(initiallyExpanded: Boolean = false, onAdd: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var xp by remember { mutableStateOf("20") }
    var skill by remember { mutableStateOf(Rules.SKILL_KEYS.first()) }
    var menuOpen by remember { mutableStateOf(false) }

    SectionCard(title = "Add a task", collapsible = true, initiallyExpanded = initiallyExpanded) {
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
                label = { Text("Attribute rewarded") },
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberField(value = xp, onValueChange = { xp = it }, label = "XP on completion", modifier = Modifier.weight(1f))
            Spacer(Modifier.width(AppLayout.contentGap))
            Button(onClick = {
                onAdd(name, skill, xp.toIntOrNull() ?: 0)
                name = ""
                xp = "20"
            }) { Text("Add task") }
        }
    }
}
