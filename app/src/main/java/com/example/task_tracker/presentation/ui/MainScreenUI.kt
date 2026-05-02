package com.example.task_tracker.presentation.ui

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.task_tracker.data.model.category
import com.example.task_tracker.presentation.viewmodel.TaskViewModel

private val CardBlue   = Color(0xFF007AFF)
private val CardRed    = Color(0xFFFF3B30)
private val CardOrange = Color(0xFFFF9500)
private val CardGreen  = Color(0xFF34C759)
private val CardPurple = Color(0xFFAF52DE)

private val CategoryColors = listOf(CardGreen, CardPurple, CardOrange, CardBlue, CardRed)

@Composable
fun MainScreen(navController: NavController, viewModel: TaskViewModel) {

    val categories by viewModel.allCategory.observeAsState(emptyList())
    val xp = viewModel.userPoints.value
    val level = viewModel.level
    val progress = viewModel.progress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Поиск",
                            tint = CardBlue
                        )
                    }
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = "Тема",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = CardOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Уровень $level",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = CardBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$xp XP",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = CardBlue
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = CardBlue,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AppleCard(
                        icon = Icons.Rounded.WbSunny,
                        title = "Сегодня",
                        color = CardBlue,
                        onClick = { navController.navigate("category_tasks_today") }
                    )
                }
                item {
                    AppleCard(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Календарь",
                        color = CardRed,
                        onClick = { navController.navigate("calendar") }
                    )
                }
                item {
                    AppleCard(
                        icon = Icons.Rounded.Inventory2,
                        title = "Архив",
                        color = CardOrange,
                        onClick = { navController.navigate("archive") }
                    )
                }
                item {
                    AppleCard(
                        icon = Icons.Rounded.BarChart,
                        title = "Статистика",
                        color = CardPurple,
                        onClick = { navController.navigate("stats") }
                    )
                }

                items(categories) { category ->
                    CategoryItem(category, navController)
                }
            }
        }


        FloatingActionButton(
            onClick = { navController.navigate("add") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            containerColor = CardBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Добавить задачу")
        }


        FloatingActionButton(
            onClick = { navController.navigate("add_category") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.CreateNewFolder, contentDescription = "Добавить категорию")
        }
    }
}



@Composable
fun AppleCard(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun CategoryItem(category: category, navController: NavController) {
    val index = category.name.hashCode().mod(CategoryColors.size)
        .let { if (it < 0) it + CategoryColors.size else it }

    AppleCard(
        icon = Icons.Rounded.FolderOpen,
        title = category.name,
        color = CategoryColors[index],
        onClick = { navController.navigate("category_tasks/${category.categoryId}") }
    )
}