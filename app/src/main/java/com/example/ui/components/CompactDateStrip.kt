package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
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
        cal.add(Calendar.DAY_OF_YEAR, -3) // 3 days in the past

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
        val dayNumFormat = SimpleDateFormat("d", Locale.US)

        for (i in 0..17) {
            val dateStr = dateFormat.format(cal.time)
            list.add(
                CalendarDayItem(
                    dateStr = dateStr,
                    dayOfWeek = dayOfWeekFormat.format(cal.time),
                    dayNumber = dayNumFormat.format(cal.time),
                    isToday = dateStr == todayDateStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val taskDateCounts = remember(allTasks) {
        allTasks.groupBy { it.date }.mapValues { it.value.size }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val index = daysList.indexOfFirst { it.dateStr == selectedDate }
        if (index != -1) {
            listState.animateScrollToItem(maxOf(0, index - 2))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayMonth = remember(selectedDate) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val parsed = sdf.parse(selectedDate) ?: Date()
                    val outSdf = SimpleDateFormat("MMMM yyyy", Locale.US)
                    outSdf.format(parsed)
                } catch (_: Exception) {
                    selectedDate
                }
            }

            Text(
                text = displayMonth,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedDate != todayDateStr) {
                Surface(
                    onClick = { onSelectDate(todayDateStr) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("jump_to_today_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Today",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysList, key = { it.dateStr }) { item ->
                val isSelected = item.dateStr == selectedDate
                val taskCount = taskDateCounts[item.dateStr] ?: 0

                val animatedBg by animateColorAsState(
                    targetValue = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        item.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    label = "bgColor"
                )

                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    item.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(animatedBg)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else if (item.isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectDate(item.dateStr) }
                        .padding(vertical = 10.dp, horizontal = 2.dp)
                        .testTag("date_chip_${item.dateStr}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (item.isToday) "Today" else item.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (item.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.dayNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        // Task presence dots
                        if (taskCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
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
