package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.model.RoutineTask
import com.example.receiver.RoutineAlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoutineAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "RoutineAlarmScheduler"
        const val ACTION_ROUTINE_TRIGGER = "com.example.routinemate.ACTION_ROUTINE_TRIGGER"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    fun scheduleTaskAlarm(task: RoutineTask) {
        if (task.isCompleted) return

        val triggerTimeMs = parseDateTimeToMillis(task.date, task.time)
        val now = System.currentTimeMillis()

        // If trigger time is already in the past by more than 1 minute, don't schedule alarm,
        // or trigger immediately if within recent window
        if (triggerTimeMs <= now) {
            Log.d(TAG, "Task ${task.id} time is in the past: ${task.date} ${task.time}")
            return
        }

        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            action = ACTION_ROUTINE_TRIGGER
            putExtra(EXTRA_TASK_ID, task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setAlarmClockOrExact(triggerTimeMs, pendingIntent)
                } else {
                    // Fallback to inexact or standard while idle
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            } else {
                setAlarmClockOrExact(triggerTimeMs, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for task ${task.id} at $triggerTimeMs (${task.date} ${task.time})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for task ${task.id}", e)
        }
    }

    private fun setAlarmClockOrExact(triggerTimeMs: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val showIntent = Intent(context, MainActivity::class.java).apply {
                action = NotificationHelper.ACTION_TASK_DUE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                0,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    fun cancelTaskAlarm(taskId: Long) {
        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            action = ACTION_ROUTINE_TRIGGER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for task $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for task $taskId", e)
        }
    }

    private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = format.parse("$dateStr $timeStr")
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing datetime: $dateStr $timeStr", e)
            System.currentTimeMillis()
        }
    }

    /**
     * Calculates the new date & time string after adding [minutes] to current time.
     * Returns Pair(newDateStr, newTimeStr)
     */
    fun calculateSnoozeDateTime(minutesFromNow: Int): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutesFromNow)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return Pair(dateFormat.format(cal.time), timeFormat.format(cal.time))
    }
}
