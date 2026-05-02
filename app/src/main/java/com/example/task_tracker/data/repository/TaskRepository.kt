package com.example.task_tracker.data.repository

import androidx.lifecycle.LiveData
import com.example.task_tracker.data.model.CategoryStat
import com.example.task_tracker.data.model.category
import com.example.task_tracker.data.local.CategoryDao
import com.example.task_tracker.data.local.TaskDao
import com.example.task_tracker.data.model.task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskRepository(private val taskDao: TaskDao, private val categoryDao: CategoryDao) {
    private  val coroutineScope = CoroutineScope(Dispatchers.Main)

    val categoryList: LiveData<List<category>> = categoryDao.getCategory()

    fun getTasksByCategory(categoryId: Int): LiveData<List<task>> {
        return taskDao.getTasksByCategory(categoryId)
    }
    fun getTasksToArchive(): LiveData<List<task>> {
        return taskDao.getTasksToArchive()
    }

    fun searchTasks(query: String): Flow<List<task>> {
        return taskDao.searchTasks(query)
    }

    fun selectTaskToday(start: Long, end: Long): LiveData<List<task>> {
        return taskDao.selectTaskToday(start, end)
    }

    fun updateTask(categoryId: Int?, id: Int){
        coroutineScope.launch(Dispatchers.IO) {
            taskDao.updateTask(categoryId, id)
        }
    }

    fun updateTasktoCategory(categoryId: Int?, id: Int){
        coroutineScope.launch(Dispatchers.IO) {
            taskDao.updateTasktoCategory(categoryId, id)
        }
    }

    suspend fun addTask(task: task): Int {
        return taskDao.addTask(task).toInt()
    }

    fun deleteTask(id:Int){
        coroutineScope.launch(Dispatchers.IO){
            taskDao.deleteTask(id)
        }
    }

    fun addCategory(category: category) {
        coroutineScope.launch(Dispatchers.IO){
            categoryDao.addCategory(category)
        }
    }

    fun deleteCategory(id:Int){
        coroutineScope.launch(Dispatchers.IO){
            categoryDao.deleteCategory(id)
        }
    }

    fun updateTaskDate(taskId: Int, newDate: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            taskDao.updateTaskDate(taskId, newDate)
        }
    }

    suspend fun getDoneTasks() = taskDao.getDoneTasks()

    suspend fun getNotDoneTasks() = taskDao.getNotDoneTasks()

    suspend fun getTasksByCategoryStats() : List<CategoryStat> {
       return taskDao.getTasksByCategoryStats()
    }

    suspend fun getProductivityByDay() =
        taskDao.getProductivityByDay()



    suspend fun getAllTasks(): List<task> {
        return taskDao.getAllTasks()
    }

    fun updateTaskDone(id: Int, state: Boolean) {
        taskDao.updateTaskDone(id, state)
    }

    suspend fun getTaskById(id: Int): task {
        return taskDao.getTaskById(id)
    }
}