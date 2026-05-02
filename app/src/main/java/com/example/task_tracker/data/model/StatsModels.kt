package com.example.task_tracker.data.model

data class CategoryStat(
    val categoryId: Int,
    val count: Int
)

data class DayStat(
    val day: Long,
    val count: Int
)