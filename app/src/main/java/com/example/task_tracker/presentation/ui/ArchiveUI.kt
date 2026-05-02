package com.example.task_tracker.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.task_tracker.data.model.category
import com.example.task_tracker.data.model.task
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

private val AccentBlue = Color(0xFF007AFF)
private val PriorityHigh = Color(0xFFFF3B30)
private val PriorityMed = Color(0xFFFF9500)
private val PriorityLow = Color(0xFF34C759)



@Composable
fun ArchiveUI(navController: NavController, viewModel: TaskViewModel) {

    val tasks by viewModel.taskArchive.observeAsState(emptyList())
    val categories by viewModel.allCategory.observeAsState(emptyList())

    val filteredTasks = remember(tasks, viewModel.archiveFilter) {
        tasks.filter { task ->

            val filter = viewModel.archiveFilter

            val matchCategory =
                filter.categoryId == null || task.categoryId == filter.categoryId

            val matchPriority =
                filter.priority == null || task.priority == filter.priority

            val matchDate =
                (filter.fromDate == null || (task.date ?: 0) >= filter.fromDate!!) &&
                        (filter.toDate == null || (task.date ?: 0) <= filter.toDate!!)

            matchCategory && matchPriority && matchDate
        }
    }

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
                    text = "Архив",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            FilterSection(viewModel, categories)

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Найдено: ${filteredTasks.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredTasks) { task ->
                    TaskArchive(task, viewModel)
                }
            }
        }
    }
}



@Composable
fun TaskArchive(task: task, viewModel: TaskViewModel) {

    val scope = rememberCoroutineScope()

    var isDone = task.isdone

    val priorityColor = when (task.priority) {
        "Высокий" -> PriorityHigh
        "Средний" -> PriorityMed
        "Низкий" -> PriorityLow
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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

                        scope.launch {

                            viewModel.updateTasktoCategory(task.categoryId, task.taskId)

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

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = task.name,
                    color = if (isDone)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(viewModel: TaskViewModel, categories: List<category>) {

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val filter = viewModel.archiveFilter

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Высокий", "Средний", "Низкий")) { priority ->

                val selected = filter.priority == priority

                val color = when (priority) {
                    "Высокий" -> PriorityHigh
                    "Средний" -> PriorityMed
                    else -> PriorityLow
                }

                FilterChip(
                    selected = selected,
                    onClick = {
                        viewModel.archiveFilter = filter.copy(
                            priority = if (selected) null else priority
                        )
                    },
                    label = { Text(priority) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color
                    )
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->

                val selected = filter.categoryId == category.categoryId

                FilterChip(
                    selected = selected,
                    onClick = {
                        viewModel.archiveFilter = filter.copy(
                            categoryId = if (selected) null else category.categoryId
                        )
                    },
                    label = { Text(category.name) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            OutlinedButton(onClick = { showFromDatePicker = true }) {
                Text(filter.fromDate?.let { "От: ${formatDate(it)}" } ?: "От")
            }

            OutlinedButton(onClick = { showToDatePicker = true }) {
                Text(filter.toDate?.let { "До: ${formatDate(it)}" } ?: "До")
            }
        }
    }

    if (showFromDatePicker) {
        val state = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        viewModel.archiveFilter = filter.copy(fromDate = it)
                    }
                    showFromDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showToDatePicker) {
        val state = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        viewModel.archiveFilter = filter.copy(toDate = it)
                    }
                    showToDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}



fun formatDate(millis: Long): String {
    val date = Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return "${date.dayOfMonth}.${date.monthValue}.${date.year}"
}