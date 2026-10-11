package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.RoutineTask
import com.example.receiver.RoutineAlarmReceiver

object NotificationHelper {

    const val CHANNEL_ID = "routine_persistent_channel"
    const val CHANNEL_NAME = "Routine Reminders & Alarms"

    const val ACTION_TASK_DUE = "com.example.routinemate.ACTION_TASK_DUE"
    const val ACTION_SNOOZE_PICKER = "com.example.routinemate.ACTION_SNOOZE_PICKER"
    const val ACTION_COMPLETE_FROM_NOTIFICATION = "com.example.routinemate.ACTION_COMPLETE_FROM_NOTIFICATION"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Persistent routine alarms that require user completion or rescheduling"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showPersistentDueNotification(context: Context, task: RoutineTask) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Content intent: open app directly to Task Due Dialog
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_TASK_DUE
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Done (Complete)
        val doneIntent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_FROM_NOTIFICATION
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 100000).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Reschedule (Open app for new time selection)
        val snoozeIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SNOOZE_PICKER
            putExtra(EXTRA_TASK_ID, task.id)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val snoozePendingIntent = PendingIntent.getActivity(
            context,
            (task.id + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ Routine Due: ${task.title}")
            .setContentText("Scheduled for ${task.time} • Tap to complete or reschedule")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🔔 '${task.title}' is due right now (${task.time}).\n\nDid you finish it? This alert will remain until you open the app to mark Completed or choose a new time.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // CANNOT be dismissed by swiping!
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Done",
                donePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "⏳ Reschedule",
                snoozePendingIntent
            )
            .build()

        manager.notify(task.id.toInt(), notification)
    }

    fun dismissNotification(context: Context, taskId: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(taskId.toInt())
    }
}
