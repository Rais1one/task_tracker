package com.example.task_tracker.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.task_tracker.data.model.task
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val PriorityHigh = Color(0xFFFF3B30)
private val PriorityMedium = Color(0xFFFF9500)
private val PriorityLow = Color(0xFF34C759)
private val AccentBlue = Color(0xFF007AFF)

@Composable
fun CategoryTasksScreenUI(
    navController: NavController,
    viewModel: TaskViewModel,
    categoryId: Int
) {

    val tasks by remember(categoryId) {
        if (categoryId == 0) {
            val start = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()

            val end = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()

            viewModel.selectTaskToday(start, end)
        } else {
            viewModel.getTasksByCategory(categoryId)
        }
    }.observeAsState(emptyList())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = if (categoryId == 0) "Сегодня" else "Задачи",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks) { task ->
                    ListTask(task, viewModel)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun ListTask(task: task, viewModel: TaskViewModel) {

    val scope = rememberCoroutineScope()


    var isDone by remember(task.taskId) { mutableStateOf(task.isdone) }

    LaunchedEffect(task.isdone) {
        isDone = task.isdone
    }

    val priorityColor = when (task.priority) {
        "Высокий" -> PriorityHigh
        "Средний" -> PriorityMedium
        "Низкий" -> PriorityLow
        else -> Color.Transparent
    }

    val formattedDate = task.date?.let {
        val dt = Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        "${dt.dayOfMonth}.${dt.monthValue}.${dt.year}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        color = if (isDone)
                            AccentBlue
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .clickable {


                        isDone = !isDone

                        scope.launch {
                            delay(500)
                            viewModel.updateTask(task.categoryId, task.taskId)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = task.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDone)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
                )

                task.description?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    formattedDate?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    task.priority?.let {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = priorityColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = it,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = priorityColor
                            )
                        }
                    }
                }
            }
        }
    }
}