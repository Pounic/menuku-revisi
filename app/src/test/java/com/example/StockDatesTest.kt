package com.example

import com.example.menuku.data.models.StockDates
import com.example.menuku.data.models.StockItem
import com.example.menuku.data.models.StorageLocation
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class StockDatesTest {
    private fun stock(day: Long) = StockItem(
        id = "date-test", name = "Telur", quantity = 2.0, unit = "butir",
        location = StorageLocation.KULKAS, category = "Daging & Telur",
        purchaseDate = "", expiryDate = "old label", remainingDays = 999,
        expiryEpochDay = day
    )

    @Test fun remainingDaysComeFromDateInsteadOfLegacyLabel() {
        val today = StockDates.todayEpochDay()
        assertEquals(2, stock(today + 2).daysUntilExpiry)
        assertTrue(stock(today + 2).isUrgent)
        assertFalse(stock(today + 3).isUrgent)
        assertEquals(-1, stock(today - 1).daysUntilExpiry)
        assertEquals("Kedaluwarsa hari ini", stock(today).expiryStatus)
    }

    @Test fun changingQuantityPreservesExpiryDate() {
        val item = stock(StockDates.todayEpochDay() + 5)
        val edited = item.copy(quantity = 10.0, location = StorageLocation.FREEZER)
        assertEquals(item.expiryEpochDay, edited.expiryEpochDay)
        assertEquals(item.formattedExpiryDate, edited.formattedExpiryDate)
    }

    @Test fun formattingDoesNotShiftDatesWithLocalTimeZone() {
        val original = TimeZone.getDefault()
        try {
            for (zone in listOf("Asia/Jakarta", "America/Los_Angeles", "Pacific/Auckland")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                assertEquals("01 Januari 1970", StockDates.format(0))
            }
        } finally { TimeZone.setDefault(original) }
    }
}
