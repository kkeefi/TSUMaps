package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.algorithms.k_means.KMeans
import com.example.tsumaps.algorithms.k_means.KMeansMetric
import com.example.tsumaps.algorithms.k_means.Point
import com.example.tsumaps.ui.theme.TSUMapsTheme

private enum class ClusterView { EUCLIDEAN, MANHATTAN, COMPARE }

class ClustersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                ClustersScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClustersScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    val mapImage = ImageBitmap.imageResource(id = R.drawable.tsu_map_photo)

    val baseCoords = remember { mutableStateListOf<Pair<Double, Double>>() }

    var euclidPoints by remember { mutableStateOf<List<Point>>(emptyList()) }
    var manhPoints by remember { mutableStateOf<List<Point>>(emptyList()) }

    var view by remember { mutableStateOf(ClusterView.EUCLIDEAN) }
    var disputedCount by remember { mutableStateOf(0) }

    fun recompute() {
        if (baseCoords.size < 3) {
            euclidPoints = baseCoords.map { Point(it.first, it.second) }
            manhPoints = baseCoords.map { Point(it.first, it.second) }
            disputedCount = 0
            return
        }
        val ep = baseCoords.map { Point(it.first, it.second) }
        val mp = baseCoords.map { Point(it.first, it.second) }
        KMeans(3, ep, KMeansMetric.EUCLIDEAN).run()
        KMeans(3, mp, KMeansMetric.MANHATTAN).run()
        euclidPoints = ep
        manhPoints = mp
        disputedCount = ep.indices.count { ep[it].clusterNumber != mp[it].clusterNumber }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кластеризация (K-Means)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tsuBlue
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricTabButton(
                            label = "Евклид",
                            selected = view == ClusterView.EUCLIDEAN,
                            tsuBlue = tsuBlue,
                            modifier = Modifier.weight(1f)
                        ) { view = ClusterView.EUCLIDEAN }

                        MetricTabButton(
                            label = "Манхэттен",
                            selected = view == ClusterView.MANHATTAN,
                            tsuBlue = tsuBlue,
                            modifier = Modifier.weight(1f)
                        ) { view = ClusterView.MANHATTAN }

                        MetricTabButton(
                            label = if (disputedCount > 0) "Сравнение ($disputedCount ⚡)" else "Сравнение",
                            selected = view == ClusterView.COMPARE,
                            tsuBlue = tsuBlue,
                            modifier = Modifier.weight(1f)
                        ) { view = ClusterView.COMPARE }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildString {
                                append("Точек: ${baseCoords.size}")
                                if (baseCoords.size < 3) append("  (нужно ≥ 3)")
                                else append("  •  3 кластера")
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        if (baseCoords.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    baseCoords.clear()
                                    euclidPoints = emptyList()
                                    manhPoints = emptyList()
                                    disputedCount = 0
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Очистить", fontSize = 12.sp, color = Color(0xFFB00020))
                            }
                        }
                    }

                    if (view == ClusterView.COMPARE && baseCoords.size >= 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = Color(0xFF1565C0), label = "Кластер по Евклиду")
                            if (disputedCount > 0) {
                                LegendItem(color = Color(0xFFE91E63), label = "⚡ Расходятся ($disputedCount)")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val displayPoints = when (view) {
                ClusterView.EUCLIDEAN -> euclidPoints
                ClusterView.MANHATTAN -> manhPoints
                ClusterView.COMPARE -> euclidPoints
            }
            val compareArg = if (view == ClusterView.COMPARE) manhPoints else emptyList()

            InteractiveCampusMap(
                mapImage = mapImage,
                kmeansPoints = displayPoints,
                comparePoints = compareArg,
                showComparison = view == ClusterView.COMPARE,
                onMapClick = { offset ->
                    baseCoords.add(offset.x.toDouble() to offset.y.toDouble())
                    recompute()
                }
            )
        }
    }
}

@Composable
private fun MetricTabButton(
    label: String,
    selected: Boolean,
    tsuBlue: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) tsuBlue else Color(0xFFE0E0E0),
        animationSpec = tween(200)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.DarkGray,
        animationSpec = tween(200)
    )
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(5.dp))
        )
        Text(label, fontSize = 11.sp, color = Color.DarkGray)
    }
}
