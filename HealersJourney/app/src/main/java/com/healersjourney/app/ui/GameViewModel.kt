package com.healersjourney.app.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.healersjourney.app.AppContainer
import com.healersjourney.app.data.backup.LocalBackupStore
import com.healersjourney.app.data.backup.StateJson
import com.healersjourney.app.data.repo.GameRepository
import com.healersjourney.app.data.sync.DriveSyncRepository
import com.healersjourney.app.data.sync.SyncResult
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [GameState] is a mutable aggregate, so a revision counter is emitted alongside it to make
 * Compose recompose after every mutation.
 */
data class GameSnapshot(val state: GameState, val revision: Int)

data class DriveUiState(
    val configured: Boolean = false,
    val signedIn: Boolean = false,
    val lastSyncAt: Long = 0L,
    val busy: Boolean = false
)

class GameViewModel(
    private val repository: GameRepository,
    private val driveSync: DriveSyncRepository,
    private val backups: LocalBackupStore
) : ViewModel() {

    private var current: GameState = GameState()
    private var revision = 0
    private val saveMutex = Mutex()

    private val _snapshot = MutableStateFlow(GameSnapshot(current, 0))
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _drive = MutableStateFlow(DriveUiState())
    val drive: StateFlow<DriveUiState> = _drive.asStateFlow()

    private val _lastLocalBackup = MutableStateFlow(0L)
    val lastLocalBackup: StateFlow<Long> = _lastLocalBackup.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        viewModelScope.launch {
            current = repository.load()
            GameEngine.ensureDefaults(current)
            repository.save(current)
            publish()
            refreshDriveState()
            _lastLocalBackup.value = backups.lastBackupAt
            backups.backupIfDue(current)
            _lastLocalBackup.value = backups.lastBackupAt
            _loaded.value = true
        }
    }

    private fun publish() {
        revision += 1
        _snapshot.value = GameSnapshot(current, revision)
    }

    private fun persist(forceBackup: Boolean = false) {
        viewModelScope.launch {
            saveMutex.withLock { repository.save(current) }
            if (forceBackup) backups.backupNow(current) else backups.backupIfDue(current)
            _lastLocalBackup.value = backups.lastBackupAt
        }
    }

    private fun commit(forceBackup: Boolean = false) {
        publish()
        persist(forceBackup)
    }

    fun showMessage(text: String?) {
        _message.value = text
    }

    /** Applies a mutation that can be rejected, surfacing the rejection as a message. */
    fun run(block: (GameState) -> Outcome) {
        when (val outcome = block(current)) {
            Outcome.Applied -> commit()
            is Outcome.Failure -> showMessage(outcome.message)
        }
    }

    /** Applies a mutation that always succeeds. */
    fun runQuiet(block: (GameState) -> Unit) {
        block(current)
        commit()
    }

    fun exportJson(): String = StateJson.backupEnvelope(current)

    fun backupNow() {
        if (!LocalBackupStore.supported) {
            showMessage("Auto-backup needs Android 10 or newer. Use Export backup instead.")
            return
        }
        viewModelScope.launch {
            val saved = backups.backupNow(current)
            _lastLocalBackup.value = backups.lastBackupAt
            showMessage(
                if (saved) "Snapshot saved to ${LocalBackupStore.LOCATION}."
                else "Could not write to ${LocalBackupStore.LOCATION}."
            )
        }
    }

    fun importJson(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            showMessage("Paste backup JSON first.")
            return
        }
        val imported = runCatching { StateJson.decode(trimmed) }.getOrElse {
            showMessage("Import failed: ${it.message ?: "the file is not valid backup JSON."}")
            return
        }
        GameEngine.ensureDefaults(imported)
        current = imported
        commit(forceBackup = true)
        showMessage("Backup imported successfully.")
    }

    fun resetGame() {
        current = GameEngine.resetGame(GameState())
        commit()
        showMessage("Game data has been reset.")
    }

    // ---- Google Drive ----

    fun driveSignInIntent(): Intent = driveSync.signInIntent()

    fun handleDriveSignInResult(intent: Intent) {
        viewModelScope.launch {
            val error = driveSync.handleSignInResult(intent)
            showMessage(error ?: "Signed in to Google Drive.")
            refreshDriveState()
        }
    }

    fun uploadToDrive() {
        if (!driveSync.isConfigured) {
            showMessage("Add your OAuth client id to BuildConfig.DRIVE_OAUTH_CLIENT_ID first.")
            return
        }
        setDriveBusy(true)
        viewModelScope.launch {
            val result = driveSync.upload(current)
            setDriveBusy(false)
            showMessage(
                when (result) {
                    is SyncResult.Success -> result.message
                    is SyncResult.Failure -> result.message
                    SyncResult.NotSignedIn -> "Sign in to Google Drive first."
                }
            )
            refreshDriveState()
        }
    }

    fun restoreFromDrive() {
        if (!driveSync.isConfigured) {
            showMessage("Add your OAuth client id to BuildConfig.DRIVE_OAUTH_CLIENT_ID first.")
            return
        }
        setDriveBusy(true)
        viewModelScope.launch {
            val (result, state) = driveSync.restoreFromDrive()
            setDriveBusy(false)
            if (state != null) {
                GameEngine.ensureDefaults(state)
                current = state
                commit(forceBackup = true)
            }
            showMessage(
                when (result) {
                    is SyncResult.Success -> result.message
                    is SyncResult.Failure -> result.message
                    SyncResult.NotSignedIn -> "Sign in to Google Drive first."
                }
            )
            refreshDriveState()
        }
    }

    fun signOutOfDrive() {
        driveSync.signOut()
        refreshDriveState()
        showMessage("Signed out of Google Drive.")
    }

    private fun setDriveBusy(busy: Boolean) {
        _drive.value = _drive.value.copy(busy = busy)
    }

    private fun refreshDriveState() {
        _drive.value = DriveUiState(
            configured = driveSync.isConfigured,
            signedIn = driveSync.isSignedIn,
            lastSyncAt = driveSync.lastSyncAt,
            busy = _drive.value.busy
        )
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(container.repository, container.driveSync, container.localBackups) as T
    }
}
