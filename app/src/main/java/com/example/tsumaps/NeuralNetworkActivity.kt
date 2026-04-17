package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.tsumaps.algorithms.neural.NeuralNetwork
import com.example.tsumaps.ui.theme.TSUMapsTheme

const val GRID_SIZE = 50

class NeuralNetworkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                NeuralNetworkScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralNetworkScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    val context = LocalContext.current

    val pixels = remember { Array(GRID_SIZE) { BooleanArray(GRID_SIZE) { false } } }
    var pixelVersion by remember { mutableStateOf(0) }
    var predictedDigit by remember { mutableStateOf<Int?>(null) }
    var confidence by remember { mutableStateOf(0f) }
    var neuralNet by remember { mutableStateOf<NeuralNetwork?>(null) }
    var loadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val nn = NeuralNetwork()
            nn.loadWeights(context)
            neuralNet = nn
        } catch (e: Exception) {
            loadError = "Файл весов не найден. Сначала обучите нейросеть на Python."
        }
    }

    fun predict() {
        neuralNet?.let { nn ->
            val flat = pixels.flatMap { row -> row.map { if (it) 1f else 0f } }
            val result = nn.predict(flat)
            predictedDigit = result.first
            confidence = result.second
        }
    }

    fun paintCell(offset: Offset, canvasSizePx: Float) {
        val cellSize = canvasSizePx / GRID_SIZE
        val col = (offset.x / cellSize).toInt()
        val row = (offset.y / cellSize).toInt()
        if (row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
            pixels[row][col] = true
            pixelVersion++
            predict()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нейросеть — оценка заведения", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tsuBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Нарисуйте цифру (оценку от 0 до 9)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Рисуйте пальцем — закрашивает пиксели", fontSize = 13.sp, color = Color.Gray)

            if (loadError.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEEEE))
                ) {
                    Text(loadError, modifier = Modifier.padding(12.dp), color = Color(0xFFB00020), fontSize = 14.sp)
                }
            }
            @Suppress("UnusedBoxWithConstraintsScope")
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
            ) {
                val canvasSizePx = constraints.maxWidth.toFloat()

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                paintCell(change.position, canvasSizePx)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                paintCell(offset, canvasSizePx)
                            }
                        }
                ) {
                    val dummy = pixelVersion
                    val cellSize = canvasSizePx / GRID_SIZE

                    for (row in 0 until GRID_SIZE) {
                        for (col in 0 until GRID_SIZE) {
                            drawRect(
                                color = if (pixels[row][col]) Color.Black else Color.White,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                            drawRect(
                                color = Color(0xFFDDDDDD),
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize, cellSize),
                                style = Stroke(width = 0.3f)
                            )
                        }
                    }
                }
            }

            if (predictedDigit != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Распознана цифра:", fontSize = 14.sp, color = Color.Gray)
                            Text("$predictedDigit", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = tsuBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Уверенность:", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                "${"%.1f".format(confidence * 100)}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (confidence > 0.7f) Color(0xFF2E7D32) else Color(0xFFF57C00)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    for (r in 0 until GRID_SIZE) {
                        for (c in 0 until GRID_SIZE) {
                            pixels[r][c] = false
                        }
                    }
                    pixelVersion++
                    predictedDigit = null
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Очистить", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
