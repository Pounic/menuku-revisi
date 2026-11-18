package com.example.menuku.core.utils

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class ReminderScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("local_reminders", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private fun intent(uid: String, plan: ReminderPlan) = Intent(context, ReminderReceiver::class.java).apply {
        data = Uri.parse("menuku://reminder/${Uri.encode(uid)}/${Uri.encode(plan.id)}")
        putExtra("uid", uid); putExtra("id", plan.id); putExtra("title", plan.title)
        putExtra("message", plan.message); putExtra("category", plan.category)
    }
    private fun pending(uid: String, plan: ReminderPlan): PendingIntent = PendingIntent.getBroadcast(
        context, 0, intent(uid, plan), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun replace(uid: String?, plans: List<ReminderPlan>) {
        val oldUid = prefs.getString("uid", null)
        if (oldUid != null) readPlans().forEach { alarms.cancel(pending(oldUid, it)) }
        if (oldUid != uid) prefs.edit().remove("delivered").apply()
        val rows = JSONArray()
        plans.forEach { rows.put(JSONObject().put("id", it.id).put("title", it.title)
            .put("message", it.message).put("at", it.at).put("category", it.category)) }
        prefs.edit().putString("uid", uid).putString("plans", rows.toString()).apply()
        if (uid != null) schedule(uid, plans)
        else NotificationManagerCompat.from(context).cancelAll()
    }
    private fun schedule(uid: String, plans: List<ReminderPlan>) {
        val delivered = prefs.getStringSet("delivered", emptySet()).orEmpty()
        val now = System.currentTimeMillis()
        plans.filter { it.id !in delivered && (it.category != "COOKING" || it.at > now) }.forEach {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, maxOf(it.at, now + 5_000), pending(uid, it))
        }
    }
    fun restore() {
        val uid = prefs.getString("uid", null) ?: return
        if (FirebaseAuth.getInstance().currentUser?.uid == uid) schedule(uid, readPlans())
        else replace(null, emptyList())
    }
    private fun readPlans(): List<ReminderPlan> = runCatching {
        val rows = JSONArray(prefs.getString("plans", "[]"))
        (0 until rows.length()).map { index -> rows.getJSONObject(index).let {
            ReminderPlan(it.getString("id"), it.getString("title"), it.getString("message"), it.getLong("at"), it.getString("category"))
        } }
    }.getOrDefault(emptyList())
    fun deliver(intent: Intent) {
        val uid = intent.getStringExtra("uid") ?: return
        if (uid != FirebaseAuth.getInstance().currentUser?.uid || uid != prefs.getString("uid", null)) return
        val id = intent.getStringExtra("id") ?: return
        val delivered = prefs.getStringSet("delivered", emptySet()).orEmpty().toMutableSet()
        if (id in delivered) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(
            "kitchen_reminders", "Pengingat dapur", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val message = intent.getStringExtra("message").orEmpty()
        val notification = NotificationCompat.Builder(context, "kitchen_reminders")
            .setSmallIcon(R.drawable.ic_stat_notification).setContentTitle(intent.getStringExtra("title"))
            .setContentText(message).setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(open).setAutoCancel(true).build()
        manager.notify(id, 0, notification)
        delivered.add(id)
        prefs.edit().putStringSet("delivered", delivered.toList().takeLast(500).toSet()).apply()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = ReminderScheduler(context)
        if (intent.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) scheduler.restore()
        else scheduler.deliver(intent)
    }
}
