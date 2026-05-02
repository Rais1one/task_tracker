package com.example.task_tracker.data.remote

import com.example.task_tracker.data.model.task
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseSyncRepository {

    private val db = FirebaseFirestore.getInstance()

    fun syncTask(userId: String, task: task) {

        val data = hashMapOf(
            "name" to task.name,
            "description" to task.description,
            "date" to task.date,
            "priority" to task.priority,
            "repeat" to task.repeat,
            "reminder" to task.reminder,
            "isdone" to task.isdone,
            "categoryId" to task.categoryId
        )

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.firebaseId.ifEmpty { task.taskId.toString() })
            .set(data)
    }

    fun deleteTask(userId: String, firebaseId: String) {
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(firebaseId)
            .delete()
    }

    fun listenTasks(userId: String, onChange: (List<task>) -> Unit) {

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->

                if (snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->

                    task(
                        firebaseId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description"),
                        date = doc.getLong("date"),
                        priority = doc.getString("priority"),
                        repeat = doc.getString("repeat"),
                        reminder = doc.getLong("reminder")?.toInt(),
                        isdone = doc.getBoolean("isdone") ?: false,
                        categoryId = doc.getLong("categoryId")?.toInt()
                    )
                }

                onChange(list)
            }
    }
}