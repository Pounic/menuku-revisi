package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.menuku.data.repository.SessionPreferences
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class SessionPreferencesTest {
    @Test fun rememberChoicePersistsWithoutSavingPassword() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("session", Context.MODE_PRIVATE).edit().clear().commit()
        SessionPreferences(context).remember(true)
        assertTrue(SessionPreferences(context).rememberMe)
        assertEquals(setOf("remember"), context.getSharedPreferences("session", Context.MODE_PRIVATE).all.keys)
        SessionPreferences(context).remember(false)
        assertFalse(SessionPreferences(context).rememberMe)
    }
}
