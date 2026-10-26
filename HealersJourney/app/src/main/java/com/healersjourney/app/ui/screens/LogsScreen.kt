@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.ActionEntry
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.FriendlyLog
import com.healersjourney.app.domain.LogEntry
import com.healersjourney.app.domain.Reports
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.XpProgress
import com.healersjourney.app.ui.components.skillColor

private enum class LogPeriod(val label: String) {
    Week("This week"), Month("This month"), Picked("Pick a month")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    var period by rememberSaveable { mutableStateOf(LogPeriod.Week) }
    var year by rememberSaveable { mutableStateOf(Dates.yearOfNow()) }
    var month by rememberSaveable { mutableStateOf(Dates.monthOfNowZeroBased() + 1) }
    var pickedKey by rememberSaveable { mutableStateOf(Reports.monthKey(Dates.yearOfNow(), Dates.monthOfNowZeroBased() + 1)) }
    var yearOpen by rememberSaveable { mutableStateOf(false) }
    var monthOpen by rememberSaveable { mutableStateOf(false) }

    val actions = when (period) {
        LogPeriod.Week -> Reports.actionsForPeriod(state, weekly = true)
        LogPeriod.Month -> Reports.actionsForPeriod(state, weekly = false)
        LogPeriod.Picked -> Reports.actionsForMonthKey(state, pickedKey)
    }
    val summary = Reports.summarize(actions)
    val title = when (period) {
        LogPeriod.Week -> "Week of ${Dates.weekKeyOfNow()}"
        LogPeriod.Month -> Dates.monthName(Dates.monthOfNowZeroBased() + 1) + " " + Dates.yearOfNow()
        LogPeriod.Picked -> pickedKey
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "Period") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppLayout.smallGap)) {
                LogPeriod.entries.forEach { option ->
                    FilterChip(
                        selected = option == period,
                        onClick = { period = option },
                        label = { Text(option.label) }
                    )
                }
            }
            if (period == LogPeriod.Picked) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        expanded = yearOpen,
                        onExpandedChange = { yearOpen = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "$year",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearOpen) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = yearOpen, onDismissRequest = { yearOpen = false }) {
                            Reports.availableYears(state).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option") },
                                    onClick = {
                                        year = option
                                        yearOpen = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(AppLayout.contentGap))
                    ExposedDropdownMenuBox(
                        expanded = monthOpen,
                        onExpandedChange = { monthOpen = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = Dates.monthName(month),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthOpen) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = monthOpen, onDismissRequest = { monthOpen = false }) {
                            (1..12).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(Dates.monthName(option)) },
                                    onClick = {
                                        month = option
                                        monthOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(AppLayout.contentGap))
                Button(
                    onClick = { pickedKey = Reports.monthKey(year, month) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Show monthly log") }
            }
        }

        SectionCard(title = title) {
            StatLine("Activities logged", "${summary.actions.size}")
            StatLine("Total XP", "${summary.totalXp}", MaterialTheme.colorScheme.primary)
            StatLine("Learning", "${summary.studySessions} sessions • ${GameEngine.numText(summary.studyHours)} h")
            StatLine("Sleep", "${summary.sleepSessions} nights • ${GameEngine.numText(summary.sleepHours)} h")
            StatLine("Workouts", "${summary.workouts}")
            Spacer(Modifier.height(AppLayout.contentGap))
            val best = summary.xpBySkill.values.maxOrNull() ?: 0
            Rules.SKILL_KEYS.forEach { skill ->
                val xp = summary.xpBySkill[skill] ?: 0
                XpProgress(
                    progress = if (best > 0) xp.toFloat() / best else 0f,
                    label = "${Rules.SKILL_LABELS[skill] ?: skill} • $xp XP",
                    color = skillColor(skill)
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        SectionCard(title = "Activities") {
            if (actions.isEmpty()) {
                EmptyHint("Nothing logged in this period.")
            }
            actions.forEach { action ->
                ActionRow(action) {
                    var removed = false
                    viewModel.runQuiet { removed = GameEngine.undoActionById(it, action.id) }
                    viewModel.showMessage(
                        if (removed) "Undid ${action.name}." else "That action is no longer in history."
                    )
                }
            }
        }

        SectionCard(title = "Activity log", collapsible = true, initiallyExpanded = false) {
            LogList(state.taskLog, state, "task") { index -> undoLog(viewModel, "task", index) }
        }

        SectionCard(title = "Finance log", collapsible = true, initiallyExpanded = false) {
            LogList(state.financeLog, state, "finance") { index -> undoLog(viewModel, "finance", index) }
        }
    }
}

/** Log undo has to run against the live state, so the outcome is captured from inside the mutation. */
private fun undoLog(viewModel: GameViewModel, kind: String, index: Int) {
    var message: String? = "Could not undo this log entry."
    viewModel.runQuiet { state ->
        val outcome = GameEngine.deleteLogEntry(state, kind, index)
        message = if (outcome.removed) outcome.message ?: "Log entry undone." else outcome.message
    }
    viewModel.showMessage(message)
}

@Composable
private fun ActionRow(action: ActionEntry, onUndo: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(FriendlyLog.action(action), style = MaterialTheme.typography.bodyMedium)
            Text(
                "${Dates.prettyCareDate(action.newCareLast ?: action.date)} · ${Rules.SKILL_LABELS[action.skill] ?: action.type.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onUndo) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Undo ${action.name}",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(AppLayout.icon)
            )
        }
    }
}

@Composable
private fun LogList(entries: List<LogEntry>, state: GameState, kind: String, onDelete: (Int) -> Unit) {
    if (entries.isEmpty()) {
        EmptyHint(if (kind == "finance") "No finance updates yet." else "No activity log entries yet.")
        return
    }
    entries.take(40).forEachIndexed { index, entry ->
        Row(
            Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                FriendlyLog.entry(entry, state),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onDelete(index) }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Undo log entry",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(AppLayout.icon)
                )
            }
        }
    }
    if (entries.size > 40) {
        Text(
            "Showing the newest 40 of ${entries.size}.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
