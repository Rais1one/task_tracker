package com.example.task_tracker.data.model

data class TaskFilter(
    val categoryId: Int? = null,
    val priority: String? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null
)