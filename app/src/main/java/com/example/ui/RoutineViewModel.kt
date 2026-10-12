package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.RoutineApplication
import com.example.alarm.NotificationHelper
import com.example.alarm.RoutineAlarmScheduler
import com.example.data.ai.AiSchedulerManager
import com.example.data.ai.ProposedRoutineAction
import com.example.data.model.RoutineTask
import com.example.data.preferences.ThemeMode
import com.example.data.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val proposedAction: ProposedRoutineAction? = null,
    val isConfirmed: Boolean = false
)

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RoutineRepository = (application as RoutineApplication).repository
    private val alarmScheduler = RoutineAlarmScheduler(application)
    private val aiSchedulerManager = AiSchedulerManager()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayDateStr: String = dateFormat.format(Date())

    // Selected date for calendar view
    private val _selectedDate = MutableStateFlow(todayDateStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // All tasks from database
    val allTasks: StateFlow<List<RoutineTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks for the selected date
    val tasksForSelectedDate: StateFlow<List<RoutineTask>> = combine(
        allTasks,
        _selectedDate
    ) { tasks, date ->
        tasks.filter { it.date == date }.sortedBy { it.time }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Triggered tasks (where alarm fired and action is required: Completed vs Reschedule)
    val triggeredTasks: StateFlow<List<RoutineTask>> = repository.triggeredTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current task shown in the non-dismissable Action Required modal
    private val _activeDueTask = MutableStateFlow<RoutineTask?>(null)
    val activeDueTask: StateFlow<RoutineTask?> = _activeDueTask.asStateFlow()

    // Preferences
    val geminiApiKey: StateFlow<String> = repository.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    // AI Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "Welcome to RoutineMate AI!\nI can schedule your tasks intelligently based on your free time slots. For example, tell me:\n• 'I have a meeting tomorrow around noon'\n• 'Study block tonight at 9 PM'\n• 'Workout tomorrow morning for 45 min'\n\nI'll find an open slot and place it on your schedule automatically."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    init {
        // Auto-show due dialog if there are triggered tasks awaiting user action
        viewModelScope.launch {
            triggeredTasks.collect { list ->
                if (_activeDueTask.value == null && list.isNotEmpty()) {
                    _activeDueTask.value = list.first()
                } else if (_activeDueTask.value != null && !list.any { it.id == _activeDueTask.value?.id }) {
                    _activeDueTask.value = list.firstOrNull()
                }
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun openDueTaskDialog(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                _activeDueTask.value = task
            }
        }
    }

    fun dismissDueDialogManually() {
        _activeDueTask.value = null
    }

    fun addTask(
        title: String,
        date: String,
        time: String,
        description: String = "",
        durationMinutes: Int = 30,
        category: String = "general",
        priority: String = "medium"
    ) {
        viewModelScope.launch {
            val newTask = RoutineTask(
                title = title.trim(),
                description = description.trim(),
                date = date,
                time = time,
                durationMinutes = durationMinutes,
                category = category,
                priority = priority
            )
            val insertedId = repository.insertTask(newTask)
            val created = newTask.copy(id = insertedId)
            alarmScheduler.scheduleTaskAlarm(created)
        }
    }

    fun updateTask(task: RoutineTask) {
        viewModelScope.launch {
            repository.updateTask(task)
            alarmScheduler.cancelTaskAlarm(task.id)
            if (!task.isCompleted) {
                alarmScheduler.scheduleTaskAlarm(task)
            }
        }
    }

    fun deleteTask(task: RoutineTask) {
        viewModelScope.launch {
            alarmScheduler.cancelTaskAlarm(task.id)
            NotificationHelper.dismissNotification(getApplication(), task.id)
            repository.deleteTask(task)
            if (_activeDueTask.value?.id == task.id) {
                _activeDueTask.value = null
            }
        }
    }

    /**
     * User action: "Completed" (Done)
     * Marks completed, cancels notification, clears alarm, closes dialog.
     */
    fun markTaskCompleted(taskId: Long) {
        viewModelScope.launch {
            repository.setTaskCompleted(taskId, true)
            NotificationHelper.dismissNotification(getApplication(), taskId)
            alarmScheduler.cancelTaskAlarm(taskId)
            if (_activeDueTask.value?.id == taskId) {
                _activeDueTask.value = null
            }
        }
    }

    /**
     * User action: "Reschedule" (Snooze)
     * Reschedules with new date and time, clears old notification, sets new exact alarm.
     */
    fun snoozeTask(taskId: Long, newDate: String, newTime: String) {
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDate, newTime)
            NotificationHelper.dismissNotification(getApplication(), taskId)
            val updatedTask = repository.getTaskById(taskId)
            if (updatedTask != null) {
                alarmScheduler.scheduleTaskAlarm(updatedTask)
            }
            if (_activeDueTask.value?.id == taskId) {
                _activeDueTask.value = null
            }
        }
    }

    fun snoozeQuickMinutes(taskId: Long, minutes: Int) {
        val (newDate, newTime) = alarmScheduler.calculateSnoozeDateTime(minutes)
        snoozeTask(taskId, newDate, newTime)
    }

    // --- AI Chat Functions ---

    fun getEffectiveApiKey(): String {
        val userKey = geminiApiKey.value.trim()
        if (userKey.isNotEmpty()) return userKey
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        return ""
    }

    fun sendMessageToAi(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(sender = "user", text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true
        _aiError.value = null

        viewModelScope.launch {
            val key = getEffectiveApiKey()
            if (key.isEmpty()) {
                _isAiLoading.value = false
                _aiError.value = "Gemini API Key is not set. Please add your key in the Settings tab."
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "ai",
                    text = "⚠️ Gemini API Key missing!\nPlease tap the Settings icon (⚙️) on the top right to enter your Gemini API Key."
                )
                return@launch
            }

            // Build history
            val history = _chatMessages.value.dropLast(1).takeLast(6).map {
                Pair(it.sender, it.text)
            }

            val result = aiSchedulerManager.processUserMessage(
                apiKey = key,
                userMessage = trimmed,
                existingTasks = allTasks.value,
                chatHistory = history
            )

            _isAiLoading.value = false
            result.onSuccess { aiResult ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "ai",
                    text = aiResult.conversationalReply,
                    proposedAction = aiResult.proposedAction
                )
            }.onFailure { err ->
                _aiError.value = err.message
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "ai",
                    text = "Request failed: ${err.message ?: "Unknown error"}. Please check your API key and connection."
                )
            }
        }
    }

    fun confirmAiProposedTask(messageId: String, action: ProposedRoutineAction) {
        viewModelScope.launch {
            val targetDate = if (action.date.isNotBlank()) action.date else todayDateStr
            val targetTime = if (action.time.isNotBlank()) action.time else "12:00"

            val newTask = RoutineTask(
                title = action.title.ifBlank { "Routine Task" },
                date = targetDate,
                time = targetTime,
                durationMinutes = action.durationMinutes,
                category = action.category,
                priority = action.priority
            )
            val id = repository.insertTask(newTask)
            alarmScheduler.scheduleTaskAlarm(newTask.copy(id = id))

            // Mark message as confirmed in chat
            _chatMessages.value = _chatMessages.value.map { msg ->
                if (msg.id == messageId) msg.copy(isConfirmed = true) else msg
            }

            // Add confirmation note in chat
            _chatMessages.value = _chatMessages.value + ChatMessage(
                sender = "ai",
                text = "✓ '${action.title}' has been scheduled for $targetDate at $targetTime with an exact persistent alarm set."
            )
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            repository.saveGeminiApiKey(apiKey.trim())
        }
    }

    fun setGeminiApiKey(key: String) {
        saveApiKey(key)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.saveThemeMode(mode)
        }
    }

    fun toggleTaskCompleted(task: RoutineTask) {
        if (task.isCompleted) {
            updateTask(task.copy(isCompleted = false))
        } else {
            markTaskCompleted(task.id)
        }
    }

    fun acknowledgeTriggeredTask(task: RoutineTask) {
        openDueTaskDialog(task.id)
    }

    fun sendChatMessage(text: String) {
        sendMessageToAi(text)
    }

    fun confirmProposedTask(messageId: String, action: ProposedRoutineAction) {
        confirmAiProposedTask(messageId, action)
    }

    fun markDueTaskCompleted(task: RoutineTask) {
        markTaskCompleted(task.id)
    }

    fun rescheduleDueTask(task: RoutineTask, newDate: String, newTime: String) {
        snoozeTask(task.id, newDate, newTime)
    }

    fun dismissActiveDueTask() {
        dismissDueDialogManually()
    }

    fun clearAiError() {
        _aiError.value = null
    }
}
