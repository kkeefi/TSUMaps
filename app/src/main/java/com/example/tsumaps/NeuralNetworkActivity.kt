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
            loadError = "Файл весов не найден. Сначала обучите нейросеть."
        }
    }

    fun predict() {
        neuralNet?.let { nn ->
            var minR = GRID_SIZE; var maxR = -1; var minC = GRID_SIZE; var maxC = -1
            var hasData = false
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    if (pixels[r][c]) {
                        if (r < minR) minR = r; if (r > maxR) maxR = r
                        if (c < minC) minC = c; if (c > maxC) maxC = c
                        hasData = true
                    }
                }
            }

            if (hasData) {
                val inputFlat = DoubleArray(GRID_SIZE * GRID_SIZE)
                val h = maxR - minR + 1; val w = maxC - minC + 1
                val dr = (GRID_SIZE - h) / 2; val dc = (GRID_SIZE - w) / 2

                for (r in minR..maxR) {
                    for (c in minC..maxC) {
                        if (pixels[r][c]) {
                            val targetIdx = (dr + r - minR) * GRID_SIZE + (dc + c - minC)
                            if (targetIdx in inputFlat.indices) inputFlat[targetIdx] = 1.0
                        }
                    }
                }
                val result = nn.predict(inputFlat)
                predictedDigit = result.first
                confidence = result.second.toFloat()
            } else {
                predictedDigit = null
            }
        }
    }

    fun paint(offset: Offset, sizePx: Float) {
        val cellSize = sizePx / GRID_SIZE
        val col = (offset.x / cellSize).toInt()
        val row = (offset.y / cellSize).toInt()

        if (row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
            if (!pixels[row][col]) {
                pixels[row][col] = true
                pixelVersion++
                predict()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оценка заведения", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tsuBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (loadError.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Text(loadError, color = Color.Red, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1f).border(1.dp, Color.Gray).background(Color.White)) {
                val sizePx = constraints.maxWidth.toFloat()
                Canvas(modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectDragGestures { change, _ -> change.consume(); paint(change.position, sizePx) } }
                    .pointerInput(Unit) { detectTapGestures { paint(it, sizePx) } }
                ) {
                    val dummy = pixelVersion
                    val cellSize = sizePx / GRID_SIZE

                    for (r in 0 until GRID_SIZE) {
                        for (c in 0 until GRID_SIZE) {
                            if (pixels[r][c]) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(c * cellSize - (cellSize / 2), r * cellSize - (cellSize / 2)),
                                    size = Size(cellSize * 2f, cellSize * 2f)
                                )
                            }
                        }
                    }
                }
            }

            if (predictedDigit != null) {
                Card(
                    modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Результат:", fontSize = 14.sp, color = Color.Gray)
                        Text("$predictedDigit", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = tsuBlue)
                        Text("Уверенность: ${(confidence * 100).toInt()}%", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    for (r in 0 until GRID_SIZE) for (c in 0 until GRID_SIZE) pixels[r][c] = false
                    pixelVersion++; predictedDigit = null
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Очистить поле", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}