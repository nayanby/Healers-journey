package com.healersjourney.app.ui.components

import com.healersjourney.app.R

/** Shared PNG symbols for skills and ranks; character illustrations remain separate. */
object Art {

    fun badgeFor(title: String): Int = when (title) {
        "Disciplined Seeker" -> R.drawable.pixel_badge_seeker
        "Focused Builder" -> R.drawable.pixel_badge_builder
        "Momentum Keeper" -> R.drawable.pixel_badge_keeper
        "Master of Routine" -> R.drawable.pixel_badge_routine
        "Iron Discipline" -> R.drawable.pixel_badge_iron
        "Relentless Achiever" -> R.drawable.pixel_badge_achiever
        "Legend of Consistency" -> R.drawable.pixel_badge_legend
        else -> R.drawable.pixel_badge_initiate
    }

    fun skillIcon(skill: String): Int = when (skill) {
        "knowledgeXp", "knowledge" -> R.drawable.pixel_study
        "medicineXp", "medicine" -> R.drawable.pixel_medicine
        "strengthXp", "strength" -> R.drawable.pixel_fitness
        "socialXp", "social" -> R.drawable.pixel_social
        "hobbiesXp", "hobbies" -> R.drawable.pixel_hobbies
        else -> R.drawable.pixel_xp
    }

    fun careAvatar(key: String): Int = when (key) {
        "pristine" -> R.drawable.care_pristine
        "grimy" -> R.drawable.care_grimy
        "dirty" -> R.drawable.care_dirty
        else -> R.drawable.care_filthy
    }

    val achievements = R.drawable.pixel_achievement
    val quickCare = R.drawable.pixel_sleep
    val streakStudy = R.drawable.pixel_streak
    val streakWorkout = R.drawable.pixel_streak
    val studyWeek = R.drawable.pixel_week
    val workoutWeek = R.drawable.pixel_week
}
