package com.healersjourney.app.domain

import java.util.Calendar

data class PersonalGoal(val id: String, val title: String, val skill: String, val activity: String,
                        val target: Int, val start: String, val end: String)
data class RecurringTask(val id: String, val name: String, val skill: String, val xp: Int,
                         val weekdays: Set<Int>, val start: String)
data class FocusSession(val taskId: String, val day: String, val durationMs: Long,
                        val remainingMs: Long, val endsAt: Long, val running: Boolean) {
    fun remaining(now: Long = System.currentTimeMillis()): Long =
        (if (running) endsAt - now else remainingMs).coerceIn(0L, durationMs)
}

/** Progress always comes from actual history, so undo and deletion stay consistent. */
object Planning {
    fun goalActions(state: GameState, goal: PersonalGoal): List<ActionEntry> = state.actionHistory.filter {
        DailyShowcase.collection(it.skill) != null && it.type in setOf("activity", "study", "hobby", "workout", "other") &&
            it.date >= goal.start && it.date <= goal.end && it.skill == goal.skill &&
            (goal.activity.isBlank() || Rules.activityLabel(it.skill, it.originalName ?: it.name).equals(goal.activity, true))
    }.distinctBy { it.id }

    fun saveGoal(state: GameState, id: String?, title: String, skill: String, activity: String,
                 target: Int, month: String): Outcome {
        if (title.isBlank() || skill !in Rules.SKILL_KEYS || target !in 1..1000 ||
            !month.matches(Regex("\\d{4}-(0[1-9]|1[0-2])"))) return Outcome.Failure("Add a title, category, valid month and a target from 1 to 1,000.")
        val start = "$month-01"
        val cal = Dates.keyToCal(start) ?: return Outcome.Failure("Choose a valid month.")
        val end = "$month-${cal.getActualMaximum(Calendar.DAY_OF_MONTH)}"
        val goal = PersonalGoal(id ?: Ids.make("goal"), title.trim().take(100), skill, activity.trim().take(100), target, start, end)
        state.personalGoals.removeAll { it.id == goal.id }
        state.personalGoals.add(goal)
        return Outcome.Applied
    }

    fun weekday(day: String): Int = ((Dates.keyToCal(day)?.get(Calendar.DAY_OF_WEEK) ?: 1) + 5) % 7 + 1
    fun occurrenceId(rule: RecurringTask, day: String) = "todo-repeat-${rule.id}-$day"

    fun materialize(state: GameState, day: String, tasks: MutableList<TodoTask>) {
        state.recurringTasks.filter { day >= it.start && weekday(day) in it.weekdays }.forEach { rule ->
            val id = occurrenceId(rule, day)
            if (state.scheduledOccurrences.add(id) && tasks.none { it.id == id || (it.skill == rule.skill && it.name.equals(rule.name, true)) }) {
                tasks.add(TodoTask(id, rule.name, rule.skill, rule.xp, day))
            }
        }
    }

    fun saveRecurring(state: GameState, id: String?, name: String, skill: String, xp: Int, days: Set<Int>): Outcome {
        if (name.isBlank() || skill !in Rules.SKILL_KEYS || xp !in 1..10000 || days.isEmpty() || days.any { it !in 1..7 })
            return Outcome.Failure("Enter a task, choose weekdays and use 1–10,000 base XP.")
        val rule = RecurringTask(id ?: Ids.make("repeat"), name.trim().take(100), skill, xp, days, Dates.gameDayKey())
        removeRecurring(state, rule.id)
        state.recurringTasks.add(rule)
        materialize(state, state.todoDate, state.todoTasks)
        state.todoHistory.filterKeys { it >= rule.start }.forEach { (day, tasks) -> materialize(state, day, tasks) }
        return Outcome.Applied
    }

    fun removeRecurring(state: GameState, id: String) {
        state.recurringTasks.removeAll { it.id == id }
        val prefix = "todo-repeat-$id-"
        (listOf(state.todoTasks) + state.todoHistory.values).forEach { tasks ->
            val removed = tasks.filter { task -> task.id.startsWith(prefix) && !task.completed && task.date >= Dates.gameDayKey() && task.id != state.focusSession?.taskId }.map { it.id }.toSet()
            tasks.removeAll { task -> task.id.startsWith(prefix) && !task.completed && task.date >= Dates.gameDayKey() && task.id != state.focusSession?.taskId }
            state.scheduledOccurrences.removeAll(removed)
        }
    }

    fun task(state: GameState, session: FocusSession): TodoTask? =
        (if (session.day == state.todoDate) state.todoTasks else state.todoHistory[session.day].orEmpty()).firstOrNull { it.id == session.taskId }

    fun startFocus(state: GameState, task: TodoTask, minutes: Int, now: Long = System.currentTimeMillis()): Outcome {
        if (state.focusSession != null) return Outcome.Failure("Finish or cancel the current focus session first.")
        if (state.todoTasks.none { it.id == task.id && !it.completed }) return Outcome.Failure("Choose an open task from today.")
        if (task.completed || task.date != Dates.gameDayKey() || minutes !in 1..180)
            return Outcome.Failure("Choose an open task for today and 1–180 minutes.")
        val duration = minutes * 60_000L
        state.focusSession = FocusSession(task.id, task.date, duration, duration, now + duration, true)
        return Outcome.Applied
    }

    fun pauseFocus(state: GameState, now: Long = System.currentTimeMillis()) {
        state.focusSession = state.focusSession?.let { it.copy(remainingMs = it.remaining(now), running = false) }
    }
    fun resumeFocus(state: GameState, now: Long = System.currentTimeMillis()) {
        state.focusSession = state.focusSession?.let { if (it.running) it else it.copy(endsAt = now + it.remainingMs, running = true) }
    }
    fun finishFocus(state: GameState, now: Long = System.currentTimeMillis()): Outcome {
        val session = state.focusSession ?: return Outcome.Failure("No focus session is running.")
        if (session.remaining(now) > 0) return Outcome.Failure("Your focus session is still running.")
        GameEngine.ensureTodoTasks(state)
        val tasks = if (session.day == state.todoDate) state.todoTasks else state.todoHistory[session.day]
            ?: return Outcome.Failure("This task no longer exists. Cancel the session.")
        val result = GameEngine.completePlannedTask(state, tasks, session.taskId)
        if (result is Outcome.Applied) state.focusSession = null
        return result
    }
}
