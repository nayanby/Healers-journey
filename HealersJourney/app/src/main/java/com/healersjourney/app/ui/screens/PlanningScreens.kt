package com.healersjourney.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
fun CategoryPicker(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Rules.SKILL_KEYS.forEach { skill -> FilterChip(selected == skill, { onSelect(skill) }, label = { Text(Rules.SKILL_LABELS.getValue(skill)) }) }
    }
}

@Composable
fun WeekdayPicker(days: Set<Int>, onChange: (Set<Int>) -> Unit) {
    Text("Repeat on", style = MaterialTheme.typography.labelLarge)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, label ->
            val day = index + 1
            FilterChip(day in days, { onChange(if (day in days) days - day else days + day) },
                label = { Text(label) }, modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun PersonalGoalsSection(snapshot: GameSnapshot, viewModel: GameViewModel, compact: Boolean = false) {
    val state = snapshot.state
    var editing by remember { mutableStateOf<PersonalGoal?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<PersonalGoal?>(null) }
    var commemorative by remember { mutableStateOf<PersonalGoal?>(null) }
    val goals = state.personalGoals.sortedWith(compareBy<PersonalGoal> { it.end < Dates.gameDayKey() }.thenBy { it.end })
    SectionCard(title = "Personal goals", collapsible = compact, initiallyExpanded = !compact,
        trailing = { AppTextButton(onClick = { adding = true }) { Text("Add goal") } }) {
        if (goals.isEmpty()) Text("Give your activities a purpose. Try exercising 12 times this month.", style = MaterialTheme.typography.bodySmall)
        goals.forEach { goal ->
            val count = Planning.goalActions(state, goal).size
            val earned = count >= goal.target
            Row(Modifier.fillMaxWidth().clickable { editing = goal }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TrophyImage(goal.skill, Modifier.size(if (earned) 64.dp else 52.dp).clickable(enabled = earned) { commemorative = goal },
                    description = if (earned) "View earned goal trophy" else "Goal trophy in progress", alpha = if (earned) 1f else .55f)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(goal.title, style = MaterialTheme.typography.titleSmall)
                    Text("${minOf(count, goal.target)} / ${goal.target} · ${goal.start.take(7)}${if (earned) " · Trophy earned" else if (goal.end < Dates.gameDayKey()) " · Ended" else ""}", style = MaterialTheme.typography.labelSmall)
                    SimpleProgress(progress = { (count.toFloat() / goal.target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                }
                if (!compact) AppTextButton(onClick = { deleting = goal }) { Text("Remove") }
            }
        }
        Text("Completions count automatically. Undoing an activity updates its goal and trophy too.", style = MaterialTheme.typography.labelSmall)
    }
    if (adding || editing != null) GoalEditor(state, editing, onDismiss = { adding = false; editing = null }) { title, skill, activity, target, month ->
        viewModel.run { val result = Planning.saveGoal(it, editing?.id, title, skill, activity, target, month)
            if (result is Outcome.Applied) { adding = false; editing = null }; result }
    }
    commemorative?.let { goal ->
        val reached = Planning.goalActions(state, goal).sortedBy { it.ts }.getOrNull(goal.target - 1)
        AlertDialog(onDismissRequest = { commemorative = null }, title = { Text(goal.title) }, text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TrophyImage(goal.skill, Modifier.size(200.dp), description = "Commemorative goal trophy")
                Text("${goal.target} completions · ${goal.start.take(7)}", style = MaterialTheme.typography.titleMedium)
                Text(reached?.let { "Earned ${Dates.prettyCareDate(it.date)}" } ?: "Keep going to earn this trophy.")
            }
        }, confirmButton = { AppTextButton(onClick = { commemorative = null }) { Text("Close") } })
    }
    deleting?.let { goal -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Remove this goal?") },
        text = { Text("Your completed activities and rewards will stay.") }, confirmButton = { AppTextButton(onClick = {
            viewModel.runQuiet { it.personalGoals.removeAll { g -> g.id == goal.id } }; deleting = null
        }) { Text("Remove") } }, dismissButton = { AppTextButton(onClick = { deleting = null }) { Text("Keep") } }) }
}

@Composable
private fun GoalEditor(state: GameState, goal: PersonalGoal?, onDismiss: () -> Unit,
                       onSave: (String, String, String, Int, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf(goal?.title ?: "") }
    var skill by rememberSaveable { mutableStateOf(goal?.skill ?: "strengthXp") }
    var activity by rememberSaveable { mutableStateOf(goal?.activity ?: "") }
    var target by rememberSaveable { mutableStateOf(goal?.target?.toString() ?: "12") }
    var month by rememberSaveable { mutableStateOf(goal?.start?.take(7) ?: Dates.gameDayKey().take(7)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (goal == null) "Set a personal goal" else "Your goal") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(title, { title = it.take(100) }, label = { Text("Goal name") }, singleLine = true)
            CategoryPicker(skill) { skill = it; activity = "" }
            OutlinedTextField(target, { target = it.filter(Char::isDigit).take(4) }, label = { Text("Number of completions") }, singleLine = true)
            OutlinedTextField(month, { month = it.take(7) }, label = { Text("Month (YYYY-MM)") }, singleLine = true)
            Text("Counts activities completed during this calendar month, including earlier completions.", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(activity.isEmpty(), { activity = "" }, label = { Text("All in category") })
                val defs = state.activityDefs[skill]
                (defs?.major.orEmpty() + defs?.other.orEmpty()).distinctBy { it.name }.forEach { def ->
                    val label = Rules.activityLabel(skill, def.name)
                    FilterChip(activity == label, { activity = label }, label = { Text(label) })
                }
            }
            OutlinedTextField(activity, { activity = it.take(100) }, label = { Text("Specific activity (optional)") }, singleLine = true)
            Text("Leave empty for all activities in this category, or match an activity name exactly.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { AppTextButton(onClick = { onSave(title, skill, activity, target.toIntOrNull() ?: 0, month) }) { Text("Save goal") } },
        dismissButton = { AppTextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
fun RecurringTasksSection(snapshot: GameSnapshot, viewModel: GameViewModel) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringTask?>(null) }
    var deleting by remember { mutableStateOf<RecurringTask?>(null) }
    SectionCard("Recurring tasks", collapsible = true, initiallyExpanded = false,
        trailing = { AppTextButton(onClick = { adding = true }) { Text("Add") } }) {
        if (snapshot.state.recurringTasks.isEmpty()) Text("Choose weekdays and let Today fill itself. Nothing is marked done automatically.", style = MaterialTheme.typography.bodySmall)
        snapshot.state.recurringTasks.forEach { rule ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable { editing = rule }.padding(vertical = 8.dp)) {
                    Text(rule.name, style = MaterialTheme.typography.bodyMedium)
                    Text(rule.weekdays.sorted().joinToString(" · ") { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[it - 1] }, style = MaterialTheme.typography.labelSmall)
                }
                AppTextButton(onClick = { deleting = rule }) { Text("Remove") }
            }
        }
        Text("Tap a schedule to edit it. Changes affect open tasks from today onward; completed days stay unchanged.", style = MaterialTheme.typography.labelSmall)
    }
    if (adding || editing != null) RecurringEditor(editing, { adding = false; editing = null }) { name, skill, xp, days ->
        viewModel.run { val result = Planning.saveRecurring(it, editing?.id, name, skill, xp, days)
            if (result is Outcome.Applied) { adding = false; editing = null }; result }
    }
    deleting?.let { rule -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Stop repeating ${rule.name}?") },
        text = { Text("Open scheduled copies from today onward will be removed. Completed tasks and rewards stay.") },
        confirmButton = { AppTextButton(onClick = { viewModel.runQuiet { Planning.removeRecurring(it, rule.id) }; deleting = null }) { Text("Stop repeating") } },
        dismissButton = { AppTextButton(onClick = { deleting = null }) { Text("Keep") } }) }
}

@Composable
private fun RecurringEditor(rule: RecurringTask?, onDismiss: () -> Unit, onSave: (String, String, Int, Set<Int>) -> Unit) {
    var name by rememberSaveable { mutableStateOf(rule?.name ?: "") }
    var skill by rememberSaveable { mutableStateOf(rule?.skill ?: Rules.SKILL_KEYS.first()) }
    var xp by rememberSaveable { mutableStateOf((rule?.xp ?: 20).toString()) }
    var days by remember { mutableStateOf(rule?.weekdays ?: setOf(1, 2, 3, 4, 5)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Recurring task") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it.take(100) }, label = { Text("Task name") }, singleLine = true)
            CategoryPicker(skill) { skill = it }
            OutlinedTextField(xp, { xp = it.filter(Char::isDigit) }, label = { Text("Base XP") }, singleLine = true)
            WeekdayPicker(days) { days = it }
            Text("Starts today. Each scheduled day gets one editable task with the usual rewards.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { AppTextButton(onClick = { onSave(name, skill, xp.toIntOrNull() ?: 0, days) }) { Text("Save schedule") } },
        dismissButton = { AppTextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun FocusCard(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val session = snapshot.state.focusSession ?: return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var cancel by remember { mutableStateOf(false) }
    LaunchedEffect(session) { while (true) { now = System.currentTimeMillis(); delay(500) } }
    val remaining = session.remaining(now)
    val seconds = (remaining + 999) / 1000
    SectionCard("Focus session") {
        Text(Planning.task(snapshot.state, session)?.name ?: "Task no longer available", style = MaterialTheme.typography.titleMedium)
        Text("%02d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.displaySmall)
        SimpleProgress(progress = { 1f - remaining.toFloat() / session.durationMs }, modifier = Modifier.fillMaxWidth())
        Text(if (remaining == 0L) "Session finished. Complete your task to collect its usual rewards." else if (session.running) "One task at a time. Your timer keeps running when you leave this screen." else "Paused. Resume whenever you're ready.", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (remaining == 0L) AppButton(onClick = { viewModel.run { Planning.finishFocus(it) } }) { Text("Finish & complete task") }
            else AppTonalButton(onClick = { viewModel.runQuiet { if (session.running) Planning.pauseFocus(it) else Planning.resumeFocus(it) } }) { Text(if (session.running) "Pause" else "Resume") }
            AppTextButton(onClick = { cancel = true }) { Text("Cancel") }
        }
    }
    if (cancel) AlertDialog(onDismissRequest = { cancel = false }, title = { Text("Cancel this session?") }, text = { Text("The task stays open. No XP or coins are awarded.") },
        confirmButton = { AppTextButton(onClick = { viewModel.runQuiet { it.focusSession = null }; cancel = false }) { Text("Cancel session") } },
        dismissButton = { AppTextButton(onClick = { cancel = false }) { Text("Keep focusing") } })
}

@Composable
fun StartFocusDialog(task: TodoTask, viewModel: GameViewModel, onDismiss: () -> Unit) {
    var minutes by rememberSaveable { mutableStateOf("25") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Focus on ${task.name}") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Minutes (1–180)") }, singleLine = true)
            Text("When time is up, finish the session to complete this task once, with its normal XP and coins.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { AppTextButton(onClick = { viewModel.run { val result = Planning.startFocus(it, task, minutes.toIntOrNull() ?: 0)
        if (result is Outcome.Applied) onDismiss(); result } }) { Text("Start focus") } }, dismissButton = { AppTextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun EditPlannedTaskDialog(task: TodoTask, viewModel: GameViewModel, onRemove: () -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable(task.id) { mutableStateOf(task.name) }
    var skill by rememberSaveable(task.id) { mutableStateOf(task.skill) }
    var xp by rememberSaveable(task.id) { mutableStateOf(task.xp.toString()) }
    var repeat by rememberSaveable { mutableStateOf(false) }
    var days by remember { mutableStateOf(setOf(Planning.weekday(task.date))) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (task.completed) "Completed task" else "Edit task") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (task.completed) Text("${task.name}\nUndo this task before editing its details.") else {
                OutlinedTextField(name, { name = it.take(100) }, label = { Text("Task name") }, singleLine = true)
                CategoryPicker(skill) { skill = it }
                OutlinedTextField(xp, { xp = it.filter(Char::isDigit) }, label = { Text("Base XP") }, singleLine = true)
                if (!task.id.startsWith("todo-repeat-")) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(repeat, { repeat = it }); Text("Repeat on selected weekdays") }
                    if (repeat) WeekdayPicker(days) { days = it }
                } else Text("This edits this day's copy. To change future days, edit its recurring schedule below the task list.", style = MaterialTheme.typography.bodySmall)
            }
            AppTextButton(onClick = onRemove) { Text("Remove task", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { if (!task.completed) AppTextButton(onClick = {
        viewModel.run { state ->
            if (repeat && days.isEmpty()) Outcome.Failure("Choose at least one weekday.") else {
                val result = GameEngine.updateTodoTask(state, task.id, name, skill, xp.toIntOrNull() ?: 0)
                if (result is Outcome.Applied) {
                    if (repeat) Planning.saveRecurring(state, null, name, skill, xp.toInt(), days)
                    onDismiss()
                }; result
            }
        }
    }) { Text("Save") } }, dismissButton = { AppTextButton(onClick = onDismiss) { Text("Close") } })
}
