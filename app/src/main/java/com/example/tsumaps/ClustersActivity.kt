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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.example.tsumaps.algorithms.k_means.KMeans
import com.example.tsumaps.algorithms.k_means.Point
import com.example.tsumaps.ui.theme.TSUMapsTheme

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
    val kmeansPoints = remember { mutableStateListOf<Point>() }
    val mapImage = ImageBitmap.imageResource(id = R.drawable.tsu_map_photo)

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
                    containerColor = colorResource(id = R.color.tsu_blue_primary)
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { kmeansPoints.clear() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Очистить все точки", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            InteractiveCampusMap(
                mapImage = mapImage,
                kmeansPoints = kmeansPoints,
                onMapClick = { clickedOffset ->
                    kmeansPoints.add(Point(clickedOffset.x.toDouble(), clickedOffset.y.toDouble()))

                    if (kmeansPoints.size >= 3) {
                        val algorithm = KMeans(3, kmeansPoints.toList())
                        algorithm.run()

                        val updated = kmeansPoints.toList()
                        kmeansPoints.clear()
                        kmeansPoints.addAll(updated)
                    }
                }
            )
        }
    }
}