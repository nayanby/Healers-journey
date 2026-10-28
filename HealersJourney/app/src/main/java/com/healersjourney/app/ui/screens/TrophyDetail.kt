package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.components.*

@Composable
fun TrophyDetail(state: GameState, action: ActionEntry, onDismiss: () -> Unit) {
    val label = Rules.activityLabel(action.skill, action.originalName ?: action.name)
    val count = state.actionHistory.count { it.skill == action.skill && Rules.activityLabel(it.skill, it.originalName ?: it.name).equals(label, true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(label) }, text = {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TrophyImage(action.skill, Modifier.size(200.dp), description = "${Rules.SKILL_LABELS[action.skill]} earned trophy")
            Text(Rules.SKILL_LABELS[action.skill] ?: "Activity", style = MaterialTheme.typography.titleMedium)
            Text("Completed ${Dates.prettyCareDate(action.date)}", style = MaterialTheme.typography.bodyMedium)
            Text("${action.xp} XP · ${if (action.id in state.coinAwardedActionIds) 10 else 0} coins", style = MaterialTheme.typography.titleMedium)
            Text("Completed $count ${if (count == 1) "time" else "times"} so far", style = MaterialTheme.typography.bodySmall)
            Text("Category bonuses are shown separately on the shelf.", style = MaterialTheme.typography.labelSmall)
        }
    }, confirmButton = { AppTextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
fun TrophyCollectionDialog(state: GameState, onDismiss: () -> Unit) {
    var category by remember { mutableStateOf("all") }
    var selected by remember { mutableStateOf<ActionEntry?>(null) }
    selected?.let { TrophyDetail(state, it) { selected = null }; return }
    val entries = state.actionHistory.filter { DailyShowcase.collection(it.skill) != null &&
        it.type in setOf("activity", "study", "hobby", "workout", "other") && (category == "all" || category == it.skill) }.sortedByDescending { it.ts }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Your trophy collection") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (listOf("all") + Rules.SKILL_KEYS).forEach { skill -> FilterChip(category == skill, { category = skill }, label = { Text(Rules.SKILL_LABELS[skill] ?: "All") }) }
            }
            Text("${entries.size} earned trophies", style = MaterialTheme.typography.labelLarge)
            LazyColumn(Modifier.heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (entries.isEmpty()) item { Text("Complete an activity to start your collection.") }
                items(entries, key = { it.id }) { action ->
                    Row(Modifier.fillMaxWidth().clickable { selected = action }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TrophyImage(action.skill, Modifier.size(60.dp))
                        Column(Modifier.weight(1f)) {
                            Text(Rules.activityLabel(action.skill, action.originalName ?: action.name), style = MaterialTheme.typography.bodyMedium)
                            Text("${Dates.prettyCareDate(action.date)} · ${action.xp} XP", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }, confirmButton = { AppTextButton(onClick = onDismiss) { Text("Close") } })
}
