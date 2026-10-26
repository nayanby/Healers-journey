package com.healersjourney.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.healersjourney.app.R

/** One PNG per meaning across navigation and actions; original artwork colors are preserved. */
object AppIcons {
    fun resource(icon: ImageVector): Int = when (icon.name.substringAfterLast('.')) {
        "Home" -> R.drawable.gameart_icon_nav_world
        "Bolt" -> R.drawable.gameart_icon_nav_quests
        "MenuBook" -> R.drawable.gameart_icon_activity_learning
        "MedicalServices" -> R.drawable.gameart_icon_activity_work
        "FitnessCenter" -> R.drawable.gameart_icon_activity_fitness
        "Groups" -> R.drawable.gameart_icon_activity_social
        "Bedtime" -> R.drawable.gameart_icon_activity_sleep
        "MusicNote" -> R.drawable.gameart_icon_activity_creativity
        "AccountBalanceWallet" -> R.drawable.pixel_finance
        "Checklist" -> R.drawable.gameart_icon_nav_quests
        "Sanitizer" -> R.drawable.pixel_care
        "Flag" -> R.drawable.pixel_milestone
        "EmojiEvents" -> R.drawable.pixel_achievement
        "WorkspacePremium" -> R.drawable.pixel_achievement
        "ReceiptLong" -> R.drawable.gameart_icon_nav_journal
        "CloudSync" -> R.drawable.pixel_sync
        "Menu" -> R.drawable.pixel_menu
        "Edit" -> R.drawable.pixel_edit
        "Add" -> R.drawable.pixel_add
        "Delete" -> R.drawable.pixel_delete
        "Check" -> R.drawable.pixel_check
        "Lock" -> R.drawable.pixel_lock
        "ChevronRight" -> R.drawable.pixel_right
        "KeyboardArrowUp" -> R.drawable.pixel_up
        "KeyboardArrowDown" -> R.drawable.pixel_down
        "ArrowBack" -> R.drawable.pixel_back
        "ArrowForward" -> R.drawable.pixel_forward
        "CloudUpload" -> R.drawable.pixel_upload
        "CloudDownload" -> R.drawable.pixel_download
        "Logout" -> R.drawable.pixel_logout
        else -> error("Missing PNG icon: ${icon.name}")
    }

    fun care(key: String): Int = when (key) {
        "shower" -> R.drawable.gameart_icon_care_shower
        "haircut" -> R.drawable.gameart_icon_care_haircut
        "shaving" -> R.drawable.gameart_icon_care_shaving
        "teeth" -> R.drawable.gameart_icon_care_teeth
        "hair_wash" -> R.drawable.gameart_icon_care_hair_wash
        "skincare" -> R.drawable.gameart_icon_care_skincare
        "sunscreen" -> R.drawable.gameart_icon_care_sunscreen
        "floss" -> R.drawable.gameart_icon_care_floss
        "manscaping" -> R.drawable.gameart_icon_care_grooming
        "nails" -> R.drawable.gameart_icon_care_nails
        else -> R.drawable.pixel_care
    }
}

@Composable
fun AppIcon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier,
            tint: Color = androidx.compose.material3.LocalContentColor.current) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    androidx.compose.material3.Icon(painter = gameArtPainter(AppIcons.resource(imageVector)),
        contentDescription = contentDescription, modifier = modifier.size(24.dp).graphicsLayer {
            scaleX = if (imageVector.autoMirror && rtl) -1f else 1f
        }.then(Modifier.graphicsLayer { alpha = tint.alpha }), tint = Color.Unspecified)
}
