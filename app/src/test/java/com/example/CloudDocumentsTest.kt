package com.example

import com.example.menuku.data.models.*
import com.example.menuku.data.repository.CloudDocuments
import org.junit.Assert.*
import org.junit.Test

class CloudDocumentsTest {
    @Test fun stockRoundTripPreservesCustomLocationAndExpiry() {
        val item = StockItem("stock-1", "Beras", 2.5, "kg", StorageLocation("rak-baru", "Rak Beras", "Kering"),
            "Sembako", "18 November 2026", "unused", 4, note = "Tutup rapat", expiryEpochDay = 21000)
        val restored = CloudDocuments.stock(item.id, CloudDocuments.stock(item))
        assertEquals(item.location, restored.location)
        assertEquals(item.expiryEpochDay, restored.expiryEpochDay)
        assertEquals(item.quantity, restored.quantity, 0.0)
        assertEquals(item.note, restored.note)
    }
    @Test fun shoppingRoundTripPreservesPriceOrigin() {
        val item = ShoppingItem("shop-1", "Ayam", 500.0, "gram", "protein", UrgencyLevel.NORMAL,
            estimatedPrice = 22000, priceSource = "Input manual", isPurchased = true)
        val restored = CloudDocuments.shopping(item.id, CloudDocuments.shopping(item))
        assertEquals(item.estimatedPrice, restored.estimatedPrice)
        assertEquals(item.priceSource, restored.priceSource)
        assertTrue(restored.isPurchased)
    }
    @Test fun scheduleRoundTripPreservesDateAndReminderPreference() {
        val recipe = Recipe("recipe-1", "Sup", imageUrl = "")
        val item = MealSchedule("meal-1", "2026-12-01", "Selasa", 1, MealSlot.SIANG, recipe,
            reminderTime = "11:30 WIB", reminderEnabled = false)
        val restored = CloudDocuments.schedule(item.id, CloudDocuments.schedule(item), listOf(recipe))
        assertEquals(item.dateStr, restored.dateStr)
        assertEquals(item.slot, restored.slot)
        assertEquals(item.recipe, restored.recipe)
        assertFalse(restored.reminderEnabled)
    }
    @Test(expected = IllegalStateException::class)
    fun stockWithoutCanonicalDateIsRejected() {
        CloudDocuments.stock("old", mapOf("name" to "Telur", "quantity" to 1, "unit" to "butir", "location" to "kulkas"))
    }
}
