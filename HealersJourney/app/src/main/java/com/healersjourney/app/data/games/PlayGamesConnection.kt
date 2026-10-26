package com.healersjourney.app.data.games

import android.app.Activity
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayGamesUiState(
    val busy: Boolean = false,
    val signedIn: Boolean = false,
    val playerName: String? = null,
    val message: String? = null
)

/** Owned by the Activity so authentication never retains an old screen after recreation. */
class PlayGamesConnection(private val activity: Activity) {
    private val mutableState = MutableStateFlow(PlayGamesUiState())
    val state = mutableState.asStateFlow()
    private var requestNumber = 0

    fun refresh() = authenticate(interactive = false)

    fun signIn() = authenticate(interactive = true)

    private fun active(request: Int) =
        request == requestNumber && !activity.isFinishing && !activity.isDestroyed

    private fun authenticate(interactive: Boolean) {
        if (mutableState.value.busy || activity.isDestroyed || activity.isFinishing) return
        val request = ++requestNumber
        mutableState.value = mutableState.value.copy(busy = true, message = null)
        try {
            val client = PlayGames.getGamesSignInClient(activity)
            val task = if (interactive) client.signIn() else client.isAuthenticated()
            // Sign-in can stop the Activity while Google's account UI is visible. A listener
            // scoped to the Activity would be removed onStop and leave the button busy forever.
            task.addOnCompleteListener { result ->
                if (!active(request)) return@addOnCompleteListener
                if (!result.isSuccessful) {
                    failed(result.exception, interactive)
                } else if (result.result.isAuthenticated) {
                    mutableState.value = PlayGamesUiState(signedIn = true)
                    loadPlayerName(request)
                } else {
                    mutableState.value = PlayGamesUiState(message = if (interactive)
                        "Sign-in wasn't completed. You can try again anytime." else null)
                }
            }
        } catch (error: Exception) {
            if (active(request)) failed(error, interactive)
        }
    }

    private fun loadPlayerName(request: Int) {
        try {
            PlayGames.getPlayersClient(activity).currentPlayer.addOnSuccessListener { player ->
                if (active(request) && mutableState.value.signedIn) {
                    mutableState.value = mutableState.value.copy(playerName = player.displayName)
                }
            }
        } catch (_: Exception) {
            // A player-name lookup is optional; authentication has already succeeded.
        }
    }

    private fun failed(error: Exception?, interactive: Boolean) {
        val code = (error as? ApiException)?.statusCode
        Log.w("LifeRpgPlayGames", "Authentication failed: status=$code")
        mutableState.value = PlayGamesUiState(message = if (!interactive) null else when (code) {
            CommonStatusCodes.NETWORK_ERROR -> "Check your internet connection and try again."
            else -> "Couldn't connect to Play Games. Please try again later."
        })
    }
}
