package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import com.example.tsumaps.algorithms.A_star.AStar
import com.example.tsumaps.ui.theme.TSUMapsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                RouteScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var startPoint by remember { mutableStateOf<Offset?>(null) }
    var endPoint by remember { mutableStateOf<Offset?>(null) }
    var path by remember { mutableStateOf<List<IntArray>?>(null) }
    var matrixSize by remember { mutableStateOf(Pair(0, 0)) }
    val mapImage = ImageBitmap.imageResource(id = R.drawable.tsu_map_photo)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A* Навигация по карте", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.tsu_blue_primary)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            InteractiveCampusMap(
                mapImage = mapImage,
                startPoint = startPoint,
                endPoint = endPoint,
                path = path,
                gridWidth = matrixSize.first,
                gridHeight = matrixSize.second,
                initialScale = 0.7f,
                onMapClick = { clickedOffset ->
                    if (startPoint == null) {
                        startPoint = clickedOffset
                    } else if (endPoint == null) {
                        endPoint = clickedOffset
                        coroutineScope.launch(Dispatchers.Default) {
                            val matrix = MapDataLoader.loadMatrix(context, "map_matrix.json")
                            matrixSize = Pair(matrix[0].size, matrix.size)
                            val aStar = AStar(matrix)
                            val startX = (startPoint!!.x * matrixSize.first / mapImage.width).toInt()
                            val startY = (startPoint!!.y * matrixSize.second / mapImage.height).toInt()
                            val endX = (endPoint!!.x * matrixSize.first / mapImage.width).toInt()
                            val endY = (endPoint!!.y * matrixSize.second / mapImage.height).toInt()
                            path = aStar.findPath(
                                startX.coerceIn(0, matrixSize.first - 1),
                                startY.coerceIn(0, matrixSize.second - 1),
                                endX.coerceIn(0, matrixSize.first - 1),
                                endY.coerceIn(0, matrixSize.second - 1)
                            )
                        }
                    } else {
                        startPoint = clickedOffset
                        endPoint = null
                        path = null
                    }
                }
            )
        }
    }
}
