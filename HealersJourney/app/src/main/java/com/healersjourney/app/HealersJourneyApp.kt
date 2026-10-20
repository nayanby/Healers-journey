package com.healersjourney.app

import android.app.Application

class HealersJourneyApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
