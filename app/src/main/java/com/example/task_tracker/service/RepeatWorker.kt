package com.example.task_tracker.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RepeatWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val title = inputData.getString("title") ?: return Result.failure()
        val desc = inputData.getString("desc") ?: ""
        val taskId = inputData.getInt("taskId", 0)

        NotificationHelper.showNotification(
            applicationContext,
            taskId,
            title,
            desc
        )

        return Result.success()
    }
}