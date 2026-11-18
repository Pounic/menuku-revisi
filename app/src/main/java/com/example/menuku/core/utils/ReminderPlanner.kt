package com.example.menuku.core.utils

import com.example.menuku.data.models.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ReminderPlan(val id: String, val title: String, val message: String, val at: Long, val category: String)

object ReminderPlanner {
    fun mealTime(date: String, time: String): Long? = runCatching {
        val clean = time.removeSuffix(" WIB").trim()
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(date) && Regex("\\d{2}:\\d{2}").matches(clean))
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { isLenient = false }
        format.parse("$date $clean")!!.time
    }.getOrNull()

    fun build(stock: List<StockItem>, meals: List<MealSchedule>, profile: UserProfile, now: Long): List<ReminderPlan> {
        val plans = mutableListOf<ReminderPlan>()
        if (profile.earlyExpiryAlert) stock.forEach { item ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = (item.expiryEpochDay - 2) * StockDates.MILLIS_PER_DAY
            }
            val local = Calendar.getInstance().apply {
                clear(); set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 9, 0)
            }
            val days = item.daysUntilExpiry
            plans += ReminderPlan("expiry-${item.id}-${item.expiryEpochDay}",
                if (days < 0) "Bahan sudah kedaluwarsa" else "Periksa stok bahan dapur",
                "${item.name}: kedaluwarsa ${item.formattedExpiryDate}. Periksa kondisi bahan sebelum digunakan.",
                maxOf(local.timeInMillis, now + 5_000), "PANTRY")
        }
        if (profile.cookReminderAlarm) meals.filter { it.reminderEnabled }.forEach { meal ->
            val at = mealTime(meal.dateStr, meal.reminderTime) ?: return@forEach
            if (at > now) plans += ReminderPlan("meal-${meal.id}-$at", "Waktunya menyiapkan ${meal.slot.label}",
                meal.allRecipes.joinToString { it.title }.ifBlank { "Periksa rencana menu Anda." }, at, "COOKING")
        }
        return plans.sortedBy { it.at }
    }
}
