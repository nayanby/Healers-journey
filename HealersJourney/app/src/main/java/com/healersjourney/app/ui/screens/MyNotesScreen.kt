package com.healersjourney.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.*
import com.healersjourney.app.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun MyNotesScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    var editingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var removing by remember { mutableStateOf<ThoughtEntry?>(null) }
    val state = snapshot.state
    val day = editingDate
    if (day != null) {
        val saved = state.thoughts.firstOrNull { it.date == day }
        var draft by rememberSaveable(day) { mutableStateOf(saved?.text.orEmpty()) }
        val latestDraft = rememberUpdatedState(draft)
        val latestState = rememberUpdatedState(state)
        fun save() { if (latestState.value.thoughts.firstOrNull { it.date == day }?.text.orEmpty() != latestDraft.value)
            viewModel.runQuiet { Thoughts.save(it, day, latestDraft.value) } }
        LaunchedEffect(day, draft) { delay(600); save() }
        DisposableEffect(day) { onDispose { save() } }
        BackHandler { save(); editingDate = null }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(Dates.prettyCareDate(day), style = MaterialTheme.typography.titleLarge)
                    Text(if (saved?.text.orEmpty() == draft) "Saved on this device" else "Saving…", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AppTextButton(onClick = { save(); editingDate = null }) { Text("Done") }
            }
            OutlinedTextField(draft, { draft = it.take(50_000) }, placeholder = { Text("Write your notes…") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp, max = 560.dp), shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("My notes", style = MaterialTheme.typography.titleLarge)
                Text("A place for what you want to remember.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppButton(onClick = { editingDate = Dates.todayKey() }) {
                Text(if (state.thoughts.any { it.date == Dates.todayKey() }) "Write today" else "New note")
            }
        }
        if (state.thoughts.isEmpty()) SectionCard("Start a note") {
            Text("Type whatever you want to remember. Each note is named by its date and saved automatically.", style = MaterialTheme.typography.bodyMedium)
        }
        state.thoughts.sortedByDescending { it.date }.forEach { entry ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().clickable { editingDate = entry.date }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(Dates.prettyCareDate(entry.date), style = MaterialTheme.typography.titleSmall)
                        Text(entry.text.ifBlank { "Empty note" }, style = MaterialTheme.typography.bodyMedium, maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { removing = entry }) { Icon(Icons.Filled.DeleteOutline, "Delete note for ${entry.date}") }
                }
            }
        }
    }
    removing?.let { entry -> AlertDialog(onDismissRequest = { removing = null }, title = { Text("Delete this note?") },
        text = { Text("Your note from ${Dates.prettyCareDate(entry.date)} will be removed.") },
        confirmButton = { AppTextButton(onClick = { viewModel.runQuiet { it.thoughts.removeAll { thought -> thought.id == entry.id } }; removing = null }) { Text("Delete") } },
        dismissButton = { AppTextButton(onClick = { removing = null }) { Text("Keep") } }) }
}
