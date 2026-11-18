package com.example.menuku.data.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Calendar dates encoded as UTC days, independent of time of day or daylight saving. */
object StockDates {
    const val MILLIS_PER_DAY = 86_400_000L
    fun todayEpochDay(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        return utc.timeInMillis / MILLIS_PER_DAY
    }
    fun format(day: Long): String = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(day * MILLIS_PER_DAY))
    fun remainingLabel(days: Int): String = when {
        days < 0 -> "Kedaluwarsa ${-days} hari lalu"
        days == 0 -> "Kedaluwarsa hari ini"
        else -> "$days hari lagi"
    }
}
