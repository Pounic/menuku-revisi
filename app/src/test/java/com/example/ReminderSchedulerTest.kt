package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.menuku.core.utils.ReminderScheduler
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class ReminderSchedulerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences get() = context.getSharedPreferences("local_reminders", Context.MODE_PRIVATE)
    @Before fun clear() { preferences.edit().clear().commit() }
    @Test fun reopeningSameAccountRetainsDeduplication() {
        val scheduler = ReminderScheduler(context)
        scheduler.replace("owner-a", emptyList())
        preferences.edit().putStringSet("delivered", setOf("expiry-a")).commit()
        scheduler.replace("owner-a", emptyList())
        assertEquals(setOf("expiry-a"), preferences.getStringSet("delivered", emptySet()))
    }
    @Test fun switchingAccountClearsPreviousNotificationHistory() {
        val scheduler = ReminderScheduler(context)
        scheduler.replace("owner-a", emptyList())
        preferences.edit().putStringSet("delivered", setOf("expiry-a")).commit()
        scheduler.replace("owner-b", emptyList())
        assertTrue(preferences.getStringSet("delivered", emptySet()).orEmpty().isEmpty())
    }
}
