package com.example

import android.app.Application
import com.example.alarm.NotificationHelper
import com.example.data.local.RoutineDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.RoutineRepository

class RoutineApplication : Application() {

    lateinit var repository: RoutineRepository
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize repository
        val database = RoutineDatabase.getDatabase(this)
        val userPreferences = UserPreferences(this)
        repository = RoutineRepository(database.routineDao(), userPreferences)
    }
}
