package com.example.task_tracker.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.task_tracker.data.model.category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categorys WHERE categoryId != 1")
    fun getCategory(): LiveData<List<category>>

    @Insert
    fun addCategory(category: category)

    @Query("DELETE FROM categorys WHERE categoryId = :id")
    fun deleteCategory(id: Int)


}