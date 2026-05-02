package com.example.task_tracker.presentation.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.task_tracker.data.model.category
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import java.util.*

private val AccentBlue = Color(0xFF007AFF)

@Composable
fun AddTaskUI(navController: NavController, viewModel: TaskViewModel) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var textName by remember { mutableStateOf("") }
    var textDesc by remember { mutableStateOf("") }


    val maxChars = 255

    var isPriorityExpanded by remember { mutableStateOf(false) }
    var isRepeatExpanded by remember { mutableStateOf(false) }
    var isReminderExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("Нет", "Низкий", "Средний", "Высокий")
    var priority by remember { mutableStateOf("Нет") }

    val repeats = mapOf(
        "Нет" to null,
        "Каждый час" to "HOUR",
        "Каждый день" to "DAILY",
        "Каждую неделю" to "WEEKLY",
        "Каждый месяц" to "MONTHLY"
    )
    var repeat by remember { mutableStateOf<String?>(null) }
    var repeatLabel by remember { mutableStateOf("Нет") }

    val reminders = mapOf(
        "Нет" to null,
        "За 5 минут" to 5,
        "За 30 минут" to 30,
        "За 1 час" to 60,
        "За 1 день" to 1440,
        "За 1 неделю" to 10080
    )
    var reminderText by remember { mutableStateOf("Нет") }

    val categories by viewModel.categories.observeAsState(emptyList())
    var selectedCategory by remember { mutableStateOf<category?>(null) }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory == null) {
            categories.find { it.name == "Важное" }?.let {
                selectedCategory = it
                viewModel.changeCategory(it.categoryId)
            }
        }
    }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var isDateEnabled by remember { mutableStateOf(false) }
    var isTimeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = AccentBlue)
                }
                Text("Новая задача", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))


            Card(shape = RoundedCornerShape(16.dp)) {
                Column {

                    OutlinedTextField(
                        value = textName,
                        onValueChange = {
                            if (it.length <= maxChars) {
                                textName = it
                            }
                        },
                        placeholder = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = textDesc,
                        onValueChange = {
                            if (it.length <= maxChars) {
                                textDesc = it
                            }
                        },
                        placeholder = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(16.dp)) {

                Column {

                    RowSwitch(
                        Icons.Rounded.CalendarMonth,
                        AccentBlue,
                        if (selectedDate.isNotEmpty()) "Дата: $selectedDate" else "Дата",
                        isDateEnabled
                    ) {
                        isDateEnabled = it
                        if (it) {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    selectedDate = "$d.${m + 1}.$y"
                                    val c = Calendar.getInstance()
                                    c.set(y, m, d)
                                    viewModel.changeDate(c.timeInMillis)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    }

                    RowSwitch(
                        Icons.Rounded.AccessTime,
                        AccentBlue,
                        if (selectedTime.isNotEmpty()) "Время: $selectedTime" else "Время",
                        isTimeEnabled
                    ) {
                        isTimeEnabled = it
                        if (it) {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    selectedTime = "%02d:%02d".format(h, m)
                                    viewModel.setDateAndTime(
                                        viewModel.taskDate ?: System.currentTimeMillis(),
                                        h, m
                                    )
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        }
                    }

                    RowSwitch(
                        Icons.Rounded.Notifications,
                        AccentBlue,
                        "Уведомления",
                        notificationsEnabled
                    ) {
                        notificationsEnabled = it
                        viewModel.changeNotificationsEnabled(it)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(16.dp)) {

                Column {

                    RowPicker(Icons.Rounded.Flag, AccentBlue, "Приоритет", priority, isPriorityExpanded) {
                        isPriorityExpanded = !isPriorityExpanded
                    }

                    if (isPriorityExpanded) {
                        priorities.forEach {
                            PickerOption(it, it == priority) {
                                priority = it
                                viewModel.changePriority(if (it == "Нет") null else it)
                                isPriorityExpanded = false
                            }
                        }
                    }

                    RowDivider()

                    RowPicker(Icons.Rounded.Repeat, AccentBlue, "Повтор", repeatLabel, isRepeatExpanded) {
                        isRepeatExpanded = !isRepeatExpanded
                    }

                    if (isRepeatExpanded) {
                        repeats.forEach { (k, v) ->
                            PickerOption(k, repeatLabel == k) {
                                repeat = v
                                repeatLabel = k
                                viewModel.changeRepear(v)
                                isRepeatExpanded = false
                            }
                        }
                    }

                    RowDivider()

                    RowPicker(Icons.Rounded.NotificationsActive, AccentBlue, "Напоминание", reminderText, isReminderExpanded) {
                        isReminderExpanded = !isReminderExpanded
                    }

                    if (isReminderExpanded) {
                        reminders.forEach { (k, v) ->
                            PickerOption(k, reminderText == k) {
                                reminderText = k
                                viewModel.changeRemider(v)
                                isReminderExpanded = false
                            }
                        }
                    }

                    RowDivider()

                    RowPicker(
                        Icons.Rounded.FolderOpen,
                        AccentBlue,
                        "Категория",
                        selectedCategory?.name ?: "Выбрать",
                        isCategoryExpanded
                    ) {
                        isCategoryExpanded = !isCategoryExpanded
                    }

                    if (isCategoryExpanded) {
                        categories.forEach {
                            PickerOption(it.name, selectedCategory == it) {
                                selectedCategory = it
                                viewModel.changeCategory(it.categoryId)
                                isCategoryExpanded = false
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.changeName(textName)
                    viewModel.changeDescription(textDesc.ifBlank { null })

                    viewModel.addTask {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Сохранить")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}



@Composable
private fun RowDivider() {
    HorizontalDivider()
}

@Composable
private fun RowSwitch(
    icon: ImageVector,
    color: Color,
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun RowPicker(
    icon: ImageVector,
    color: Color,
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f))
        Text(value)
        Icon(if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, null)
    }
}

@Composable
private fun PickerOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp)
    ) {
        Text(label, Modifier.weight(1f))
        if (selected) Icon(Icons.Rounded.Check, null, tint = AccentBlue)
    }
}