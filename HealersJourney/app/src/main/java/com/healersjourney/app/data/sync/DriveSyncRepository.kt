package com.healersjourney.app.data.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.healersjourney.app.BuildConfig
import com.healersjourney.app.data.backup.StateJson
import com.healersjourney.app.data.repo.GameRepository
import com.healersjourney.app.domain.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

sealed interface SyncResult {
    data class Success(val message: String) : SyncResult
    data class Failure(val message: String) : SyncResult
    data object NotSignedIn : SyncResult
}

/**
 * Google Drive sync against the app-private `appDataFolder`, so the save file never appears in
 * the user's Drive listing and needs no shared-file permission. Sign-in uses AppAuth's
 * authorization-code flow with PKCE.
 */
class DriveSyncRepository(
    private val context: Context,
    private val repository: GameRepository
) {

    companion object {
        const val FILE_NAME = "healers_journey_save.json"
        private const val PREFS = "drive_auth"
        private const val KEY_AUTH_STATE = "authState"
        private const val KEY_LAST_SYNC = "lastSyncAt"
        private const val PLACEHOLDER_CLIENT_ID = "REPLACE_WITH_OAUTH_CLIENT_ID"
        private const val SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val DRIVE_FILES = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(AUTH_ENDPOINT),
        Uri.parse(TOKEN_ENDPOINT)
    )

    var authState: AuthState? = null
        private set

    init {
        authState = prefs.getString(KEY_AUTH_STATE, null)?.let { saved ->
            runCatching { AuthState.jsonDeserialize(saved) }.getOrNull()
        }
    }

    val isSignedIn: Boolean get() = authState?.isAuthorized == true

    val lastSyncAt: Long get() = prefs.getLong(KEY_LAST_SYNC, 0L)

    /** False while the OAuth client id is still the build placeholder. */
    val isConfigured: Boolean get() = BuildConfig.DRIVE_OAUTH_CLIENT_ID != PLACEHOLDER_CLIENT_ID

    fun signInIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            BuildConfig.DRIVE_OAUTH_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(BuildConfig.DRIVE_OAUTH_REDIRECT)
        )
            .setScope(SCOPE)
            .build()
        return AuthorizationService(context).getAuthorizationRequestIntent(request)
    }

    /** Exchanges the redirect result for tokens. Returns an error message, or null on success. */
    suspend fun handleSignInResult(intent: Intent): String? {
        val response = AuthorizationResponse.fromIntent(intent)
        if (response == null) {
            return AuthorizationException.fromIntent(intent)?.errorDescription ?: "Sign-in was cancelled."
        }
        val service = AuthorizationService(context)
        return try {
            suspendCancellableCoroutine { cont ->
                service.performTokenRequest(response.createTokenExchangeRequest()) { _, exception ->
                    if (exception != null) {
                        cont.resume(exception.errorDescription ?: "Could not exchange the authorization code.")
                    } else {
                        authState = AuthState(response, exception)
                        persistAuthState()
                        cont.resume(null)
                    }
                }
            }
        } finally {
            service.dispose()
        }
    }

    fun signOut() {
        authState = null
        prefs.edit().remove(KEY_AUTH_STATE).remove(KEY_LAST_SYNC).apply()
    }

    suspend fun upload(state: GameState): SyncResult {
        if (!isSignedIn) return SyncResult.NotSignedIn
        val token = freshAccessToken() ?: return SyncResult.NotSignedIn
        return withContext(Dispatchers.IO) {
            runCatching {
                val body = StateJson.backupEnvelope(state).toByteArray(Charsets.UTF_8)
                val fileId = findRemoteFileId(token)
                if (fileId == null) createRemoteFile(token, body) else writeRemoteFile(token, fileId, body)
                markSynced()
                SyncResult.Success("Saved to Google Drive.")
            }.getOrElse { SyncResult.Failure(it.message ?: "Upload failed.") }
        }
    }

    /** Raw JSON of the Drive save file, or null when nothing is stored yet. */
    suspend fun download(): String? {
        if (!isSignedIn) return null
        val token = freshAccessToken() ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val fileId = findRemoteFileId(token) ?: return@runCatching null
                http("$DRIVE_FILES/$fileId?alt=media", "GET", token).body
            }.getOrNull()
        }
    }

    /** Replaces local progress with the Drive copy and returns the restored state. */
    suspend fun restoreFromDrive(): Pair<SyncResult, GameState?> {
        if (!isSignedIn) return SyncResult.NotSignedIn to null
        val json = download() ?: return SyncResult.Failure("No save file found on Google Drive yet.") to null
        return runCatching {
            val state = StateJson.decode(json)
            markSynced()
            SyncResult.Success("Restored from Google Drive.") to state
        }.getOrElse { SyncResult.Failure(it.message ?: "The Drive save file could not be read.") to null }
    }

    private suspend fun freshAccessToken(): String? {
        val state = authState ?: return null
        val service = AuthorizationService(context)
        return try {
            val token = suspendCancellableCoroutine { cont ->
                state.performActionWithFreshTokens(service) { accessToken, _, exception ->
                    cont.resume(if (exception != null) null else accessToken)
                }
            }
            if (token != null) {
                authState = state
                persistAuthState()
            }
            token
        } finally {
            service.dispose()
        }
    }

    private fun markSynced() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    private fun persistAuthState() {
        val json = authState?.jsonSerializeString()
        prefs.edit().apply {
            if (json == null) remove(KEY_AUTH_STATE) else putString(KEY_AUTH_STATE, json)
        }.apply()
    }

    private data class HttpResponse(val code: Int, val body: String?)

    private fun findRemoteFileId(token: String): String? {
        val response = http(
            "$DRIVE_FILES?spaces=appDataFolder&fields=files(id,name)&pageSize=100",
            "GET",
            token
        )
        val files = JSONObject(response.body ?: "{}").optJSONArray("files") ?: return null
        for (i in 0 until files.length()) {
            val file = files.optJSONObject(i) ?: continue
            if (file.optString("name") == FILE_NAME) return file.optString("id")
        }
        return null
    }

    private fun createRemoteFile(token: String, body: ByteArray) {
        val metadata = JSONObject()
            .put("name", FILE_NAME)
            .put("parents", JSONArray().put("appDataFolder"))
            .toString()
        val created = http("$DRIVE_FILES?fields=id", "POST", token, metadata, "application/json; charset=UTF-8")
        if (created.code !in 200..299) throw IllegalStateException(created.body ?: "Could not create the Drive file.")
        val id = JSONObject(created.body ?: "{}").optString("id")
        if (id.isEmpty()) throw IllegalStateException("Drive did not return a file id.")
        writeRemoteFile(token, id, body)
    }

    private fun writeRemoteFile(token: String, fileId: String, body: ByteArray) {
        val response = http("$DRIVE_UPLOAD/$fileId?uploadType=media", "PATCH", token, rawBody = body, contentType = "application/json; charset=UTF-8")
        if (response.code !in 200..299) {
            throw IllegalStateException(response.body ?: "Could not write the Drive file.")
        }
    }

    private fun http(
        url: String,
        method: String,
        token: String,
        textBody: String? = null,
        contentType: String? = null,
        rawBody: ByteArray? = null
    ): HttpResponse {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 20_000
            connection.readTimeout = 30_000
            val payload = rawBody ?: textBody?.toByteArray(Charsets.UTF_8)
            if (payload != null) {
                connection.doOutput = true
                if (contentType != null) connection.setRequestProperty("Content-Type", contentType)
                connection.outputStream.use { it.write(payload) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(code, readBody(stream))
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(stream: InputStream?): String? =
        stream?.use { it.readBytes().toString(Charsets.UTF_8) }
}
