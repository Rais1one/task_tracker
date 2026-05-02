package com.example.task_tracker.presentation.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.task_tracker.service.RepeatScheduler
import com.example.task_tracker.data.local.TaskDao
import com.example.task_tracker.data.local.TaskRoomDatabase
import com.example.task_tracker.data.model.CategoryStat
import com.example.task_tracker.data.model.DayStat
import com.example.task_tracker.data.model.TaskFilter
import com.example.task_tracker.data.model.category
import com.example.task_tracker.data.model.task
import com.example.task_tracker.data.remote.FirebaseSyncRepository
import com.example.task_tracker.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class TaskViewModel(application: Application) : AndroidViewModel(application) {


    var isDarkTheme by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }


    private val repository: TaskRepository


    var draggedTask by mutableStateOf<task?>(null)

    val allCategory: LiveData<List<category>>
    val categories: LiveData<List<category>>
    val taskArchive: LiveData<List<task>>
    private val prefs = application.getSharedPreferences("xp", Context.MODE_PRIVATE)
    private val _userPoints = mutableStateOf(0)
    val userPoints = _userPoints
    var taskRemider by mutableStateOf<Int?>(null)


    init {
        val db = TaskRoomDatabase.Companion.getInstance(application)

        val taskDao = db.taskDao()
        val categoryDao = db.categoryDao()
        com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously()
        repository = TaskRepository(taskDao, categoryDao)
        _userPoints.value = prefs.getInt("xp", 0)
        allCategory = repository.categoryList
        categories = categoryDao.getCategory()
        taskArchive = taskDao.getTasksToArchive()
    }


    var archiveFilter by mutableStateOf(TaskFilter())

    val filteredTasks: List<task>
        get() {
            val list = taskArchive.value.orEmpty()

            return list.filter { task ->

                val matchCategory =
                    archiveFilter.categoryId == null ||
                            task.categoryId == archiveFilter.categoryId

                val matchPriority =
                    archiveFilter.priority == null ||
                            task.priority == archiveFilter.priority

                val matchDate =
                    (archiveFilter.fromDate == null || (task.date ?: 0) >= archiveFilter.fromDate!!) &&
                            (archiveFilter.toDate == null || (task.date ?: 0) <= archiveFilter.toDate!!)

                matchCategory && matchPriority && matchDate
            }
        }


    var selectedCategoryId by mutableStateOf<Int?>(null)

    var taskName by mutableStateOf("")
    var categoryName by mutableStateOf("")
    var taskDescription by mutableStateOf<String?>(null)
    var taskDate by mutableStateOf<Long?>(null)
    var taskPriority by mutableStateOf<String?>(null)
    var taskCategoryId by mutableStateOf<Int?>(2)
    var taskRepear by mutableStateOf<String?>(null)


    enum class CalendarMode {
        MONTH, WEEK, DAY
    }

    var calendarMode by mutableStateOf(CalendarMode.MONTH)
    var selectedDate by mutableStateOf(LocalDate.now())

    private val _calendarTasks = mutableStateOf<List<task>>(emptyList())
    val calendarTasks: State<List<task>> get() = _calendarTasks

    fun updateCalendarTasks() {

        val (start, end) = when (calendarMode) {

            CalendarMode.DAY -> {
                val s = selectedDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                val e = selectedDate.plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                s to e
            }

            CalendarMode.WEEK -> {
                val s = selectedDate.minusDays(3)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                val e = selectedDate.plusDays(3)
                    .atTime(23, 59)
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                s to e
            }

            CalendarMode.MONTH -> {
                val s = selectedDate.withDayOfMonth(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                val e = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
                    .atTime(23, 59)
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                s to e
            }
        }

        repository.selectTaskToday(start, end).observeForever {
            _calendarTasks.value = it.filter { task ->  !task.isdone }
        }
    }

    fun setMonth() {
        calendarMode = CalendarMode.MONTH
        updateCalendarTasks()
    }

    fun setWeek() {
        calendarMode = CalendarMode.WEEK
        updateCalendarTasks()
    }

    fun setDay() {
        calendarMode = CalendarMode.DAY
        updateCalendarTasks()
    }


    fun changeCategory(value: Int) {
        selectedCategoryId = value
    }

    fun changeName(value: String) {
        taskName = value
    }

    fun changeNameCategory(value: String) {
        categoryName = value
    }

    fun changeDescription(value: String?) {
        taskDescription = value
    }

    fun changeDate(value: Long?) {
        taskDate = value
    }

    fun changePriority(value: String?) {
        taskPriority = value
    }

    fun changeCategoryTask(value: Int?) {
        taskCategoryId = value
    }

    fun changeRepear(value: String?) {
        taskRepear = value
    }

    fun changeRemider(value: Int?) {
        taskRemider = value
    }


    fun getTasksByDate(date: LocalDate): List<task> {
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return taskArchive.value.orEmpty().filter {
            val taskDate = it.date ?: return@filter false
            taskDate in start until end
        }
    }

    fun getTasksByCategory(categoryId: Int): LiveData<List<task>> {
        return repository.getTasksByCategory(categoryId)
    }

    fun selectTaskToday(start: Long, end: Long): LiveData<List<task>> {
        return repository.selectTaskToday(start, end)
    }

    fun getTasksToArchive(categoryId: Int): LiveData<List<task>> {
        return repository.getTasksToArchive()
    }


    fun setDateAndTime(dateMillis: Long, hour: Int, minute: Int) {
        val localDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val dateTime = localDate.atTime(hour, minute)

        taskDate = dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun moveTaskToDate(task: task, newDate: LocalDate) {
        val millis = newDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        repository.updateTaskDate(task.taskId, millis)

        updateCalendarTasks()
    }


    fun addTask(onResult: (Int) -> Unit) {

        viewModelScope.launch {

            val newTask = task(
                name = taskName,
                description = taskDescription,
                date = taskDate,
                priority = taskPriority,
                repeat = taskRepear,
                reminder = taskRemider,
                categoryId = selectedCategoryId ?: 0,
                isdone = false
            )

            val id = repository.addTask(newTask)

            firebase.syncTask(
                userId,
                newTask.copy(taskId = id)
            )

            onResult(id)
        }
    }

    fun deleteTask(id: Int) {
        repository.deleteTask(id)
    }

    fun addCategory() {
        repository.addCategory(category(name = categoryName))
    }

    fun deleteCategory(id: Int) {
        repository.deleteCategory(id)
    }

    fun updateTask(categoryId: Int?, id: Int) {

        repository.updateTask(categoryId, id)

        viewModelScope.launch(Dispatchers.IO) {

            val updatedTask = repository.getTaskById(id)

            firebase.syncTask(userId, updatedTask)
        }
    }

    fun updateTasktoCategory(categoryId: Int?, id: Int) {

        repository.updateTasktoCategory(categoryId, id)

        viewModelScope.launch(Dispatchers.IO) {

            val updatedTask = repository.getTaskById(id)

            firebase.syncTask(userId, updatedTask)
        }
    }


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchedTasks: StateFlow<List<task>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.searchTasks(query)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            emptyList()
        )

    fun onSearchChange(value: String) {
        _searchQuery.value = value
    }





    enum class StatsPeriod {
        DAY, WEEK, MONTH
    }

    var statsPeriod by mutableStateOf(StatsPeriod.WEEK)




    val doneTasks = mutableStateOf(0)
    val notDoneTasks = mutableStateOf(0)



    fun loadCounts() {
        viewModelScope.launch(Dispatchers.IO) {

            val done = repository.getDoneTasks().size
            val notDone = repository.getNotDoneTasks().size

            withContext(Dispatchers.Main) {
                doneTasks.value = done
                notDoneTasks.value = notDone
            }
        }
    }


    val categoryStats = mutableStateOf<List<CategoryStat>>(emptyList())

    fun loadCategoryStats() {
        viewModelScope.launch(Dispatchers.IO) {

            val result = repository.getTasksByCategoryStats()

            withContext(Dispatchers.Main) {
                categoryStats.value = result
            }
        }
    }

    fun loadProductivity() {
        viewModelScope.launch(Dispatchers.IO) {

            val tasks = repository.getAllTasks().filter { it.date != null }

            val grouped = when (statsMode) {

                StatsMode.DAY -> {

                    tasks.groupBy { task ->
                        val date = Instant.ofEpochMilli(task.date!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        date.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }
                }

                StatsMode.WEEK -> {

                    tasks.groupBy { task ->
                        val date = Instant.ofEpochMilli(task.date!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        val monday = date.with(DayOfWeek.MONDAY)

                        monday.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }
                }

                StatsMode.MONTH -> {

                    tasks.groupBy { task ->
                        val date = Instant.ofEpochMilli(task.date!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        val firstDay = date.withDayOfMonth(1)

                        firstDay.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }
                }
            }

            val result = grouped.map { entry ->
                DayStat(
                    day = entry.key,
                    count = entry.value.size
                )
            }.sortedBy { it.day }

            withContext(Dispatchers.Main) {
                dayStats.value = result
            }
        }
    }
    enum class StatsMode {
        DAY, WEEK, MONTH
    }

    var statsMode by mutableStateOf(StatsMode.DAY)


    val dayStats = mutableStateOf<List<DayStat>>(emptyList())

    fun exportToCSV(context: Context, onDone: (File) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {

            val tasks = repository.getAllTasks()

            val file = File(
                context.getExternalFilesDir(null),
                "tasks_export_${System.currentTimeMillis()}.csv"
            )

            file.printWriter().use { out ->

                out.println("Название;Описание;Дата;Приоритет;Категория;Статус")

                tasks.forEach { task ->

                    val date = task.date?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    } ?: "—"

                    val status = if (task.isdone) "Выполнено" else "Не выполнено"

                    out.println(
                        "${task.name};" +
                                "${task.description ?: "—"};" +
                                "$date;" +
                                "${task.priority ?: "—"};" +
                                "${task.categoryId};" +
                                status
                    )
                }
            }

            withContext(Dispatchers.Main) {
                onDone(file)
            }
        }
    }



    fun exportToPDF(context: Context, onDone: (String) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {

            val tasks = repository.getAllTasks()

            val pdf = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint().apply {
                textSize = 12f
            }

            var y = 50

            canvas.drawText("ОТЧЁТ ПО ЗАДАЧАМ", 180f, y.toFloat(), paint)
            y += 40

            tasks.forEach { task ->

                val date = task.date?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                } ?: "—"

                val status = if (task.isdone) "Выполнено" else "Не выполнено"

                canvas.drawText("${task.name} | $date | $status", 20f, y.toFloat(), paint)
                y += 25
            }

            pdf.finishPage(page)

            val fileName = "tasks_report_${System.currentTimeMillis()}.pdf"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val resolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS)
                }

                val fileUri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                resolver.openOutputStream(fileUri!!)?.use {
                    pdf.writeTo(it)
                }

                fileUri.toString()

            } else {

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )

                val file = File(downloadsDir, fileName)

                file.outputStream().use {
                    pdf.writeTo(it)
                }

                file.absolutePath
            }

            pdf.close()

            withContext(Dispatchers.Main) {
                onDone(uri)
            }
        }
    }






    private val completedTaskIds = mutableSetOf<Int>()

    fun addPoints(task: task) {
        val points = when (task.priority) {
            "Низкий" -> 2
            "Средний" -> 5
            "Высокий" -> 10
            else -> 1
        }

        _userPoints.value += points
        saveXP()
    }

    fun removePoints(task: task) {
        val points = when (task.priority) {
            "Низкий" -> 2
            "Средний" -> 5
            "Высокий" -> 10
            else -> 1
        }

        _userPoints.value = (_userPoints.value - points).coerceAtLeast(0)
        saveXP()
    }

    private fun saveXP() {
        prefs.edit()
            .putInt("xp", _userPoints.value)
            .apply()
    }



    fun toggleTaskDone(task: task) {

        viewModelScope.launch(Dispatchers.IO) {

            val newState = !task.isdone

            repository.updateTaskDone(task.taskId, newState) // 👈 ВАЖНО

            withContext(Dispatchers.Main) {

                if (newState) {
                    addPoints(task)
                } else {
                    removePoints(task)
                }
            }
        }
    }

    val level: Int
        get() = (_userPoints.value / 100) + 1

    val progress: Float
        get() = (_userPoints.value % 100) / 100f


    var notificationsEnabled by mutableStateOf(true)

    fun changeNotificationsEnabled(value: Boolean) {
        notificationsEnabled = value
    }

    fun updateRepeat(taskId: Int, repeat: String?) {

        val context = getApplication<Application>()

        if (repeat == null) {
            RepeatScheduler.cancel(context, taskId)
            return
        }

        val intervalMillis = when (repeat) {
            "HOUR" -> TimeUnit.HOURS.toMillis(1)
            "DAILY" -> TimeUnit.DAYS.toMillis(1)
            "WEEKLY" -> TimeUnit.DAYS.toMillis(7)
            "MONTHLY" -> TimeUnit.DAYS.toMillis(30)
            else -> return
        }

        RepeatScheduler.schedule(
            context = context,
            taskId = taskId,
            title = taskName.ifBlank { "Задача" },
            desc = taskDescription ?: "",
            intervalMillis = intervalMillis
        )
    }

    private val firebase = FirebaseSyncRepository()
    private val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    fun startFirebaseSync(roomDao: TaskDao) {

        firebase.listenTasks(userId) { firebaseTasks ->

            viewModelScope.launch(Dispatchers.IO) {

                firebaseTasks.forEach { task ->

                    roomDao.addTask(task)
                }
            }
        }
    }
}