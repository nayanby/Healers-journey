package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
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
    var showAvatars by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "A moment for yourself") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(Art.careAvatar(Rules.careAvatarKey(stats.score))),
                    "View care states", Modifier.size(64.dp).clickable { showAvatars = true })
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stats.rank, style = MaterialTheme.typography.titleLarge)
                    XpProgress(stats.score.toFloat() / GameEngine.CARE_SCORE_MAX,
                        "${stats.score} / ${GameEngine.CARE_SCORE_MAX} care score", MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        SectionCard(title = "Your routines") {
            stats.items.forEachIndexed { index, item ->
                val done = item.last == Dates.todayKey()
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(com.healersjourney.app.ui.components.AppIcons.care(item.key)), null, Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.label, style = MaterialTheme.typography.titleSmall)
                        Text(dueCaption(item, done), style = MaterialTheme.typography.bodySmall,
                            color = if (item.missedUnits > 0 && !done) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.run { GameEngine.completePersonalCare(it, item.key) } }, enabled = !done) {
                        Text(if (done) "Done" else "Log")
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
    if (showAvatars) CareAvatarDialog(stats.score) { showAvatars = false }
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
private fun CareAvatarDialog(score: Int, onDismiss: () -> Unit) {
    val currentKey = Rules.careAvatarKey(score)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Avatar states") },
        text = {
            Column {
                Text(
                    "Your avatar follows the care score out of ${GameEngine.CARE_SCORE_MAX}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                AVATAR_STATES.forEach { (key, label, range) ->
                    val active = key == currentKey
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Art.careAvatar(key)),
                            contentDescription = "$label avatar",
                            modifier = Modifier.size(52.dp),
                            alpha = if (active) 1f else 0.5f
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Score $range",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (active) {
                            Text(
                                "Now",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
