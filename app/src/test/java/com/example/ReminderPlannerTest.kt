package com.example

import com.example.menuku.core.utils.ReminderPlanner
import com.example.menuku.data.models.*
import org.junit.Assert.*
import org.junit.Test

class ReminderPlannerTest {
    private fun stock() = StockItem("a", "Telur", 2.0, "butir", StorageLocation.KULKAS,
        "Daging & Telur", "", "", 1)
    private fun meal(enabled: Boolean = true) = MealSchedule("b", "2099-12-01", "Selasa", 1,
        MealSlot.SIANG, reminderTime = "11:30 WIB", reminderEnabled = enabled)

    @Test fun bothTypesArePlannedWithStableDistinctIds() {
        val now = System.currentTimeMillis()
        val plans = ReminderPlanner.build(listOf(stock()), listOf(meal()), UserProfile(), now)
        assertEquals(setOf("PANTRY", "COOKING"), plans.map { it.category }.toSet())
        assertEquals(2, plans.map { it.id }.toSet().size)
        assertTrue(plans.all { it.at > now })
    }
    @Test fun disablingPreferencesCancelsBothTypes() {
        assertTrue(ReminderPlanner.build(listOf(stock()), listOf(meal()),
            UserProfile(cookReminderAlarm = false, earlyExpiryAlert = false), System.currentTimeMillis()).isEmpty())
    }
    @Test fun disabledAndPastMealsAreNotScheduled() {
        assertTrue(ReminderPlanner.build(emptyList(), listOf(meal(false), meal().copy(dateStr = "2024-01-01")),
            UserProfile(), System.currentTimeMillis()).isEmpty())
    }
    @Test fun invalidDatesAndTimesAreRejected() {
        assertNull(ReminderPlanner.mealTime("2026-02-30", "11:30"))
        assertNull(ReminderPlanner.mealTime("2026-12-01", "25:30"))
        assertNull(ReminderPlanner.mealTime("2026-12-01", "11:90"))
        assertNotNull(ReminderPlanner.mealTime("2026-12-01", "11:30 WIB"))
    }
}
