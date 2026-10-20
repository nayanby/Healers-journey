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
        "Home" -> R.drawable.color_home
        "Bolt" -> R.drawable.color_today
        "MenuBook" -> R.drawable.color_study
        "MedicalServices" -> R.drawable.color_medicine
        "FitnessCenter" -> R.drawable.color_fitness
        "Groups" -> R.drawable.color_social
        "Bedtime" -> R.drawable.color_sleep
        "MusicNote" -> R.drawable.color_hobbies
        "AccountBalanceWallet" -> R.drawable.color_finance
        "Checklist" -> R.drawable.color_todo
        "Sanitizer" -> R.drawable.color_care
        "Flag" -> R.drawable.color_milestone
        "EmojiEvents" -> R.drawable.color_achievement
        "WorkspacePremium" -> R.drawable.color_achievement
        "ReceiptLong" -> R.drawable.color_logs
        "CloudSync" -> R.drawable.color_sync
        "Menu" -> R.drawable.color_menu
        "Edit" -> R.drawable.color_edit
        "Add" -> R.drawable.color_add
        "Delete" -> R.drawable.color_delete
        "Check" -> R.drawable.color_check
        "Lock" -> R.drawable.color_lock
        "ChevronRight" -> R.drawable.color_right
        "KeyboardArrowUp" -> R.drawable.color_up
        "KeyboardArrowDown" -> R.drawable.color_down
        "ArrowBack" -> R.drawable.color_back
        "ArrowForward" -> R.drawable.color_forward
        "CloudUpload" -> R.drawable.color_upload
        "CloudDownload" -> R.drawable.color_download
        "Logout" -> R.drawable.color_logout
        else -> error("Missing PNG icon: ${icon.name}")
    }

    fun care(key: String): Int = when (key) {
        "shower" -> R.drawable.color_shower
        "haircut" -> R.drawable.color_haircut
        "shaving" -> R.drawable.color_shaving
        "manscaping" -> R.drawable.color_manscaping
        "nails" -> R.drawable.color_nails
        else -> R.drawable.color_care
    }
}

@Composable
fun AppIcon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier,
            tint: Color = androidx.compose.material3.LocalContentColor.current) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    androidx.compose.material3.Icon(painter = painterResource(AppIcons.resource(imageVector)),
        contentDescription = contentDescription, modifier = modifier.size(24.dp).graphicsLayer {
            scaleX = if (imageVector.autoMirror && rtl) -1f else 1f
        }.then(Modifier.graphicsLayer { alpha = tint.alpha }), tint = Color.Unspecified)
}
