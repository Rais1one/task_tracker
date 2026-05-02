package com.example.task_tracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.task_tracker.service.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Задача"
        val desc = intent.getStringExtra("desc") ?: ""
        val id = intent.getIntExtra("id", System.currentTimeMillis().toInt())

        NotificationHelper.createChannel(context)
        NotificationHelper.showNotification(context, id, title, desc)
    }
}