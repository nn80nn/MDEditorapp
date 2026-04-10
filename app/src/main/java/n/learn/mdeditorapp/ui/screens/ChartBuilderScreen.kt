package n.learn.mdeditorapp.ui.screens

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartBuilderScreen(
    onInsertChart: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var chartType by remember { mutableStateOf("bar") }
    var labelsInput by remember { mutableStateOf("Янв,Фев,Мар,Апр") }
    var valuesInput by remember { mutableStateOf("10,25,15,30") }
    var chartTitle by remember { mutableStateOf("Мой график") }
    var chartViewRef by remember { mutableStateOf<View?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val labels = labelsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val values = valuesInput.split(",").mapNotNull { it.trim().toFloatOrNull() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Построить график") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = chartTitle,
                onValueChange = { chartTitle = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Тип: ", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = chartType == "bar",
                    onClick = { chartType = "bar" },
                    label = { Text("Столбчатый") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = chartType == "line",
                    onClick = { chartType = "line" },
                    label = { Text("Линейный") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = labelsInput,
                onValueChange = { labelsInput = it },
                label = { Text("Подписи (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = valuesInput,
                onValueChange = { valuesInput = it },
                label = { Text("Значения (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // превью графика
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (chartType == "bar") {
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).also { chartViewRef = it }
                        },
                        update = { chart ->
                            chartViewRef = chart
                            val entries = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
                            val dataset = BarDataSet(entries, chartTitle).apply {
                                colors = ColorTemplate.MATERIAL_COLORS.toList()
                            }
                            chart.data = BarData(dataset)
                            chart.description.text = chartTitle
                            chart.animateY(500)
                            chart.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            LineChart(ctx).also { chartViewRef = it }
                        },
                        update = { chart ->
                            chartViewRef = chart
                            val entries = values.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                            val dataset = LineDataSet(entries, chartTitle).apply {
                                color = ColorTemplate.MATERIAL_COLORS[0]
                                setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
                            }
                            chart.data = LineData(dataset)
                            chart.description.text = chartTitle
                            chart.animateY(500)
                            chart.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val view = chartViewRef
                    if (view == null || values.isEmpty()) {
                        error = "нет данных для графика"
                        return@Button
                    }
                    try {
                        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        view.draw(canvas)

                        val dir = File(context.cacheDir, "charts")
                        dir.mkdirs()
                        val file = File(dir, "chart_${System.currentTimeMillis()}.png")
                        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                        onInsertChart(file.absolutePath)
                    } catch (e: Exception) {
                        error = "не удалось сохранить: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Вставить в документ")
            }
        }
    }
}
