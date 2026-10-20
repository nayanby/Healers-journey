package com.healersjourney.app.domain

data class SleepSummary(
    val entries: Int,
    val lifetimeHours: Double,
    val weeklyHours: Double,
    val monthlyHours: Double,
    val monthlyEntries: Int,
    val lifetimeXp: Int,
    val weeklyXp: Int,
    val monthlyXp: Int,
    val averageSleep: Double,
    val bestSleep: Double
)

object SleepStats {

    private fun sleepActions(state: GameState) =
        state.actionHistory.filter { it.skill == "sleep" || it.type == "sleep" }

    fun of(state: GameState): SleepSummary {
        val all = sleepActions(state)
        val week = Dates.weekKeyOfNow()
        val month = Dates.monthKeyOfNow()
        val weekly = all.filter { it.week == week }
        val monthly = all.filter { it.month == month || it.date.startsWith(month) }

        val sumHours = { list: List<ActionEntry> -> list.sumOf { it.hours } }
        val sumXp = { list: List<ActionEntry> -> list.sumOf { it.xp } }

        val lifetimeHours = sumHours(all)
        return SleepSummary(
            entries = all.size,
            lifetimeHours = lifetimeHours,
            weeklyHours = sumHours(weekly),
            monthlyHours = sumHours(monthly),
            monthlyEntries = monthly.size,
            lifetimeXp = sumXp(all),
            weeklyXp = sumXp(weekly),
            monthlyXp = sumXp(monthly),
            averageSleep = if (all.isEmpty()) 0.0 else lifetimeHours / all.size,
            bestSleep = all.maxOfOrNull { it.hours } ?: 0.0
        )
    }

    /** Hours per day-key for the current Monday-first week (chart). */
    fun weeklySeries(state: GameState): List<Pair<String, Double>> {
        val map = mutableMapOf<String, Double>()
        Dates.currentWeekKeys().forEach { map[it] = 0.0 }
        sleepActions(state).filter { map.containsKey(it.date) }
            .forEach { map[it.date] = (map[it.date] ?: 0.0) + it.hours }
        return Dates.currentWeekKeys().map { it to (map[it] ?: 0.0) }
    }

    /** Hours per day-key for the current month, Monday-first aligned (calendar). */
    fun monthlySeries(state: GameState): Pair<List<Pair<String, Double>>, Int> {
        val year = Dates.yearOfNow()
        val monthZero = Dates.monthOfNowZeroBased()
        val days = Dates.daysInMonth(year, monthZero)
        val offset = Dates.mondayFirstOffset(year, monthZero)
        val map = linkedMapOf<String, Double>()
        (1..days).forEach { d -> map["${Dates.monthKeyOfNow()}-%02d".format(d)] = 0.0 }
        sleepActions(state).filter { map.containsKey(it.date) }
            .forEach { map[it.date] = (map[it.date] ?: 0.0) + it.hours }
        return map.toList() to offset
    }

    /** Hours per sleep action date across all history (for last-sleep lookups). */
    fun hoursByDate(state: GameState): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        sleepActions(state).forEach { map[it.date] = (map[it.date] ?: 0.0) + it.hours }
        return map
    }
}
