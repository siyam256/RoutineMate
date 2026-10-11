package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.NotificationHelper
import com.example.alarm.RoutineAlarmScheduler
import com.example.data.local.RoutineDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "RoutineAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(RoutineAlarmScheduler.EXTRA_TASK_ID, -1L)

        Log.d(TAG, "onReceive action=$action, taskId=$taskId")
        if (taskId == -1L) return

        val database = RoutineDatabase.getDatabase(context)
        val dao = database.routineDao()

        when (action) {
            RoutineAlarmScheduler.ACTION_ROUTINE_TRIGGER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val task = dao.getTaskById(taskId)
                    if (task != null && !task.isCompleted) {
                        // Mark task as triggered
                        dao.setTaskTriggered(taskId, true)
                        // Show persistent ongoing notification
                        NotificationHelper.showPersistentDueNotification(context, task)
                    }
                }
            }

            NotificationHelper.ACTION_COMPLETE_FROM_NOTIFICATION -> {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.setTaskCompleted(taskId, true)
                    NotificationHelper.dismissNotification(context, taskId)
                    RoutineAlarmScheduler(context).cancelTaskAlarm(taskId)
                }
            }
        }
    }
}
