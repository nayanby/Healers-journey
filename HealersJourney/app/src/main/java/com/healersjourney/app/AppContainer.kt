package com.healersjourney.app

import android.content.Context
import com.healersjourney.app.data.backup.LocalBackupStore
import com.healersjourney.app.data.db.HealersDatabase
import com.healersjourney.app.data.repo.GameRepository
import com.healersjourney.app.data.sync.DriveSyncRepository
import com.healersjourney.app.ui.theme.ThemePrefs

class AppContainer(private val context: Context) {
    val database: HealersDatabase by lazy { HealersDatabase.create(context) }
    val repository: GameRepository by lazy { GameRepository(database.gameDao()) }
    val driveSync: DriveSyncRepository by lazy { DriveSyncRepository(context, repository) }
    val localBackups: LocalBackupStore by lazy { LocalBackupStore(context) }
    val themePrefs: ThemePrefs by lazy { ThemePrefs(context) }
}
