package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        // Circular avatar / profile icon from screenshot
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "RoutineMate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Sleek Minimalist "+ Add Task" Pill Button (from screenshot!)
                    Surface(
                        onClick = { showAddTaskDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("top_add_task_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Task",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Minimalist Theme Switcher button
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Today, contentDescription = "Schedule", modifier = Modifier.size(20.dp)) },
                    label = { Text("Schedule", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_routine_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Planner", modifier = Modifier.size(20.dp)) },
                    label = { Text("AI Planner", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_ai_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(20.dp)) },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val isForward = targetState > initialState
                    (slideInHorizontally(
                        initialOffsetX = { if (isForward) it / 3 else -it / 3 },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(200))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { if (isForward) -it / 3 else it / 3 },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(200)))
                },
                label = "tabContentTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> RoutineTimelineContent(
                        selectedDate = selectedDate,
                        tasks = tasksForDate,
                        allTasks = allTasks,
                        triggeredTasks = triggeredTasks,
                        onSelectDate = { viewModel.setSelectedDate(it) },
                        onToggleComplete = { viewModel.toggleTaskCompleted(it) },
                        onEditTask = { taskToEdit = it },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onTriggerClick = { viewModel.acknowledgeTriggeredTask(it) },
                        onOpenAddTask = { showAddTaskDialog = true },
                        onOpenAiChat = { selectedTab = 1 }
                    )

                    1 -> AiChatView(
                        messages = chatMessages,
                        isLoading = isAiLoading,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onConfirmProposedTask = { msgId, proposed ->
                            viewModel.confirmProposedTask(msgId, proposed)
                            selectedTab = 0
                        }
                    )

                    2 -> SettingsView(
                        savedApiKey = geminiApiKey,
                        effectiveKey = viewModel.getEffectiveApiKey(),
                        themeMode = themeMode,
                        onSaveApiKey = { viewModel.setGeminiApiKey(it) },
                        onSelectThemeMode = { viewModel.setThemeMode(it) }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddEditTaskDialog(
            initialDate = selectedDate,
            initialTask = null,
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, description, date, time ->
                viewModel.addTask(title = title, date = date, time = time, description = description)
                showAddTaskDialog = false
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        AddEditTaskDialog(
            initialDate = task.date,
            initialTask = task,
            onDismiss = { taskToEdit = null },
            onSave = { title, description, date, time ->
                viewModel.updateTask(task.copy(title = title, description = description, date = date, time = time))
                taskToEdit = null
            }
        )
    }

    // Persistent Alarm / Due Action Dialog
    activeDueTask?.let { dueTask ->
        TaskDueActionDialog(
            task = dueTask,
            onMarkDone = {
                viewModel.markDueTaskCompleted(dueTask)
            },
            onReschedule = { newDate, newTime ->
                viewModel.rescheduleDueTask(dueTask, newDate, newTime)
            },
            onDismissTemporary = {
                viewModel.dismissActiveDueTask()
            }
        )
    }
}

@Composable
private fun RoutineTimelineContent(
    selectedDate: String,
    tasks: List<RoutineTask>,
    allTasks: List<RoutineTask>,
    triggeredTasks: List<RoutineTask>,
    onSelectDate: (String) -> Unit,
    onToggleComplete: (RoutineTask) -> Unit,
    onEditTask: (RoutineTask) -> Unit,
    onDeleteTask: (RoutineTask) -> Unit,
    onTriggerClick: (RoutineTask) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenAiChat: () -> Unit
) {
    val todayDateStr = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.format(Date())
    }

    val isToday = selectedDate == todayDateStr

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val pendingCount = totalCount - completedCount
    val progressFraction = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    // Filter mode state: 0 = All, 1 = Pending, 2 = Completed
    var filterMode by remember { mutableIntStateOf(0) }

    val displayedTasks = remember(tasks, filterMode) {
        when (filterMode) {
            1 -> tasks.filter { !it.isCompleted }
            2 -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Minimalist Horizontal Date Strip (Capsules)
        CompactDateStrip(
            selectedDate = selectedDate,
            todayDateStr = todayDateStr,
            allTasks = allTasks,
            onSelectDate = onSelectDate
        )

        // Minimalist Daily Focus & Progress Card (Pure Monochrome)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isToday) "Today's Schedule" else "Schedule for $selectedDate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (totalCount == 0) "No routines planned for this day"
                            else "$completedCount of $totalCount completed (${(progressFraction * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Compact Ask AI Pill
                    Surface(
                        onClick = onOpenAiChat,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .height(30.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("quick_ai_scheduler_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ask AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (totalCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // Persistent Triggered Alert Banner (Monochrome High Contrast)
        if (triggeredTasks.isNotEmpty()) {
            val dueTask = triggeredTasks.first()
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onTriggerClick(dueTask) }
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    .testTag("due_task_alert_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ROUTINE DUE: ${dueTask.title}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Tap to mark Done or Reschedule",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section header & Minimalist Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Routines",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple(0, "All", totalCount),
                    Triple(1, "Pending", pendingCount),
                    Triple(2, "Done", completedCount)
                ).forEach { (idx, label, count) ->
                    val isSelected = filterMode == idx
                    Surface(
                        onClick = { filterMode = idx },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .height(28.dp)
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($count)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Task list or Clean Minimalist Empty State
        if (displayedTasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (totalCount == 0) "No Routines for this Day" else "No routines in this filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (totalCount == 0) "Tap '+ Add Task' above to schedule your day."
                    else "Check other filters or add a new routine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Compact, solid Create Routine button
                Button(
                    onClick = onOpenAddTask,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("empty_state_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Routine",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedTasks, key = { it.id }) { task ->
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
