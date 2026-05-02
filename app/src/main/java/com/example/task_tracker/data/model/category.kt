package com.example.task_tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorys")
data class category (@PrimaryKey(autoGenerate = true)
                     val categoryId: Int = 0,
                     val name: String)
{

}