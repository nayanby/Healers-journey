package com.healersjourney.app.ui.screens

import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.healersjourney.app.ui.components.PixelEdgeShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.HorizontalDivider
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.healersjourney.app.ui.components.AppTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Milestone
import com.healersjourney.app.domain.Reports
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.components.FreshnessRing
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.skillColor

@Composable
fun RenameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape,
        onDismissRequest = onDismiss,
        title = { Text("Player name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") }
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MilestoneDialog(milestone: Milestone, onDismiss: () -> Unit, onComplete: () -> Unit) {
    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape,
        onDismissRequest = onDismiss,
        title = { Text(milestone.title) },
        text = {
            Column {
                Text(milestone.desc.ifEmpty { "No description added." }, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(AppLayout.contentGap))
                if (milestone.completed) {
                    AssistChip(onClick = {}, label = { Text("Completed ${Dates.formatMilestoneDate(milestone.completedDate)}") })
                } else {
                    AssistChip(onClick = {}, label = { Text("Pending") })
                }
            }
        },
        confirmButton = {
            Button(onClick = onComplete, enabled = !milestone.completed) {
                Text(if (milestone.completed) "Completed" else "Complete milestone")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
