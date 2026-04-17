package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.algorithms.A_star.AStar
import com.example.tsumaps.ui.theme.TSUMapsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

private enum class RouteMode { NAVIGATE, OBSTACLES }
private enum class SearchState { IDLE, SEARCHING, DONE, NO_PATH }

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

    var mode by remember { mutableStateOf(RouteMode.NAVIGATE) }
    var searchState by remember { mutableStateOf(SearchState.IDLE) }
    val obstacles = remember { mutableStateOf(setOf<Pair<Int,Int>>()) }
    var visitedCells by remember { mutableStateOf(setOf<Pair<Int,Int>>()) }
    var frontierCells by remember { mutableStateOf(setOf<Pair<Int,Int>>()) }
    var animCurrentCell by remember { mutableStateOf<Pair<Int,Int>?>(null) }

    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)

    fun runSearch() {
        if (startPoint == null || endPoint == null) return
        searchState = SearchState.SEARCHING
        visitedCells = emptySet()
        frontierCells = emptySet()
        path = null

        coroutineScope.launch {
            val matrix = withContext(Dispatchers.Default) {
                MapDataLoader.loadMatrix(context, "map_matrix.json")
            }
            matrixSize = Pair(matrix[0].size, matrix.size)

            val obstaclesCopy = obstacles.value
            for ((ox, oy) in obstaclesCopy) {
                if (oy in matrix.indices && ox in matrix[0].indices) {
                    matrix[oy][ox] = 999999
                }
            }

            val startX = (startPoint!!.x * matrixSize.first / mapImage.width).toInt()
                .coerceIn(0, matrixSize.first - 1)
            val startY = (startPoint!!.y * matrixSize.second / mapImage.height).toInt()
                .coerceIn(0, matrixSize.second - 1)
            val endX = (endPoint!!.x * matrixSize.first / mapImage.width).toInt()
                .coerceIn(0, matrixSize.first - 1)
            val endY = (endPoint!!.y * matrixSize.second / mapImage.height).toInt()
                .coerceIn(0, matrixSize.second - 1)

            val aStar = AStar(matrix)
            var resultPath: List<IntArray>? = null

            withContext(Dispatchers.Default) {
                resultPath = aStar.findPathAnimated(startX, startY, endX, endY) { vis, front, cur ->
                    launch(Dispatchers.Main) {
                        visitedCells = vis
                        frontierCells = front
                        animCurrentCell = cur
                    }
                }
            }

            delay(100)
            path = resultPath
            animCurrentCell = null
            searchState = if (resultPath != null) SearchState.DONE else SearchState.NO_PATH
        }
    }

    val tsuBlue2 = colorResource(id = R.color.tsu_blue_primary)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A* Навигация по карте", color = Color.White) },
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tsuBlue.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeChip(
                    label = "Навигация",
                    selected = mode == RouteMode.NAVIGATE,
                    tsuBlue = tsuBlue,
                    onClick = { mode = RouteMode.NAVIGATE }
                )
                ModeChip(
                    label = "Препятствия",
                    selected = mode == RouteMode.OBSTACLES,
                    tsuBlue = tsuBlue,
                    onClick = { mode = RouteMode.OBSTACLES }
                )

                Spacer(Modifier.weight(1f))

                if (obstacles.value.isNotEmpty()) {
                    TextButton(
                        onClick = { obstacles.value = emptySet() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Очистить заграждения", fontSize = 11.sp, color = Color(0xFFB00020))
                    }
                }
            }

            if (searchState == SearchState.NO_PATH) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Путь не найден — попробуйте другие точки или уберите препятствия",
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (searchState == SearchState.SEARCHING) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = tsuBlue
                )
            }

            val hintText = when {
                mode == RouteMode.OBSTACLES -> "Нажмите на карту чтобы расставить заграждения"
                startPoint == null -> "Нажмите на карту — начальная точка"
                endPoint == null -> "Нажмите на карту — конечная точка"
                else -> "Нажмите снова чтобы сбросить маршрут"
            }
            Text(
                hintText,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                InteractiveCampusMap(
                    mapImage = mapImage,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    path = path,
                    gridWidth = matrixSize.first,
                    gridHeight = matrixSize.second,
                    initialScale = 0.7f,
                    visitedCells = visitedCells,
                    frontierCells = frontierCells,
                    animCurrentCell = animCurrentCell,
                    obstacles = obstacles.value,
                    onMapClick = { clickedOffset ->
                        if (mode == RouteMode.OBSTACLES) {
                            if (matrixSize.first == 0) return@InteractiveCampusMap
                            val gx = (clickedOffset.x * matrixSize.first / mapImage.width).toInt()
                                .coerceIn(0, matrixSize.first - 1)
                            val gy = (clickedOffset.y * matrixSize.second / mapImage.height).toInt()
                                .coerceIn(0, matrixSize.second - 1)
                            val key = gx to gy
                            obstacles.value = if (obstacles.value.contains(key))
                                obstacles.value - key else obstacles.value + key
                        } else {
                            when {
                                startPoint == null -> {
                                    startPoint = clickedOffset
                                    if (matrixSize.first == 0) {
                                        coroutineScope.launch(Dispatchers.Default) {
                                            val m = MapDataLoader.loadMatrix(context, "map_matrix.json")
                                            matrixSize = Pair(m[0].size, m.size)
                                        }
                                    }
                                }
                                endPoint == null -> {
                                    endPoint = clickedOffset
                                    runSearch()
                                }
                                else -> {
                                    startPoint = clickedOffset
                                    endPoint = null
                                    path = null
                                    visitedCells = emptySet()
                                    frontierCells = emptySet()
                                    searchState = SearchState.IDLE
                                }
                            }
                        }
                    }
                )
            }

            if (startPoint != null && endPoint != null && searchState != SearchState.SEARCHING) {
                Button(
                    onClick = { runSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Повторить поиск", fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, tsuBlue: Color, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) tsuBlue else Color.Transparent,
        animationSpec = tween(200)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else tsuBlue,
        animationSpec = tween(200)
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
