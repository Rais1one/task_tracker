package com.example.task_tracker.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.task_tracker.data.model.CategoryStat
import com.example.task_tracker.data.model.DayStat
import com.example.task_tracker.presentation.viewmodel.TaskViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CardBlue = Color(0xFF007AFF)
private val CardRed = Color(0xFFFF3B30)
private val CardOrange = Color(0xFFFF9500)
private val CardGreen = Color(0xFF34C759)
private val CardPurple = Color(0xFFAF52DE)

@Composable
fun StatisticsScreen(viewModel: TaskViewModel) {

    val context = LocalContext.current
    val scroll = rememberScrollState()

    val categories = viewModel.categoryStats.value
    val days = viewModel.dayStats.value
    val done = viewModel.doneTasks.value
    val notDone = viewModel.notDoneTasks.value
    val mode = viewModel.statsMode

    LaunchedEffect(mode) {
        viewModel.loadCounts()
        viewModel.loadCategoryStats()
        viewModel.loadProductivity()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp)
    ) {

        Text(
            text = "Статистика",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        SegmentedControl(mode, viewModel)

        Spacer(Modifier.height(16.dp))

        Row {
            StatCard("Выполнено", done.toString(), CardGreen, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            StatCard("Не выполнено", notDone.toString(), CardRed, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text("Категории", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        AppleContainer {
            if (categories.isNotEmpty()) {
                CategoryPieChart(categories)
            } else {
                EmptyState()
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Продуктивность", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        AppleContainer {
            if (days.isNotEmpty()) {
                ProductivityChart(days, mode)
            } else {
                EmptyState()
            }
        }

        Spacer(Modifier.height(20.dp))

        Row {
            AppleButton("CSV") { viewModel.exportToCSV(context) {} }
            Spacer(Modifier.width(12.dp))
            AppleButton("PDF") { viewModel.exportToPDF(context) {} }
        }

        Spacer(Modifier.height(40.dp))
    }
}



@Composable
fun SegmentedControl(
    mode: TaskViewModel.StatsMode,
    viewModel: TaskViewModel
) {
    val items = listOf("День", "Неделя", "Месяц")

    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, text ->

            val selected = mode.ordinal == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected) Color.White else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        viewModel.statsMode =
                            TaskViewModel.StatsMode.values()[index]
                        viewModel.loadProductivity()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}



@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White.copy(alpha = 0.8f))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}



@Composable
fun AppleButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(CardBlue, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Medium)
    }
}



@Composable
fun EmptyState() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Нет данных", color = Color.Gray)
    }
}



@Composable
fun CategoryPieChart(list: List<CategoryStat>) {

    val total = list.sumOf { it.count }.coerceAtLeast(1)
    val colors = listOf(CardGreen, CardOrange, CardBlue, CardRed, CardPurple)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        list.forEachIndexed { index, item ->
            val sweep = (item.count.toFloat() / total) * 360f

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter)
            )

            startAngle += sweep
        }
    }
}



@Composable
fun ProductivityChart(
    list: List<DayStat>,
    mode: TaskViewModel.StatsMode
) {
    val max = list.maxOfOrNull { it.count } ?: 1

    val formatter = DateTimeFormatter.ofPattern("dd.MM")

    Column {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val step = size.width / list.size
            val barWidth = step * 0.5f

            list.forEachIndexed { index, item ->
                val barHeight = (item.count.toFloat() / max) * size.height

                drawRoundRect(
                    color = CardBlue,
                    topLeft = Offset(
                        x = index * step + (step - barWidth) / 2,
                        y = size.height - barHeight
                    ),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Spacer(Modifier.height(8.dp))


        Row(Modifier.fillMaxWidth()) {
            list.forEach { item ->

                val date = Instant.ofEpochMilli(item.day)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                Text(
                    text = date.format(formatter),
                    modifier = Modifier.weight(1f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}



@Composable
fun AppleContainer(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(Modifier.padding(12.dp)) {
            content()
        }
    }
}