package com.example.task_tracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.task_tracker.data.model.category

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.Companion.SET_NULL
        )
    ],
    indices = [
        Index("categoryId")
    ]
)
data class task(

    @PrimaryKey(autoGenerate = true)
    val taskId: Int = 0,

    val firebaseId: String = "",

    val name: String,
    val description: String? = null,
    val date: Long? = null,
    val priority: String? = null,
    val repeat: String? = null,
    val reminder: Int? = null,
    val isdone: Boolean = false,
    val categoryId: Int? = null,
    val notificationsEnabled: Boolean = true
)