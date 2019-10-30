package com.droid.wizmusic

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.droid.wizmusic.db.WizMusicDatabase

class Application : Application() {

    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}