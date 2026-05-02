package com.example.task_tracker.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.task_tracker.data.model.task
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val AccentBlue   = Color(0xFF007AFF)
private val PriorityHigh = Color(0xFFFF3B30)
private val PriorityMed  = Color(0xFFFF9500)
private val PriorityLow  = Color(0xFF34C759)

@Composable
fun CalendarScreen(navController: NavController, viewModel: TaskViewModel) {

    LaunchedEffect(Unit) {
        viewModel.updateCalendarTasks()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Назад",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Календарь",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        CalendarTopBar(viewModel)

        Spacer(Modifier.height(12.dp))

        when (viewModel.calendarMode) {
            TaskViewModel.CalendarMode.MONTH -> MonthCalendar(viewModel)
            TaskViewModel.CalendarMode.WEEK  -> WeekCalendar(viewModel)
            TaskViewModel.CalendarMode.DAY   -> DayCalendar(viewModel)
        }

        Spacer(Modifier.height(16.dp))

        CalendarTasks(viewModel)
    }
}

@Composable
fun CalendarTopBar(viewModel: TaskViewModel) {

    val modes = listOf(
        TaskViewModel.CalendarMode.MONTH to "Месяц",
        TaskViewModel.CalendarMode.WEEK  to "Неделя",
        TaskViewModel.CalendarMode.DAY   to "День"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (mode, label) ->
            val selected = viewModel.calendarMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        when (mode) {
                            TaskViewModel.CalendarMode.MONTH -> viewModel.setMonth()
                            TaskViewModel.CalendarMode.WEEK  -> viewModel.setWeek()
                            TaskViewModel.CalendarMode.DAY   -> viewModel.setDay()
                        }
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun MonthCalendar(viewModel: TaskViewModel) {

    val date = viewModel.selectedDate
    val days = (1..date.lengthOfMonth()).map { date.withDayOfMonth(it) }
    val today = LocalDate.now()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.minusMonths(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = AccentBlue)
            }
            Text(
                text = "${date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() }} ${date.year}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.plusMonths(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronRight, null, tint = AccentBlue)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        val firstDayOfWeek = (date.withDayOfMonth(1).dayOfWeek.value - 1)
        val totalCells = firstDayOfWeek + days.size
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val index = row * 7 + col - firstDayOfWeek
                    val day = if (index in days.indices) days[index] else null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                color = when {
                                    day == viewModel.selectedDate -> AccentBlue
                                    day == today -> AccentBlue.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .then(
                                if (day != null) Modifier.clickable {
                                    viewModel.selectedDate = day
                                    viewModel.draggedTask?.let { dragged ->
                                        viewModel.moveTaskToDate(dragged, day)
                                        viewModel.draggedTask = null
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (day == viewModel.selectedDate) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    day == viewModel.selectedDate -> Color.White
                                    day == today -> AccentBlue
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekCalendar(viewModel: TaskViewModel) {

    val startOfWeek = viewModel.selectedDate.minusDays(3)
    val today = LocalDate.now()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.minusWeeks(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = AccentBlue)
            }
            Text(
                text = "Неделя",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.plusWeeks(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronRight, null, tint = AccentBlue)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..6) {
                val day = startOfWeek.plusDays(i.toLong())
                val selected = day == viewModel.selectedDate
                val isToday = day == today

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = when {
                                selected -> AccentBlue
                                isToday  -> AccentBlue.copy(alpha = 0.12f)
                                else     -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectedDate = day }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = day.dayOfMonth.toString(),
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DayCalendar(viewModel: TaskViewModel) {

    val today = LocalDate.now()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.minusDays(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = AccentBlue)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.selectedDate.dayOfWeek
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "${viewModel.selectedDate.dayOfMonth} ${
                        viewModel.selectedDate.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                    } ${viewModel.selectedDate.year}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = {
                viewModel.selectedDate = viewModel.selectedDate.plusDays(1)
                viewModel.updateCalendarTasks()
            }) {
                Icon(Icons.Rounded.ChevronRight, null, tint = AccentBlue)
            }
        }

        if (viewModel.selectedDate == today) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Сегодня", fontSize = 13.sp, color = AccentBlue, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CalendarTasks(viewModel: TaskViewModel) {

    val tasks = viewModel.calendarTasks.value

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Задач нет",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
        return
    }

    Text(
        text = "Задачи · ${tasks.size}",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tasks) { task ->
            TaskItem(task, viewModel)
        }
    }
}

@Composable
fun TaskItem(task: task, viewModel: TaskViewModel) {

    val isDragging = viewModel.draggedTask?.taskId == task.taskId

    val priorityColor = when (task.priority) {
        "Высокий" -> PriorityHigh
        "Средний" -> PriorityMed
        "Низкий"  -> PriorityLow
        else      -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { viewModel.draggedTask = task })
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                AccentBlue.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (task.priority != null && priorityColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(
                            color = priorityColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isdone)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isdone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (!task.description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (isDragging) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = null,
                    tint = AccentBlue.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}