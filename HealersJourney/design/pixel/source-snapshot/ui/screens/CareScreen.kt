package com.healersjourney.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppButton as Button
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.MaterialTheme
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
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.CareItemStatus
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.components.FreshnessRing
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.XpProgress
import com.healersjourney.app.ui.components.freshnessColor

/** Avatar key, rank name and the care-score band it covers, best first. */
private val AVATAR_STATES = listOf(
    Triple("pristine", "Pristine", "51 – 60"),
    Triple("grimy", "Grimy", "41 – 50"),
    Triple("dirty", "Dirty", "21 – 40"),
    Triple("filthy", "Filthy", "0 – 20")
)

@Composable
fun CareScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val stats = GameEngine.personalCareStats(state)
    val context = LocalContext.current
    var showAvatars by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "A moment for yourself") {
            androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WorldCharacter(state, Modifier.size(220.dp)) { showAvatars = true }
            }
            Text(stats.rank, style = MaterialTheme.typography.titleLarge)
            XpProgress(stats.score.toFloat() / GameEngine.CARE_SCORE_MAX,
                "${stats.score} / ${GameEngine.CARE_SCORE_MAX} care score", MaterialTheme.colorScheme.tertiary)
            Text("Tap to compare care states. Your avatar becomes cleaner as routines restore your care score.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard(title = "Your routines") {
            stats.items.forEachIndexed { index, item ->
                val done = item.last == Dates.todayKey()
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(com.healersjourney.app.ui.components.AppIcons.care(item.key)), null, Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.label, style = MaterialTheme.typography.titleSmall)
                        Text("${Dates.prettyCareDate(item.last)} · ${dueCaption(item, done)}", style = MaterialTheme.typography.bodySmall,
                            color = if (item.missedUnits > 0 && !done) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(context, { _, year, month, day ->
                            val selected = Calendar.getInstance().apply { set(year, month, day) }
                            val date = Dates.dateKeyOf(selected)
                            viewModel.run { current ->
                                val result = GameEngine.completePersonalCare(current, item.key, date)
                                if (result is com.healersjourney.app.domain.Outcome.Applied) {
                                    viewModel.showMessage("${item.label} logged for ${Dates.prettyCareDate(date)}")
                                }
                                result
                            }
                        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).apply {
                            setTitle("${item.label}: date completed")
                            datePicker.maxDate = System.currentTimeMillis()
                            show()
                        }
                    }) {
                        Text(if (done) "Edit date" else "Log")
                    }
                }
                if (index < stats.items.lastIndex) androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
        SectionCard(title = "Routine details", collapsible = true, initiallyExpanded = false) {
            Text("Care starts at 60 points. Each missed routine cycle reduces the score by its weight.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            stats.items.forEach { item ->
                Text(item.label, style = MaterialTheme.typography.titleSmall)
                Text("Every ${item.cycleDays} days · Weight ${item.weight} · ${item.loss} points lost · Last done ${Dates.prettyCareDate(item.last)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        }
        SectionCard(title = "Care history", collapsible = true, initiallyExpanded = false) {
            val entries = state.taskLog.filter { it.text.contains("personal care", true) }.take(10)
            if (entries.isEmpty()) Text("Your care check-ins will appear here.", style = MaterialTheme.typography.bodyMedium)
            entries.forEach { Text(it.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 6.dp)) }
        }
    }
    if (showAvatars) CareAvatarDialog(state, stats.score) { showAvatars = false }
}
private fun dueCaption(item: CareItemStatus, doneToday: Boolean): String {
    val left = item.cycleDays - item.days
    return when {
        doneToday -> "Done today"
        left > 0 -> "Due in ${left}d"
        left == 0 -> "Due today"
        else -> "${-left}d over"
    }
}

@Composable
private fun CareAvatarDialog(state: com.healersjourney.app.domain.GameState, score: Int, onDismiss: () -> Unit) {
    var preview by remember { mutableStateOf(score) }
    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape, onDismissRequest = onDismiss, title = { Text("Your character's care") },
        text = {
            Column {
                WorldCharacter(state, Modifier.size(200.dp), previewScore = preview)
                Text("Current state: ${GameEngine.personalCareStats(state).rank}. This preview does not change your care score.",
                    style = MaterialTheme.typography.bodySmall)
                listOf(60 to "Pristine · Score 51–60", 45 to "Grimy · Score 41–50", 30 to "Dirty · Score 21–40", 10 to "Filthy · Score 0–20").forEach { (value, label) ->
                    TextButton(onClick = { preview = value }) { Text(label, style = MaterialTheme.typography.labelMedium) }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}
