package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.alarm.NotificationHelper
import com.example.ui.HomeScreen
import com.example.ui.RoutineViewModel
import com.example.ui.theme.RoutineMateTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RoutineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            RoutineMateTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)

        if (taskId != -1L && (action == NotificationHelper.ACTION_TASK_DUE || action == NotificationHelper.ACTION_SNOOZE_PICKER)) {
            viewModel.openDueTaskDialog(taskId)
        }
    }
}
