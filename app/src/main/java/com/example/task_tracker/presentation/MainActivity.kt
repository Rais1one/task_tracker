package com.example.task_tracker.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.task_tracker.presentation.ui.AddTaskUI
import com.example.task_tracker.presentation.ui.AddCategoryUI
import com.example.task_tracker.presentation.ui.ArchiveUI
import com.example.task_tracker.presentation.ui.CalendarScreen
import com.example.task_tracker.presentation.ui.CategoryTasksScreenUI
import com.example.task_tracker.presentation.ui.MainScreen
import com.example.task_tracker.presentation.ui.SearchScreen
import com.example.task_tracker.presentation.ui.StatisticsScreen
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import com.example.task_tracker.service.NotificationHelper
import com.example.task_tracker.service.NotificationService
import com.example.task_tracker.ui.theme.Task_trackerTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.getInstance()
            .collection("test")
            .document("ping")
            .set(mapOf("ok" to true))
            .addOnSuccessListener {
                Log.d("FIREBASE", "WRITE OK")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "WRITE FAIL", it)
            }


        val serviceIntent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            Task_trackerTheme(darkTheme = taskViewModel.isDarkTheme) {
                AppNavigation(taskViewModel)
            }
        }
    }
}

@Composable
fun ButtonAdd(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(color = Color.White, shape = CircleShape)
            .clickable { navController.navigate("add") },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = Color.Black
        )
    }
}

@Composable
fun AppNavigation(taskViewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController, taskViewModel)
        }
        composable("add") {
            AddTaskUI(navController, taskViewModel)
        }
        composable("add_category") {
            AddCategoryUI(navController, taskViewModel)
        }
        composable("category_tasks/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toInt()
            CategoryTasksScreenUI(navController, taskViewModel, categoryId ?: 0)
        }
        composable("archive") {
            ArchiveUI(navController, taskViewModel)
        }
        composable("category_tasks_today") {
            CategoryTasksScreenUI(navController, taskViewModel, 0)
        }
        composable("calendar") {
            CalendarScreen(navController, taskViewModel)
        }
        composable("search") {
            SearchScreen(taskViewModel)
        }
        composable("stats") {
            StatisticsScreen(taskViewModel)
        }
    }
}