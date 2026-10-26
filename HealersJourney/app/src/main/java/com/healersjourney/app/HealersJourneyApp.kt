package com.healersjourney.app

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk

class HealersJourneyApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this)
    }
}
