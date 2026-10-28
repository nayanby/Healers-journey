package com.healersjourney.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.healersjourney.app.R

object TrophyArt {
    fun resource(skill: String): Int = when (skill) {
        "medicineXp" -> R.drawable.gameart_trophy_work
        "strengthXp" -> R.drawable.gameart_trophy_fitness
        "socialXp" -> R.drawable.gameart_trophy_social
        "hobbiesXp" -> R.drawable.gameart_trophy_creativity
        else -> R.drawable.gameart_trophy_learning
    }
}

/** Transparent trophy PNGs share the existing bounded bitmap cache. */
@Composable
fun TrophyImage(skill: String, modifier: Modifier = Modifier, description: String? = null, alpha: Float = 1f) {
    Image(gameArtPainter(TrophyArt.resource(skill)), description, modifier, alpha = alpha)
}
