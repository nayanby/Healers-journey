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
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import com.healersjourney.app.R

/** One PNG per meaning across navigation and actions; original artwork colors are preserved. */
object AppIcons {
    fun resource(icon: ImageVector): Int = when (icon.name.substringAfterLast('.')) {
        "Home" -> R.drawable.pixel_home
        "Bolt" -> R.drawable.pixel_today
        "MenuBook" -> R.drawable.pixel_study
        "MedicalServices" -> R.drawable.pixel_medicine
        "FitnessCenter" -> R.drawable.pixel_fitness
        "Groups" -> R.drawable.pixel_social
        "Bedtime" -> R.drawable.pixel_sleep
        "MusicNote" -> R.drawable.pixel_hobbies
        "AccountBalanceWallet" -> R.drawable.pixel_finance
        "Checklist" -> R.drawable.pixel_todo
        "Sanitizer" -> R.drawable.pixel_care
        "Flag" -> R.drawable.pixel_milestone
        "EmojiEvents" -> R.drawable.pixel_achievement
        "WorkspacePremium" -> R.drawable.pixel_achievement
        "ReceiptLong" -> R.drawable.pixel_logs
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
        "shower" -> R.drawable.pixel_shower
        "haircut" -> R.drawable.pixel_haircut
        "shaving" -> R.drawable.pixel_shaving
        "manscaping" -> R.drawable.pixel_manscaping
        "nails" -> R.drawable.pixel_nails
        else -> R.drawable.pixel_care
    }
}

@Composable
fun AppIcon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier,
            tint: Color = androidx.compose.material3.LocalContentColor.current) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    PixelImage(painter = painterResource(AppIcons.resource(imageVector)),
        contentDescription = contentDescription, modifier = modifier.size(24.dp).graphicsLayer {
            scaleX = if (imageVector.autoMirror && rtl) -1f else 1f
        }.then(Modifier.graphicsLayer { alpha = tint.alpha }), colorFilter = null)
}
