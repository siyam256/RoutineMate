package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dateStr: String,
    val dayOfWeek: String,
    val dayNumber: String,
    val isToday: Boolean
)

@Composable
fun CompactDateStrip(
    selectedDate: String,
    todayDateStr: String,
    allTasks: List<RoutineTask>,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysList = remember(todayDateStr) {
        val list = mutableListOf<CalendarDayItem>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -4)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
        val dayNumFormat = SimpleDateFormat("d", Locale.US)

        for (i in 0..24) {
            val dateStr = dateFormat.format(cal.time)
            list.add(
                CalendarDayItem(
                    dateStr = dateStr,
                    dayOfWeek = dayOfWeekFormat.format(cal.time).uppercase(),
                    dayNumber = dayNumFormat.format(cal.time),
                    isToday = dateStr == todayDateStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val tasksByDate = remember(allTasks) {
        allTasks.groupBy { it.date }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val index = daysList.indexOfFirst { it.dateStr == selectedDate }
        if (index >= 0) {
            val scrollTarget = (index - 2).coerceAtLeast(0)
            listState.animateScrollToItem(scrollTarget)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysList, key = { it.dateStr }) { item ->
                val isSelected = item.dateStr == selectedDate
                val dayTasks = tasksByDate[item.dateStr] ?: emptyList()
                val totalCount = dayTasks.size
                val completedCount = dayTasks.count { it.isCompleted }

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(180),
                    label = "dateContainer"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(180),
                    label = "dateContent"
                )

                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                else if (item.isToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                // Sleek Minimalist Capsule (Black & White style)
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(containerColor)
                        .border(
                            width = if (isSelected || item.isToday) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectDate(item.dateStr) }
                        .testTag("date_chip_${item.dateStr}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = item.dayOfWeek.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) contentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.dayNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 17.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = contentColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Task status dot
                        if (totalCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            if (completedCount == totalCount) contentColor
                                            else contentColor.copy(alpha = 0.6f)
                                        } else {
                                            if (completedCount == totalCount) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.size(5.dp))
                        }
                    }
                }
            }
        }
    }
}
