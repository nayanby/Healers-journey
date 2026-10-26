package com.healersjourney.app.ui.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import com.healersjourney.app.R

/** Decode artwork at a sensible texture size; typography keeps normal rendering. */
@Composable
fun gameArtPainter(id: Int, maxPixels: Int = 512): BitmapPainter {
    val resources = LocalContext.current.resources
    val bitmap = remember(resources, id, maxPixels) {
        val key = (id.toLong() shl 32) or maxPixels.toLong()
        synchronized(artCache) {
            artCache.get(key) ?: run {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, id, bounds)
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxPixels) sample *= 2
                BitmapFactory.decodeResource(resources, id, BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inScaled = false
                }).asImageBitmap().also { artCache.put(key, it) }
            }
        }
    }
    return remember(bitmap) { BitmapPainter(bitmap, filterQuality = FilterQuality.None) }
}

private val artCache = object : LruCache<Long, ImageBitmap>(16 * 1024 * 1024) {
    override fun sizeOf(key: Long, value: ImageBitmap): Int = value.width * value.height * 4
}

object GameArt {
    fun careAvatar(id: String?, character: String?, careState: String): Int = when ("${character ?: "male"}|${id ?: "traveler"}|$careState") {
        "male|traveler|grimy" -> R.drawable.gameart_avatar_male_traveler_grimy
        "male|traveler|dirty" -> R.drawable.gameart_avatar_male_traveler_dirty
        "male|traveler|filthy" -> R.drawable.gameart_avatar_male_traveler_filthy
        "male|forest_cloak|grimy" -> R.drawable.gameart_avatar_male_forest_cloak_grimy
        "male|forest_cloak|dirty" -> R.drawable.gameart_avatar_male_forest_cloak_dirty
        "male|forest_cloak|filthy" -> R.drawable.gameart_avatar_male_forest_cloak_filthy
        "male|cozy_knit|grimy" -> R.drawable.gameart_avatar_male_cozy_knit_grimy
        "male|cozy_knit|dirty" -> R.drawable.gameart_avatar_male_cozy_knit_dirty
        "male|cozy_knit|filthy" -> R.drawable.gameart_avatar_male_cozy_knit_filthy
        "male|ocean_explorer|grimy" -> R.drawable.gameart_avatar_male_ocean_explorer_grimy
        "male|ocean_explorer|dirty" -> R.drawable.gameart_avatar_male_ocean_explorer_dirty
        "male|ocean_explorer|filthy" -> R.drawable.gameart_avatar_male_ocean_explorer_filthy
        "male|sun_cape|grimy" -> R.drawable.gameart_avatar_male_sun_cape_grimy
        "male|sun_cape|dirty" -> R.drawable.gameart_avatar_male_sun_cape_dirty
        "male|sun_cape|filthy" -> R.drawable.gameart_avatar_male_sun_cape_filthy
        "male|ranger|grimy" -> R.drawable.gameart_avatar_male_ranger_grimy
        "male|ranger|dirty" -> R.drawable.gameart_avatar_male_ranger_dirty
        "male|ranger|filthy" -> R.drawable.gameart_avatar_male_ranger_filthy
        "male|winter_coat|grimy" -> R.drawable.gameart_avatar_male_winter_coat_grimy
        "male|winter_coat|dirty" -> R.drawable.gameart_avatar_male_winter_coat_dirty
        "male|winter_coat|filthy" -> R.drawable.gameart_avatar_male_winter_coat_filthy
        "male|royal_cape|grimy" -> R.drawable.gameart_avatar_male_royal_cape_grimy
        "male|royal_cape|dirty" -> R.drawable.gameart_avatar_male_royal_cape_dirty
        "male|royal_cape|filthy" -> R.drawable.gameart_avatar_male_royal_cape_filthy
        "male|starlight_robes|grimy" -> R.drawable.gameart_avatar_male_starlight_robes_grimy
        "male|starlight_robes|dirty" -> R.drawable.gameart_avatar_male_starlight_robes_dirty
        "male|starlight_robes|filthy" -> R.drawable.gameart_avatar_male_starlight_robes_filthy
        "male|golden_knight|grimy" -> R.drawable.gameart_avatar_male_golden_knight_grimy
        "male|golden_knight|dirty" -> R.drawable.gameart_avatar_male_golden_knight_dirty
        "male|golden_knight|filthy" -> R.drawable.gameart_avatar_male_golden_knight_filthy
        "female|traveler|grimy" -> R.drawable.gameart_avatar_female_traveler_grimy
        "female|traveler|dirty" -> R.drawable.gameart_avatar_female_traveler_dirty
        "female|traveler|filthy" -> R.drawable.gameart_avatar_female_traveler_filthy
        "female|forest_cloak|grimy" -> R.drawable.gameart_avatar_female_forest_cloak_grimy
        "female|forest_cloak|dirty" -> R.drawable.gameart_avatar_female_forest_cloak_dirty
        "female|forest_cloak|filthy" -> R.drawable.gameart_avatar_female_forest_cloak_filthy
        "female|cozy_knit|grimy" -> R.drawable.gameart_avatar_female_cozy_knit_grimy
        "female|cozy_knit|dirty" -> R.drawable.gameart_avatar_female_cozy_knit_dirty
        "female|cozy_knit|filthy" -> R.drawable.gameart_avatar_female_cozy_knit_filthy
        "female|ocean_explorer|grimy" -> R.drawable.gameart_avatar_female_ocean_explorer_grimy
        "female|ocean_explorer|dirty" -> R.drawable.gameart_avatar_female_ocean_explorer_dirty
        "female|ocean_explorer|filthy" -> R.drawable.gameart_avatar_female_ocean_explorer_filthy
        "female|sun_cape|grimy" -> R.drawable.gameart_avatar_female_sun_cape_grimy
        "female|sun_cape|dirty" -> R.drawable.gameart_avatar_female_sun_cape_dirty
        "female|sun_cape|filthy" -> R.drawable.gameart_avatar_female_sun_cape_filthy
        "female|ranger|grimy" -> R.drawable.gameart_avatar_female_ranger_grimy
        "female|ranger|dirty" -> R.drawable.gameart_avatar_female_ranger_dirty
        "female|ranger|filthy" -> R.drawable.gameart_avatar_female_ranger_filthy
        "female|winter_coat|grimy" -> R.drawable.gameart_avatar_female_winter_coat_grimy
        "female|winter_coat|dirty" -> R.drawable.gameart_avatar_female_winter_coat_dirty
        "female|winter_coat|filthy" -> R.drawable.gameart_avatar_female_winter_coat_filthy
        "female|royal_cape|grimy" -> R.drawable.gameart_avatar_female_royal_cape_grimy
        "female|royal_cape|dirty" -> R.drawable.gameart_avatar_female_royal_cape_dirty
        "female|royal_cape|filthy" -> R.drawable.gameart_avatar_female_royal_cape_filthy
        "female|starlight_robes|grimy" -> R.drawable.gameart_avatar_female_starlight_robes_grimy
        "female|starlight_robes|dirty" -> R.drawable.gameart_avatar_female_starlight_robes_dirty
        "female|starlight_robes|filthy" -> R.drawable.gameart_avatar_female_starlight_robes_filthy
        "female|golden_knight|grimy" -> R.drawable.gameart_avatar_female_golden_knight_grimy
        "female|golden_knight|dirty" -> R.drawable.gameart_avatar_female_golden_knight_dirty
        "female|golden_knight|filthy" -> R.drawable.gameart_avatar_female_golden_knight_filthy
        else -> avatar(id, character)
    }

    fun avatar(id: String?, character: String? = "male"): Int = if (character == "female") when(id) {
        "traveler" -> R.drawable.gameart_avatar_female_traveler
        "forest_cloak" -> R.drawable.gameart_avatar_female_forest_cloak
        "cozy_knit" -> R.drawable.gameart_avatar_female_cozy_knit
        "ocean_explorer" -> R.drawable.gameart_avatar_female_ocean_explorer
        "sun_cape" -> R.drawable.gameart_avatar_female_sun_cape
        "ranger" -> R.drawable.gameart_avatar_female_ranger
        "winter_coat" -> R.drawable.gameart_avatar_female_winter_coat
        "royal_cape" -> R.drawable.gameart_avatar_female_royal_cape
        "starlight_robes" -> R.drawable.gameart_avatar_female_starlight_robes
        "golden_knight" -> R.drawable.gameart_avatar_female_golden_knight
        else -> R.drawable.gameart_avatar_female_traveler
    } else when(id) {
        "traveler" -> R.drawable.gameart_avatar_traveler
        "forest_cloak" -> R.drawable.gameart_avatar_forest_cloak
        "cozy_knit" -> R.drawable.gameart_avatar_cozy_knit
        "ocean_explorer" -> R.drawable.gameart_avatar_ocean_explorer
        "sun_cape" -> R.drawable.gameart_avatar_sun_cape
        "ranger" -> R.drawable.gameart_avatar_ranger
        "winter_coat" -> R.drawable.gameart_avatar_winter_coat
        "royal_cape" -> R.drawable.gameart_avatar_royal_cape
        "starlight_robes" -> R.drawable.gameart_avatar_starlight_robes
        "golden_knight" -> R.drawable.gameart_avatar_golden_knight
        else -> R.drawable.gameart_avatar_traveler
    }

    fun companion(id: String?): Int = when(id) {
        "firefly" -> R.drawable.gameart_companion_firefly
        "sprite" -> R.drawable.gameart_companion_sprite
        "star" -> R.drawable.gameart_companion_star
        "fox" -> R.drawable.gameart_companion_fox
        "bunny" -> R.drawable.gameart_companion_bunny
        "cat" -> R.drawable.gameart_companion_cat
        "owl" -> R.drawable.gameart_companion_owl
        "cloud" -> R.drawable.gameart_companion_cloud
        "dragon" -> R.drawable.gameart_companion_dragon
        else -> R.drawable.gameart_companion_firefly
    }

    fun backdrop(id: String?): Int = when(id) {
        "garden_room" -> R.drawable.gameart_backdrop_garden
        "reading_room" -> R.drawable.gameart_backdrop_reading
        "ocean_room" -> R.drawable.gameart_backdrop_ocean
        "lantern_room" -> R.drawable.gameart_backdrop_lantern
        "workshop_room" -> R.drawable.gameart_backdrop_workshop
        "winter_room" -> R.drawable.gameart_backdrop_winter
        "royal_room" -> R.drawable.gameart_backdrop_royal
        "celestial_hall" -> R.drawable.gameart_backdrop_celestial
        "golden_hall" -> R.drawable.gameart_backdrop_golden
        else -> R.drawable.gameart_backdrop_starter
    }

    fun item(slot: String, id: String, character: String? = "male"): Int = when(slot) {
        "outfit" -> avatar(id, character)
        "companion" -> companion(id)
        "backdrop" -> backdrop(id)
        else -> avatar(id, character)
    }
}
