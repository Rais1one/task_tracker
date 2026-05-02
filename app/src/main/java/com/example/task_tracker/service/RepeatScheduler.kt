package com.example.task_tracker.service

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.task_tracker.service.RepeatWorker
import java.util.concurrent.TimeUnit

object RepeatScheduler {

    fun schedule(
        context: Context,
        taskId: Int,
        title: String,
        desc: String,
        intervalMillis: Long
    ) {

        val work = OneTimeWorkRequestBuilder<RepeatWorker>()
            .setInitialDelay(intervalMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "taskId" to taskId,
                    "title" to title,
                    "desc" to desc
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "repeat_$taskId",
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    fun cancel(context: Context, taskId: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("repeat_$taskId")
    }
}