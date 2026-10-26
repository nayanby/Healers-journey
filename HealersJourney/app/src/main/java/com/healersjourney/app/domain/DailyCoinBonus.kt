package com.healersjourney.app.domain

/** Cumulative category goals for each game day. Claim keys live with saved coin rewards. */
object DailyCoinBonus {
    data class Goal(
        val id: String,
        val label: String,
        val coins: Int,
        val categoriesRequired: Int,
        val activitiesPerCategory: Int
    )

    val goals = listOf(
        Goal("three_categories", "Complete 3 categories", 30, 3, 1),
        Goal("all_categories", "Complete all 5 categories", 50, 5, 1)
    )

    fun claimKey(day: String, goal: Goal): String = "category_bonus|$day|${goal.id}"

    fun progress(state: GameState, day: String, goal: Goal): Int {
        val counts = DailyShowcase.entries(state, day).groupingBy { it.skill }.eachCount()
        return Rules.SKILL_KEYS.count { (counts[it] ?: 0) >= goal.activitiesPerCategory }
    }

    fun completed(state: GameState, day: String, goal: Goal): Boolean =
        progress(state, day, goal) >= goal.categoriesRequired

    fun earnedToday(state: GameState, day: String): Int =
        DailyShowcase.entries(state, day).count { it.id in state.coinAwardedActionIds } * 10 +
            goals.filter { claimKey(day, it) in state.coinRewardDays }.sumOf { it.coins }

    fun rewardForClaimKey(key: String): Int =
        goals.firstOrNull { key.endsWith("|${it.id}") && key.startsWith("category_bonus|") }?.coins ?: 0
}
