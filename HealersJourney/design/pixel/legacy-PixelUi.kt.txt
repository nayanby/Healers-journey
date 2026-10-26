package com.healersjourney.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.*
import com.healersjourney.app.R

/** Shared stepped corners and unfiltered bitmap sampling throughout the game. */
object PixelEdgeShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { 3.dp.toPx() }.coerceAtMost(size.minDimension / 6f)
        val w=size.width;val h=size.height
        return Outline.Generic(Path().apply {
            moveTo(2*s,0f);lineTo(w-2*s,0f);lineTo(w-2*s,s);lineTo(w-s,s);lineTo(w-s,2*s);lineTo(w,2*s)
            lineTo(w,h-2*s);lineTo(w-s,h-2*s);lineTo(w-s,h-s);lineTo(w-2*s,h-s);lineTo(w-2*s,h)
            lineTo(2*s,h);lineTo(2*s,h-s);lineTo(s,h-s);lineTo(s,h-2*s);lineTo(0f,h-2*s)
            lineTo(0f,2*s);lineTo(s,2*s);lineTo(s,s);lineTo(2*s,s);close()
        })
    }
}
@Suppress("UNUSED_PARAMETER") fun PixelShape(radius: Dp): Shape = PixelEdgeShape
@Suppress("UNUSED_PARAMETER") fun PixelShape(percent: Int): Shape = PixelEdgeShape

@Suppress("UNUSED_PARAMETER")
@Composable
fun PixelImage(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center, contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f, colorFilter: ColorFilter? = null, filterQuality: FilterQuality = FilterQuality.None) {
    Image(painter,contentDescription,modifier,alignment,contentScale,alpha,colorFilter)
}

@Composable
fun pixelPainterResource(id: Int): Painter {
    val image = ImageBitmap.imageResource(id)
    return remember(image) { BitmapPainter(image, filterQuality = FilterQuality.None) }
}

@Composable
fun PixelToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(Modifier.size(52.dp,48.dp).toggleable(checked,role = Role.Switch,onValueChange = onCheckedChange),contentAlignment = Alignment.Center) {
        Box(Modifier.size(48.dp,26.dp).clip(PixelEdgeShape)
            .background(if(checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp,MaterialTheme.colorScheme.outlineVariant,PixelEdgeShape).padding(4.dp),
            contentAlignment = if(checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.size(18.dp).background(MaterialTheme.colorScheme.surface,PixelEdgeShape))
        }
    }
}

object PixelArt {
    fun companion(id: String) = when(id) {
        "fox" -> R.drawable.pixel_companion_fox; "cat" -> R.drawable.pixel_companion_cat
        "owl" -> R.drawable.pixel_companion_owl; "bunny" -> R.drawable.pixel_companion_bunny
        "dragon" -> R.drawable.pixel_companion_dragon; "cloud" -> R.drawable.pixel_companion_cloud
        "sprite" -> R.drawable.pixel_companion_sprite; "star" -> R.drawable.pixel_companion_star
        else -> R.drawable.pixel_companion_firefly
    }
    fun scenery(id: String?) = when(id) {
        "sunset" -> R.drawable.pixel_world_sunset; "moonlight" -> R.drawable.pixel_world_moonlight
        "forest" -> R.drawable.pixel_world_forest; "beach" -> R.drawable.pixel_world_beach
        "snow" -> R.drawable.pixel_world_snow; "aurora" -> R.drawable.pixel_world_aurora
        "sky_islands" -> R.drawable.pixel_world_sky_islands; else -> R.drawable.pixel_world_meadow
    }
    fun room(id: String?) = when(id) {
        "sunset" -> R.drawable.pixel_room_sunset; "moonlight" -> R.drawable.pixel_room_moonlight
        "forest" -> R.drawable.pixel_room_forest; "beach" -> R.drawable.pixel_room_beach
        "snow" -> R.drawable.pixel_room_snow; "aurora" -> R.drawable.pixel_room_aurora
        "sky_islands" -> R.drawable.pixel_room_sky_islands; else -> R.drawable.pixel_room_meadow
    }
    fun decor(id: String?) = when(id) {
        "flowers" -> R.drawable.pixel_decor_flowers; "lanterns" -> R.drawable.pixel_decor_lanterns
        "bookshelf" -> R.drawable.pixel_decor_bookshelf; "rug" -> R.drawable.pixel_decor_rug
        "plants" -> R.drawable.pixel_decor_plants; "aquarium" -> R.drawable.pixel_decor_aquarium
        "telescope" -> R.drawable.pixel_decor_telescope; "fireplace" -> R.drawable.pixel_decor_fireplace
        "trophy" -> R.drawable.pixel_decor_trophy; else -> R.drawable.pixel_decor_plain
    }
    fun place(skill: String, stage: Int): Int {
        val variants = when(skill) {
            "medicineXp" -> listOf(R.drawable.pixel_place_medicine_0,R.drawable.pixel_place_medicine_1,R.drawable.pixel_place_medicine_2,R.drawable.pixel_place_medicine_3)
            "strengthXp" -> listOf(R.drawable.pixel_place_strength_0,R.drawable.pixel_place_strength_1,R.drawable.pixel_place_strength_2,R.drawable.pixel_place_strength_3)
            "socialXp" -> listOf(R.drawable.pixel_place_social_0,R.drawable.pixel_place_social_1,R.drawable.pixel_place_social_2,R.drawable.pixel_place_social_3)
            "hobbiesXp" -> listOf(R.drawable.pixel_place_hobbies_0,R.drawable.pixel_place_hobbies_1,R.drawable.pixel_place_hobbies_2,R.drawable.pixel_place_hobbies_3)
            else -> listOf(R.drawable.pixel_place_knowledge_0,R.drawable.pixel_place_knowledge_1,R.drawable.pixel_place_knowledge_2,R.drawable.pixel_place_knowledge_3)
        }
        return variants[stage.coerceIn(0,3)]
    }
}
