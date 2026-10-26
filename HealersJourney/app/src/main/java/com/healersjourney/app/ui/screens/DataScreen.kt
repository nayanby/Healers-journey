package com.healersjourney.app.ui.screens

import androidx.compose.foundation.shape.CircleShape
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.CircularProgressIndicator
import com.healersjourney.app.ui.components.AppTonalButton as FilledTonalButton
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.MaterialTheme
import com.healersjourney.app.ui.components.AppOutlinedButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.healersjourney.app.ui.components.AppTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.data.backup.LocalBackupStore
import com.healersjourney.app.data.sync.DriveSyncRepository
import com.healersjourney.app.ui.DriveUiState
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataScreen(snapshot: GameSnapshot, viewModel: GameViewModel, drive: DriveUiState, onSignIn: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var exporting by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    var confirmingReset by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val lastBackup by viewModel.lastLocalBackup.collectAsState()

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            }.getOrNull()
            if (text.isNullOrBlank()) {
                viewModel.showMessage("Could not read that file.")
            } else {
                viewModel.importJson(text)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "Game feedback") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reward sounds", Modifier.weight(1f))
                Switch(snapshot.state.soundEnabled, { value -> viewModel.runQuiet { it.soundEnabled = value } })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Haptic feedback", Modifier.weight(1f))
                Switch(snapshot.state.hapticsEnabled, { value -> viewModel.runQuiet { it.hapticsEnabled = value } })
            }
        }
        SectionCard(title = "Google Drive") {
            StatLine("Status", if (drive.signedIn) "Signed in" else "Not signed in")
            StatLine(
                "Last sync",
                if (drive.lastSyncAt > 0) SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(drive.lastSyncAt)) else "Never"
            )
            if (!drive.configured) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Drive sync is disabled until an OAuth client id is set in BuildConfig.DRIVE_OAUTH_CLIENT_ID. " +
                        "Create one in Google Cloud Console with the redirect URI shown in build.gradle.kts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            if (drive.busy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(AppLayout.icon), strokeWidth = 2.dp)
                    Spacer(Modifier.width(AppLayout.contentGap))
                    Text("Working…", style = MaterialTheme.typography.bodySmall)
                }
            } else if (!drive.signedIn) {
                Button(onClick = onSignIn, enabled = drive.configured, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign in with Google")
                }
            } else {
                Button(
                    onClick = { viewModel.uploadToDrive() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(AppLayout.icon)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Back up to Drive")
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { viewModel.restoreFromDrive() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(AppLayout.icon)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Restore from Drive")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.signOutOfDrive() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(AppLayout.icon)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sign out")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "The save lives in your Drive app data folder (${DriveSyncRepository.FILE_NAME}), which only this app can read.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = "Backup file") {
            StatLine(
                "Auto-backup",
                if (LocalBackupStore.supported) LocalBackupStore.LOCATION else "Needs Android 10+"
            )
            StatLine(
                "Last snapshot",
                if (lastBackup > 0) SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(lastBackup)) else "Never"
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Snapshots are written to shared storage as you play, so they survive an uninstall or a data wipe. " +
                    "They use the same JSON layout as the browser version, so a backup from either side imports into the other.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.backupNow() }, modifier = Modifier.fillMaxWidth()) { Text("Back up now") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { exporting = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Export backup")
            }
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { pickFile.launch(arrayOf("application/json", "text/plain", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Import from a file") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { importing = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Import pasted JSON")
            }
        }

        SectionCard(title = "Privacy") {
            Text(
                "Your activity and progress are stored on this device. Read how backups and exports are handled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nayanby/Healers-journey/blob/main/PRIVACY.md"))
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Privacy policy") }
        }

        SectionCard(title = "Danger zone", collapsible = true, initiallyExpanded = false) {
            Text(
                "Resetting clears every attribute, log, milestone and achievement and starts a fresh game.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { confirmingReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset all game data", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (exporting) {
        val json = remember { viewModel.exportJson() }
        AlertDialog(
            onDismissRequest = { exporting = false },
            title = { Text("Backup JSON") },
            text = {
                Column {
                    Text(
                        "${json.length} characters. Copy it somewhere safe, or share it as a file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(AppLayout.contentGap))
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(json)) }) { Text("Copy") }
                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "Life RPG backup")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(send, "Share backup"))
                    }) { Text("Share") }
                }
            },
            dismissButton = { TextButton(onClick = { exporting = false }) { Text("Close") } }
        )
    }

    if (importing) {
        AlertDialog(
            onDismissRequest = { importing = false },
            title = { Text("Import backup JSON") },
            text = {
                Column {
                    EmptyHint("Paste the full backup contents. This replaces the current game.")
                    Spacer(Modifier.height(AppLayout.contentGap))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        label = { Text("Backup JSON") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importJson(importText)
                    importText = ""
                    importing = false
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { importing = false }) { Text("Cancel") } }
        )
    }

    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text("Reset everything?") },
            text = { Text("Every attribute, log entry, milestone, achievement and finance figure will be cleared. Export a backup first if you might want it back.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetGame()
                    confirmingReset = false
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmingReset = false }) { Text("Keep my data") } }
        )
    }
}
