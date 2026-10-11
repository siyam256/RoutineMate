package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_tasks")
data class RoutineTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String,             // Format: "yyyy-MM-dd"
    val time: String,             // Format: "HH:mm" (24-hour)
    val durationMinutes: Int = 30, // For scheduling & free slot calculation
    val isCompleted: Boolean = false,
    val isTriggered: Boolean = false, // When alarm has fired and awaiting user response
    val snoozeCount: Int = 0,
    val category: String = "general", // "work", "meeting", "study", "health", "personal", "general"
    val priority: String = "medium",   // "low", "medium", "high"
    val createdAt: Long = System.currentTimeMillis()
)
