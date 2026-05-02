package com.example.task_tracker.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.task_tracker.data.model.CategoryStat
import com.example.task_tracker.data.model.DayStat
import com.example.task_tracker.data.model.task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao{
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId AND isdone = false")
    fun getTasksByCategory(categoryId: Int): LiveData<List<task>>

    @Query("SELECT * FROM tasks WHERE isdone = true")
    fun getTasksToArchive(): LiveData<List<task>>

    @Query("UPDATE tasks SET isdone = :state WHERE taskId = :id")
    fun updateTaskDone(id: Int, state: Boolean)

    @Query("""
    SELECT * FROM tasks 
    WHERE isdone = false 
    AND name LIKE '%' || :query || '%'
""")
    fun searchTasks(query: String): Flow<List<task>>

    @Insert
    suspend fun addTask(task: task): Long

    @Query("DELETE FROM tasks WHERE taskId = :id")
    fun deleteTask(id: Int)

    @Query("UPDATE tasks SET isdone = true WHERE categoryId = :categoryId AND taskId = :id")
    fun updateTask(categoryId: Int?, id: Int)

    @Query("UPDATE tasks SET isdone = false WHERE categoryId = :categoryId AND taskId = :id")
    fun updateTasktoCategory(categoryId: Int?, id: Int)

    @Query("SELECT * FROM tasks WHERE date BETWEEN :start AND :end AND isdone = false")
    fun selectTaskToday(start: Long, end: Long): LiveData<List<task>>

    @Query("UPDATE tasks SET date = :newDate WHERE taskId = :taskId")
    fun updateTaskDate(taskId: Int, newDate: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE isdone = true")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isdone = false")
    suspend fun getNotCompletedCount(): Int


    @Query("SELECT * FROM tasks WHERE isdone = 1")
    suspend fun getDoneTasks(): List<task>

    @Query("SELECT * FROM tasks WHERE isdone = 0")
    suspend fun getNotDoneTasks(): List<task>
    @Query("""
SELECT categoryId, COUNT(*) as count
FROM tasks
WHERE categoryId IS NOT NULL
GROUP BY categoryId
""")
    suspend fun getTasksByCategoryStats(): List<CategoryStat>



    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<task>

    @Query("""
    SELECT 
        DATE(date / 1000, 'unixepoch') AS day,
        COUNT(*) AS count
    FROM tasks
    WHERE date IS NOT NULL
    GROUP BY day
    ORDER BY day
""")
    suspend fun getProductivityByDay(): List<DayStat>
    @Query("SELECT * FROM tasks WHERE taskId = :id")
    suspend fun getTaskById(id: Int): task
}