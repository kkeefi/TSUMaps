package com.example.tsumaps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import com.example.tsumaps.algorithms.k_means.Point

@Composable
fun InteractiveCampusMap(
    mapImage: ImageBitmap,
    path: List<IntArray>? = null,
    startPoint: Offset? = null,
    endPoint: Offset? = null,
    kmeansPoints: List<Point> = emptyList(),
    comparePoints: List<Point> = emptyList(),
    showComparison: Boolean = false,
    visitedCells: Set<Pair<Int, Int>> = emptySet(),
    frontierCells: Set<Pair<Int, Int>> = emptySet(),
    animCurrentCell: Pair<Int, Int>? = null,
    obstacles: Set<Pair<Int, Int>> = emptySet(),
    gridWidth: Int = 100,
    gridHeight: Int = 100,
    initialScale: Float = 1f,
    onMapClick: (Offset) -> Unit
) {
    var scale by remember { mutableFloatStateOf(initialScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.3f, 10f)
                    val newOffset = (offset + pan) * (newScale / oldScale) -
                            centroid * (newScale / oldScale - 1f)
                    val limitX = (mapImage.width * newScale - size.width).coerceAtLeast(0f)
                    val limitY = (mapImage.height * newScale - size.height).coerceAtLeast(0f)
                    offset = Offset(
                        x = newOffset.x.coerceIn(-limitX, 0f),
                        y = newOffset.y.coerceIn(-limitY, 0f)
                    )
                    scale = newScale
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val ix = (tap.x - offset.x) / scale
                    val iy = (tap.y - offset.y) / scale
                    if (ix in 0f..mapImage.width.toFloat() && iy in 0f..mapImage.height.toFloat()) {
                        onMapClick(Offset(ix, iy))
                    }
                }
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawImage(image = mapImage)

            val cellW = mapImage.width.toFloat() / gridWidth
            val cellH = mapImage.height.toFloat() / gridHeight

            visitedCells.forEach { (gx, gy) ->
                drawRect(
                    color = Color(0x33888888),
                    topLeft = Offset(gx * cellW, gy * cellH),
                    size = Size(cellW, cellH)
                )
            }

            frontierCells.forEach { (gx, gy) ->
                drawRect(
                    color = Color(0x88FFC107),
                    topLeft = Offset(gx * cellW, gy * cellH),
                    size = Size(cellW, cellH)
                )
            }

            animCurrentCell?.let { (gx, gy) ->
                drawRect(
                    color = Color(0xCCFF5722),
                    topLeft = Offset(gx * cellW, gy * cellH),
                    size = Size(cellW, cellH)
                )
            }

            obstacles.forEach { (gx, gy) ->
                drawRect(
                    color = Color(0xCCE53935),
                    topLeft = Offset(gx * cellW, gy * cellH),
                    size = Size(cellW, cellH)
                )
            }

            path?.let { p ->
                if (p.isNotEmpty()) {
                    val dp = Path()
                    for (i in p.indices) {
                        val px = p[i][0] * cellW + cellW / 2f
                        val py = p[i][1] * cellH + cellH / 2f
                        if (i == 0) dp.moveTo(px, py) else dp.lineTo(px, py)
                    }
                    drawPath(dp, color = Color(0xFFD4AF37), style = Stroke(width = 10f / scale))
                }
            }

            startPoint?.let { drawCircle(Color.Red, 15f / scale, it) }
            endPoint?.let { drawCircle(Color(0xFF43A047), 15f / scale, it) }

            val clusterColors = listOf(
                Color(0xFF1565C0), Color(0xFFAD1457), Color(0xFF00838F),
                Color(0xFF558B2F), Color(0xFF6A1B9A)
            )

            if (showComparison && comparePoints.isNotEmpty() && comparePoints.size == kmeansPoints.size) {
                kmeansPoints.forEachIndexed { idx, kp ->
                    val c1 = kp.clusterNumber
                    val c2 = if (idx < comparePoints.size) comparePoints[idx].clusterNumber else c1
                    val color = clusterColors.getOrElse(c1) { Color.Gray }
                    val center = Offset(kp.x.toFloat(), kp.y.toFloat())

                    drawCircle(color, 14f / scale, center)

                    if (c1 != c2) {
                        drawCircle(
                            color = Color.White,
                            radius = 20f / scale,
                            center = center,
                            style = Stroke(width = 3f / scale)
                        )
                        val c2Color = clusterColors.getOrElse(c2) { Color.Gray }
                        drawCircle(c2Color, 6f / scale, center + Offset(16f / scale, -16f / scale))
                    }
                }
            } else {
                kmeansPoints.forEach { kp ->
                    val color = clusterColors.getOrElse(kp.clusterNumber) { Color.Gray }
                    drawCircle(color, 12f / scale, Offset(kp.x.toFloat(), kp.y.toFloat()))
                }
            }
        }
    }
}
