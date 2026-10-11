package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.RoutineAlarmScheduler
import com.example.data.local.RoutineDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d(TAG, "Reboot/Update received, restoring routine alarms...")
            val scheduler = RoutineAlarmScheduler(context)
            val database = RoutineDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                val pendingTasks = database.routineDao().getAllPendingTasksDirect()
                for (task in pendingTasks) {
                    scheduler.scheduleTaskAlarm(task)
                }
                Log.d(TAG, "Restored ${pendingTasks.size} routine alarms successfully.")
            }
        }
    }
}
