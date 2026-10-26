package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel

/** Opens either a new-preset form or actions for exactly one existing preset. */
@Composable
fun PresetEditorDialog(skill: String, snapshot: GameSnapshot, viewModel: GameViewModel, initialActivity: ActivityDef? = null, onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(if (initialActivity == null) "form" else "actions") }
    var name by remember { mutableStateOf(initialActivity?.let { Rules.activityLabel(skill, it.name) } ?: "") }
    var xp by remember { mutableStateOf(initialActivity?.xp?.toString() ?: "20") }
    when (mode) {
        "actions" -> AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape, onDismissRequest = onDismiss,
            title = { Text(Rules.activityLabel(skill, initialActivity!!.name)) },
            confirmButton = { TextButton(onClick = { mode = "form" }) { Text("Edit") } },
            dismissButton = { TextButton(onClick = { mode = "delete" }) { Text("Delete") } })
        "delete" -> AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape, onDismissRequest = onDismiss,
            title = { Text("Delete preset?") },
            text = { Text("Delete ${Rules.activityLabel(skill, initialActivity!!.name)}? Previously earned XP will be kept.") },
            confirmButton = { TextButton(onClick = {
                viewModel.run { state ->
                    val result = GameEngine.removeActivityById(state, skill, initialActivity!!.id)
                    if (result is Outcome.Applied) onDismiss()
                    result
                }
            }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
        else -> AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape, onDismissRequest = onDismiss,
            title = { Text(if (initialActivity == null) "Add preset" else "Edit preset") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Activity name") }, singleLine = true)
                OutlinedTextField(xp, { xp = it.filter(Char::isDigit) }, label = { Text("Base XP") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Text("Changes apply to future logs. Earned XP stays the same.", style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { TextButton(enabled = name.isNotBlank() && (xp.toIntOrNull() ?: 0) > 0, onClick = {
                viewModel.run { state ->
                    val savedName = if (initialActivity != null && name.trim() == Rules.activityLabel(skill, initialActivity.name)) initialActivity.name else name
                    val result = if (initialActivity == null) GameEngine.addActivity(state, skill, savedName, xp.toInt())
                        else GameEngine.updateActivityById(state, skill, initialActivity.id, savedName, xp.toInt())
                    if (result is Outcome.Applied) onDismiss()
                    result
                }
            }) { Text("Save") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    }
}
