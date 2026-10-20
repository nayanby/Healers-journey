package com.healersjourney.app.data.backup

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.healersjourney.app.domain.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rolling JSON snapshots in the shared [LOCATION] folder. Shared storage survives an uninstall or
 * a data-dir wipe, which the Room database inside the app's own data dir does not, so this is the
 * only copy of a save that outlives the package.
 */
class LocalBackupStore(private val context: Context) {

    companion object {
        const val FOLDER = "HealersJourney"
        const val LOCATION = "Download/$FOLDER"
        private const val NAME_PREFIX = "healers-journey-"
        private const val MIN_INTERVAL_MS = 5 * 60_000L
        private const val KEEP = 20
        private const val PREFS = "local_backup"
        private const val KEY_LAST = "lastBackupAt"

        /** MediaStore.Downloads arrived in Android 10; below that a storage permission would be needed. */
        val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val nameFormat = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US)

    var lastBackupAt: Long = prefs.getLong(KEY_LAST, 0L)
        private set

    suspend fun backupIfDue(state: GameState) {
        if (System.currentTimeMillis() - lastBackupAt < MIN_INTERVAL_MS) return
        backupNow(state)
    }

    suspend fun backupNow(state: GameState): Boolean {
        if (!supported) return false
        return write(StateJson.backupEnvelope(state))
    }

    private suspend fun write(json: String): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext false
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$NAME_PREFIX${nameFormat.format(Date())}.json")
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER/")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = runCatching { resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) }
            .getOrNull() ?: return@withContext false

        val written = runCatching {
            val out = resolver.openOutputStream(uri) ?: return@runCatching false
            out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            true
        }.getOrDefault(false)

        if (!written) {
            runCatching { resolver.delete(uri, null, null) }
            return@withContext false
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        runCatching { resolver.update(uri, values, null, null) }
        prune(resolver)
        lastBackupAt = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST, lastBackupAt).apply()
        true
    }

    private fun prune(resolver: ContentResolver) {
        val ids = mutableListOf<Long>()
        runCatching {
            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                arrayOf("$NAME_PREFIX%"),
                "${MediaStore.Downloads.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val column = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                while (cursor.moveToNext()) ids.add(cursor.getLong(column))
            }
        }
        ids.drop(KEEP).forEach { id ->
            runCatching {
                resolver.delete(
                    ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id),
                    null,
                    null
                )
            }
        }
    }
}
