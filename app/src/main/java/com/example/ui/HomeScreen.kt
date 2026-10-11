package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineTask
import com.example.data.preferences.ThemeMode
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.AiChatView
import com.example.ui.components.CompactDateStrip
import com.example.ui.components.RoutineTaskCard
import com.example.ui.components.SettingsView
import com.example.ui.components.TaskDueActionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Routine, 1: AI Planner, 2: Settings
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<RoutineTask?>(null) }

    val selectedDate by viewModel.selectedDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()
    val activeDueTask by viewModel.activeDueTask.collectAsState()
    val triggeredTasks by viewModel.triggeredTasks.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // Request Notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RoutineMate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Minimalist Routine Planner",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick theme toggle button
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.SYSTEM
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("quick_theme_toggle")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.SYSTEM -> Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings shortcut
                    IconButton(
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("settings_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedTab == 2) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(0.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Today, contentDescription = "Routines") },
                    label = { Text("Routines") },
                    modifier = Modifier.testTag("nav_routine_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Planner") },
                    label = { Text("AI Planner") },
                    modifier = Modifier.testTag("nav_ai_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn() + androidx.compose.animation.scaleIn(spring(Spring.DampingRatioMediumBouncy)),
                exit = fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Butter-smooth AnimatedContent transition between tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val isForward = targetState > initialState
                    (slideInHorizontally(
                        initialOffsetX = { if (isForward) it / 4 else -it / 4 },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(220))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { if (isForward) -it / 4 else it / 4 },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(220)))
                },
                label = "tabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Routines Timeline
                        RoutineTimelineContent(
                            selectedDate = selectedDate,
                            todayDateStr = viewModel.todayDateStr,
                            allTasks = allTasks,
                            tasksForDate = tasksForDate,
                            triggeredTasks = triggeredTasks,
                            onSelectDate = { viewModel.setSelectedDate(it) },
                            onToggleComplete = { task ->
                                viewModel.markTaskCompleted(task.id)
                            },
                            onEditTask = { task ->
                                taskToEdit = task
                            },
                            onDeleteTask = { task ->
                                viewModel.deleteTask(task)
                            },
                            onTriggerClick = { task ->
                                viewModel.openDueTaskDialog(task.id)
                            },
                            onOpenAiChat = { selectedTab = 1 },
                            onOpenAddTask = { showAddTaskDialog = true }
                        )
                    }

                    1 -> {
                        // AI Planner Screen
                        AiChatView(
                            messages = chatMessages,
                            isLoading = isAiLoading,
                            onSendMessage = { text -> viewModel.sendMessageToAi(text) },
                            onConfirmProposedTask = { id, action ->
                                viewModel.confirmAiProposedTask(id, action)
                            }
                        )
                    }

                    2 -> {
                        // Settings Screen
                        SettingsView(
                            savedApiKey = geminiApiKey,
                            effectiveKey = viewModel.getEffectiveApiKey(),
                            themeMode = themeMode,
                            onSaveApiKey = { viewModel.saveApiKey(it) },
                            onSelectThemeMode = { viewModel.setThemeMode(it) }
                        )
                    }
                }
            }

            // Task Due Action Modal (Persistent action: Completed vs Reschedule)
            if (activeDueTask != null) {
                TaskDueActionDialog(
                    task = activeDueTask!!,
                    onMarkDone = {
                        viewModel.markTaskCompleted(activeDueTask!!.id)
                    },
                    onReschedule = { newDate, newTime ->
                        viewModel.snoozeTask(activeDueTask!!.id, newDate, newTime)
                    },
                    onDismissTemporary = {
                        viewModel.dismissDueDialogManually()
                    }
                )
            }

            // Add new routine dialog (Title, Date, Time only)
            if (showAddTaskDialog) {
                AddEditTaskDialog(
                    initialDate = selectedDate,
                    onDismiss = { showAddTaskDialog = false },
                    onSave = { title, date, time ->
                        viewModel.addTask(title = title, date = date, time = time)
                        showAddTaskDialog = false
                    }
                )
            }

            // Edit existing routine dialog (Title, Date, Time only)
            if (taskToEdit != null) {
                AddEditTaskDialog(
                    initialDate = taskToEdit!!.date,
                    initialTask = taskToEdit,
                    onDismiss = { taskToEdit = null },
                    onSave = { title, date, time ->
                        viewModel.updateTask(
                            taskToEdit!!.copy(
                                title = title,
                                date = date,
                                time = time
                            )
                        )
                        taskToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
private fun RoutineTimelineContent(
    selectedDate: String,
    todayDateStr: String,
    allTasks: List<RoutineTask>,
    tasksForDate: List<RoutineTask>,
    triggeredTasks: List<RoutineTask>,
    onSelectDate: (String) -> Unit,
    onToggleComplete: (RoutineTask) -> Unit,
    onEditTask: (RoutineTask) -> Unit,
    onDeleteTask: (RoutineTask) -> Unit,
    onTriggerClick: (RoutineTask) -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenAddTask: () -> Unit
) {
    // Gentle pulse animation for due alert banner
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date Strip Row
        CompactDateStrip(
            selectedDate = selectedDate,
            todayDateStr = todayDateStr,
            allTasks = allTasks,
            onSelectDate = onSelectDate
        )

        // Persistent Triggered Alert Banner with smooth pulse
        if (triggeredTasks.isNotEmpty()) {
            val dueTask = triggeredTasks.first()
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onTriggerClick(dueTask) }
                    .testTag("due_task_alert_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 ROUTINE DUE: ${dueTask.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Tap to mark Completed or Reschedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Summary row
        val completedCount = tasksForDate.count { it.isCompleted }
        val totalCount = tasksForDate.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (totalCount == 0) "Daily Schedule"
                else "$totalCount routines • $completedCount completed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // AI shortcut chip
            Surface(
                onClick = onOpenAiChat,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("quick_ai_scheduler_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Plan with AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Timeline tasks list
        if (tasksForDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No routines scheduled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap (+) below or ask AI Planner to automatically schedule routines in your open slots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        onClick = onOpenAddTask,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("empty_state_add_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "New Routine",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasksForDate, key = { it.id }) { task ->
                    RoutineTaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                        onTriggerClick = { onTriggerClick(task) }
                    )
                }
            }
        }
    }
}
