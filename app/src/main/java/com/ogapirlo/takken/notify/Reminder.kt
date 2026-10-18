package com.ogapirlo.takken.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ogapirlo.takken.MainActivity
import com.ogapirlo.takken.R
import com.ogapirlo.takken.data.ProgressStore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {
    private const val CHANNEL_ID = "daily_reminder"
    private const val REQUEST_CODE = 1001

    fun apply(context: Context) {
        val p = ProgressStore(context).load()
        if (p.reminderEnabled) schedule(context, p.reminderHour, p.reminderMinute) else cancel(context)
    }

    private fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var at = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        if (!at.isAfter(now)) at = at.plusDays(1)
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = context.getSystemService(AlarmManager::class.java)
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, millis, AlarmManager.INTERVAL_DAY, pendingIntent(context))
    }

    private fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "毎日のリマインダー", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "今日の5問をまだ解いていないときにお知らせします"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun notifyIfNeeded(context: Context) {
        val p = ProgressStore(context).load()
        val today = LocalDate.now().toEpochDay()
        if (!p.reminderEnabled || p.doneOn(today)) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        ensureChannel(context)
        val alive = p.streak.current > 0 && p.streak.lastActiveDay == today - 1
        val (title, body) = when {
            alive -> "🔥 ${p.streak.current}日連続中！" to "今日の5問で記録を守ろう。2分で終わります。"
            p.streak.best > 0 -> "また今日から始めよう 💪" to "1日5問だけ。小さな積み重ねが合格への近道です。"
            else -> "今日の5問、やってみよう 📚" to "宅建の過去問ベースの問題を5問だけ。2分でOK！"
        }
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REQUEST_CODE, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.notifyIfNeeded(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) ReminderScheduler.apply(context)
    }
}
