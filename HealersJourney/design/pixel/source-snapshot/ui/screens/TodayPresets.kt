package com.healersjourney.app.ui.screens

import androidx.compose.animation.animateContentSize
import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.healersjourney.app.R
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.*

/** Shares the configured activities and completion path used by each category screen. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayPresets(snapshot: GameSnapshot, viewModel: GameViewModel, initialSkill: String? = null) {
    var editingSkill by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPreset by remember { mutableStateOf<ActivityDef?>(null) }
    editingSkill?.let { PresetEditorDialog(it, snapshot, viewModel, initialActivity = selectedPreset) { editingSkill = null; selectedPreset = null } }
    val state = snapshot.state
    var expandedSkill by rememberSaveable(initialSkill) { mutableStateOf(initialSkill?.takeIf { it in Rules.SKILL_KEYS }) }
    SectionCard(title = "All activities", collapsible = true, initiallyExpanded = initialSkill != null) {
        Text("Choose another activity, or hold a preset to edit it.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Rules.SKILL_KEYS.forEach { skill ->
            val expanded = expandedSkill == skill
            val label = Rules.SKILL_LABELS.getValue(skill)
            Column(Modifier.fillMaxWidth().animateContentSize()) {
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp)
                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                    .clickable(role = Role.Button) { expandedSkill = if (expanded) null else skill }
                    .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(Art.skillIcon(skill)), null, Modifier.size(34.dp))
                    Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Image(painterResource(if (expanded) R.drawable.pixel_up else R.drawable.pixel_down), null, Modifier.size(18.dp))
                }
                if (expanded) {
                    val activities = listOf("major", "other").flatMap { list ->
                        GameEngine.defsFor(state, skill, list).mapIndexed { index, definition -> Triple(list, index, definition) }
                    }
                    if (activities.isEmpty()) EmptyHint("No presets yet. Add your first activity below.")
                    activities.forEach { (list, index, definition) ->
                        val name = Rules.activityLabel(skill, definition.name)
                        val (xp, decay) = GameEngine.activityXpPreview(state, skill, definition.xp)
                        val logged = state.actionHistory.count {
                            it.date == Dates.gameDayKey() && it.skill == skill && it.name == definition.name && it.type == "activity"
                        }
                        Row(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClickLabel = "Edit or delete preset", onLongClick = { selectedPreset = definition; editingSkill = skill }).padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                Text("+$xp XP" + (if (decay > 0) " · $decay% reduced" else "") +
                                    (if (logged > 0) " · Logged $logged× today" else ""),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            AppTonalButton(onClick = {
                                viewModel.run { current ->
                                    val reward = GameEngine.activityXpPreview(current, skill, definition.xp).first
                                    val result = GameEngine.completeActivity(current, skill, list, index)
                                    if (result is Outcome.Applied) viewModel.showMessage("$name logged · +$reward XP")
                                    result
                                }
                            }) { Text(if (logged > 0) "Log again" else "Log") }
                        }
                    }
                    AppTextButton(onClick = { selectedPreset = null; editingSkill = skill }) { Text("Add preset") }
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (skill != Rules.SKILL_KEYS.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
