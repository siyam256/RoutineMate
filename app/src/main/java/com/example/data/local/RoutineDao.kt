package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RoutineTask
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routine_tasks ORDER BY date ASC, time ASC")
    fun getAllTasks(): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks WHERE date = :date ORDER BY time ASC")
    fun getTasksByDate(date: String): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks WHERE date = :date ORDER BY time ASC")
    suspend fun getTasksByDateDirect(date: String): List<RoutineTask>

    @Query("SELECT * FROM routine_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): RoutineTask?

    @Query("SELECT * FROM routine_tasks WHERE isTriggered = 1 AND isCompleted = 0 ORDER BY date ASC, time ASC")
    fun getTriggeredTasks(): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks WHERE isCompleted = 0 ORDER BY date ASC, time ASC")
    suspend fun getAllPendingTasksDirect(): List<RoutineTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RoutineTask): Long

    @Update
    suspend fun updateTask(task: RoutineTask)

    @Delete
    suspend fun deleteTask(task: RoutineTask)

    @Query("UPDATE routine_tasks SET isCompleted = :isCompleted, isTriggered = 0 WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)

    @Query("UPDATE routine_tasks SET isTriggered = :isTriggered WHERE id = :taskId")
    suspend fun setTaskTriggered(taskId: Long, isTriggered: Boolean)

    @Query("UPDATE routine_tasks SET date = :newDate, time = :newTime, isTriggered = 0, snoozeCount = snoozeCount + 1 WHERE id = :taskId")
    suspend fun rescheduleTask(taskId: Long, newDate: String, newTime: String)
}
