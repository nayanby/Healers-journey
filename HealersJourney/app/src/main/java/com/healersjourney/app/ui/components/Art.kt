package com.healersjourney.app.ui.components

import com.healersjourney.app.R

/** Shared PNG symbols for skills and ranks; character illustrations remain separate. */
object Art {

    fun badgeFor(title: String): Int = when (title) {
        "Disciplined Seeker" -> R.drawable.color_badge_seeker
        "Focused Builder" -> R.drawable.color_badge_builder
        "Momentum Keeper" -> R.drawable.color_badge_keeper
        "Master of Routine" -> R.drawable.color_badge_routine
        "Iron Discipline" -> R.drawable.color_badge_iron
        "Relentless Achiever" -> R.drawable.color_badge_achiever
        "Legend of Consistency" -> R.drawable.color_badge_legend
        else -> R.drawable.color_badge_initiate
    }

    fun skillIcon(skill: String): Int = when (skill) {
        "knowledgeXp", "knowledge" -> R.drawable.color_study
        "medicineXp", "medicine" -> R.drawable.color_medicine
        "strengthXp", "strength" -> R.drawable.color_fitness
        "socialXp", "social" -> R.drawable.color_social
        "hobbiesXp", "hobbies" -> R.drawable.color_hobbies
        else -> R.drawable.color_xp
    }

    fun careAvatar(key: String): Int = when (key) {
        "pristine" -> R.drawable.care_pristine
        "grimy" -> R.drawable.care_grimy
        "dirty" -> R.drawable.care_dirty
        else -> R.drawable.care_filthy
    }

    val achievements = R.drawable.color_achievement
    val quickCare = R.drawable.color_sleep
    val streakStudy = R.drawable.color_streak
    val streakWorkout = R.drawable.color_streak
    val studyWeek = R.drawable.color_week
    val workoutWeek = R.drawable.color_week
}
