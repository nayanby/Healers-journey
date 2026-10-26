package com.healersjourney.app.ui.components

import com.healersjourney.app.R

object DailyShowcaseArt {
    fun badgeResource(skill: String): Int = when(skill) {
        "knowledgeXp" -> R.drawable.gameart_badge_learning
        "medicineXp" -> R.drawable.gameart_badge_work
        "strengthXp" -> R.drawable.gameart_badge_fitness
        "socialXp" -> R.drawable.gameart_badge_social
        "hobbiesXp" -> R.drawable.gameart_badge_creativity
        else -> R.drawable.gameart_badge_learning
    }

    fun resource(skill: String): Int = when(skill) {
        "knowledgeXp" -> R.drawable.gameart_reward_learning
        "medicineXp" -> R.drawable.gameart_reward_work
        "strengthXp" -> R.drawable.gameart_reward_fitness
        "socialXp" -> R.drawable.gameart_reward_social
        "hobbiesXp" -> R.drawable.gameart_reward_creativity
        "sleep" -> R.drawable.gameart_icon_activity_sleep
        else -> R.drawable.gameart_icon_care_grooming
    }
}
