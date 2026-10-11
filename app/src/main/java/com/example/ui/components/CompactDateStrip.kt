package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineTask
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Violet500
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

        for (i in 0..20) {
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
        // Month & Quick Today Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
            }

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
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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

        // Horizontal Day Pills
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysList, key = { it.dateStr }) { item ->
                val isSelected = item.dateStr == selectedDate
                val taskCount = taskDateCounts[item.dateStr] ?: 0

                val itemScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "itemScale"
                )

                val textColor = when {
                    isSelected -> Color.White
                    item.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .scale(itemScale)
                        .width(52.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Indigo500, spotColor = Violet500)
                                    .background(BrandGradient)
                            } else {
                                Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (item.isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            }
                        )
                        .clickable { onSelectDate(item.dateStr) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (item.isToday) "TODAY" else item.dayOfWeek.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else textColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.dayNumber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        // Task indicator dots
                        if (taskCount > 0) {
                            Box(
                                modifier = Modifier
                                    .width(if (taskCount > 1) 12.dp else 5.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.secondary
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}
