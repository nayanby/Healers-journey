package com.healersjourney.app.ui.components

import com.healersjourney.app.R

/** Shared PNG symbols for skills and ranks; character illustrations remain separate. */
object Art {

    fun badgeFor(title: String): Int = when (title) {
        "Disciplined Seeker" -> R.drawable.level_badge_seeker
        "Focused Builder" -> R.drawable.level_badge_builder
        "Momentum Keeper" -> R.drawable.level_badge_keeper
        "Master of Routine" -> R.drawable.level_badge_routine
        "Iron Discipline" -> R.drawable.level_badge_iron
        "Relentless Achiever" -> R.drawable.level_badge_achiever
        "Legend of Consistency" -> R.drawable.level_badge_legend
        else -> R.drawable.level_badge_initiate
    }

    fun skillIcon(skill: String): Int = when (skill) {
        "knowledgeXp", "knowledge" -> R.drawable.gameart_icon_activity_learning
        "medicineXp", "medicine" -> R.drawable.gameart_icon_activity_work
        "strengthXp", "strength" -> R.drawable.gameart_icon_activity_fitness
        "socialXp", "social" -> R.drawable.gameart_icon_activity_social
        "hobbiesXp", "hobbies" -> R.drawable.gameart_icon_activity_creativity
        else -> R.drawable.color_xp
    }

    fun careAvatar(key: String): Int = when (key) {
        "pristine" -> R.drawable.care_pristine
        "grimy" -> R.drawable.care_grimy
        "dirty" -> R.drawable.care_dirty
        else -> R.drawable.care_filthy
    }

    val achievements = R.drawable.color_achievement
    val quickCare = R.drawable.gameart_icon_activity_sleep
    val streakStudy = R.drawable.color_streak
    val streakWorkout = R.drawable.color_streak
    val studyWeek = R.drawable.color_week
    val workoutWeek = R.drawable.color_week
}
