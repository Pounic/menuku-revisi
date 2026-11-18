package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.example.menuku.data.repository.SessionPreferences
import com.example.menuku.core.utils.ReminderScheduler

class MenuKuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.initializeApp(this) != null) {
            if (BuildConfig.DEBUG) {
                com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
                )
            }
            SessionPreferences(this).restore()
            ReminderScheduler(this).restore()
        }
    }
}
