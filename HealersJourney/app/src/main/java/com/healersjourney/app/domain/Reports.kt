package com.healersjourney.app.domain

/** Read-only aggregations for the tracker and log screens. */
object Reports {

    data class StudySummary(
        val all: List<ActionEntry>,
        val weeklyHours: Double,
        val monthlyHours: Double,
        val lifetimeHours: Double,
        val weeklyXp: Int,
        val monthlyXp: Int,
        val lifetimeXp: Int,
        val entries: Int,
        val bestStudy: Double,
        val averageStudy: Double
    )

    data class PeriodSummary(
        val actions: List<ActionEntry>,
        val totalXp: Int,
        val xpBySkill: Map<String, Int>,
        val studyHours: Double,
        val sleepHours: Double,
        val studySessions: Int,
        val sleepSessions: Int,
        val workouts: Int
    )

    fun studyActions(state: GameState): List<ActionEntry> =
        state.actionHistory.filter { it.type == "study" || (it.skill == "knowledgeXp" && it.hours > 0) }

    fun studySummary(state: GameState): StudySummary {
        val all = studyActions(state)
        val weekKeys = Dates.currentWeekKeys().toSet()
        val month = Dates.monthKeyOfNow()
        val hoursEntries = state.studyHoursByDate.entries
        val weeklyHours = hoursEntries.filter { weekKeys.contains(it.key) }.sumOf { it.value }
        val monthlyHours = hoursEntries.filter { it.key.startsWith(month) }.sumOf { it.value }
        val lifetimeHours = hoursEntries.sumOf { it.value }
        val entries = hoursEntries.count { it.value > 0 }
        val weeklyXp = all.filter { it.week == Dates.weekKeyOfNow() }.sumOf { it.xp }
        val monthlyXp = all.filter { it.date.startsWith(month) }.sumOf { it.xp }
        val bestStudy = hoursEntries.maxOfOrNull { it.value } ?: 0.0
        return StudySummary(
            all = all,
            weeklyHours = weeklyHours,
            monthlyHours = monthlyHours,
            lifetimeHours = lifetimeHours,
            weeklyXp = weeklyXp,
            monthlyXp = monthlyXp,
            lifetimeXp = all.sumOf { it.xp },
            entries = entries,
            bestStudy = bestStudy,
            averageStudy = if (entries == 0) 0.0 else lifetimeHours / entries
        )
    }

    /** Hours per day-key for the current Monday-first week. */
    fun weeklyStudySeries(state: GameState): List<Pair<String, Double>> {
        val keys = Dates.currentWeekKeys()
        return keys.map { it to (state.studyHoursByDate[it] ?: 0.0) }
    }

    /** Hours per day-key for the current month plus the Monday-first leading blank count. */
    fun monthlyStudyCalendar(state: GameState): Pair<List<Pair<String, Double>>, Int> {
        val year = Dates.yearOfNow()
        val monthZero = Dates.monthOfNowZeroBased()
        val days = Dates.daysInMonth(year, monthZero)
        val month = Dates.monthKeyOfNow()
        val cells = (1..days).map { day ->
            val key = "$month-%02d".format(day)
            key to (state.studyHoursByDate[key] ?: 0.0)
        }
        return cells to Dates.mondayFirstOffset(year, monthZero)
    }

    fun actionsForPeriod(state: GameState, weekly: Boolean): List<ActionEntry> {
        val week = Dates.weekKeyOfNow()
        val month = Dates.monthKeyOfNow()
        return state.actionHistory.filter { if (weekly) it.week == week else it.date.startsWith(month) }
    }

    fun actionsForMonthKey(state: GameState, monthKey: String): List<ActionEntry> =
        state.actionHistory.filter { it.date.startsWith(monthKey) }
            .sortedByDescending { it.date }

    /** Finance log lines belonging to a month, resolved through the action each line points at. */
    fun financeUpdatesForMonthKey(state: GameState, monthKey: String): List<LogEntry> {
        val byId = state.actionHistory.associateBy { it.id }
        return state.financeLog.filter { entry ->
            val action = entry.actionId?.let { byId[it] }
            if (action != null) action.date.startsWith(monthKey) else monthKey.isNotEmpty() && entry.text.contains(monthKey)
        }
    }

    fun summarize(actions: List<ActionEntry>): PeriodSummary {
        val xpBySkill = Rules.SKILL_KEYS.associateWith { skill ->
            actions.filter { it.skill == skill }.sumOf { it.xp }
        }
        return PeriodSummary(
            actions = actions,
            totalXp = actions.sumOf { it.xp },
            xpBySkill = xpBySkill,
            studyHours = actions.filter { it.type == "study" || it.skill == "knowledgeXp" }.sumOf { it.hours },
            sleepHours = actions.filter { it.type == "sleep" || it.skill == "sleep" }.sumOf { it.hours },
            studySessions = actions.count { it.type == "study" },
            sleepSessions = actions.count { it.type == "sleep" || it.skill == "sleep" },
            workouts = actions.count {
                it.type == "workout" || it.skill == "strengthXp" ||
                    Regex("workout|exercise|fitness").containsMatchIn(it.name.lowercase())
            }
        )
    }

    /** Years that actually hold data, newest first, for the monthly log selector. */
    fun availableYears(state: GameState): List<Int> {
        val years = mutableSetOf(Dates.yearOfNow())
        state.actionHistory.forEach { action ->
            action.date.take(4).toIntOrNull()?.let { years.add(it) }
        }
        return years.sortedDescending()
    }

    fun monthKey(year: Int, month: Int) = "$year-%02d".format(month)

    fun actionLabel(action: ActionEntry): String {
        val skill = Rules.SKILL_LABELS[action.skill] ?: action.skill
        val hours = if (action.hours > 0) " • ${GameEngine.numText(action.hours)} h" else ""
        val decay = if (action.decayPercent > 0) " (-${action.decayPercent}%)" else ""
        return "${action.ts} • $skill • ${action.name} • +${action.xp} XP$decay$hours"
    }
}
