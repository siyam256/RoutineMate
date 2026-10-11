package com.example.data.repository

import com.example.data.local.RoutineDao
import com.example.data.model.RoutineTask
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

class RoutineRepository(
    private val dao: RoutineDao,
    private val userPreferences: UserPreferences
) {
    val allTasks: Flow<List<RoutineTask>> = dao.getAllTasks()
    val triggeredTasks: Flow<List<RoutineTask>> = dao.getTriggeredTasks()
    val geminiApiKey: Flow<String> = userPreferences.geminiApiKeyFlow
    val themeMode: Flow<ThemeMode> = userPreferences.themeModeFlow

    fun getTasksByDate(date: String): Flow<List<RoutineTask>> = dao.getTasksByDate(date)

    suspend fun getTasksByDateDirect(date: String): List<RoutineTask> = dao.getTasksByDateDirect(date)

    suspend fun getAllPendingTasksDirect(): List<RoutineTask> = dao.getAllPendingTasksDirect()

    suspend fun getTaskById(id: Long): RoutineTask? = dao.getTaskById(id)

    suspend fun insertTask(task: RoutineTask): Long = dao.insertTask(task)

    suspend fun updateTask(task: RoutineTask) = dao.updateTask(task)

    suspend fun deleteTask(task: RoutineTask) = dao.deleteTask(task)

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) =
        dao.setTaskCompleted(taskId, isCompleted)

    suspend fun setTaskTriggered(taskId: Long, isTriggered: Boolean) =
        dao.setTaskTriggered(taskId, isTriggered)

    suspend fun rescheduleTask(taskId: Long, newDate: String, newTime: String) =
        dao.rescheduleTask(taskId, newDate, newTime)

    suspend fun saveGeminiApiKey(apiKey: String) = userPreferences.saveGeminiApiKey(apiKey)

    suspend fun saveThemeMode(mode: ThemeMode) = userPreferences.saveThemeMode(mode)
}
