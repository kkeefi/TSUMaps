package com.example.tsumaps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    kmeansPoints: List<Point> = emptyList(),
    startPoint: Offset? = null,
    endPoint: Offset? = null,
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
                detectTapGestures { tapOffset ->
                    val imgX = (tapOffset.x - offset.x) / scale
                    val imgY = (tapOffset.y - offset.y) / scale
                    if (imgX in 0f..mapImage.width.toFloat() && imgY in 0f..mapImage.height.toFloat()) {
                        onMapClick(Offset(imgX, imgY))
                    }
                }
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawImage(image = mapImage)

            path?.let { p ->
                if (p.isNotEmpty() && gridWidth > 0) {
                    val drawPath = Path()
                    val cellW = mapImage.width.toFloat() / gridWidth
                    val cellH = mapImage.height.toFloat() / gridHeight
                    for (i in p.indices) {
                        val px = p[i][0] * cellW + (cellW / 2f)
                        val py = p[i][1] * cellH + (cellH / 2f)
                        if (i == 0) drawPath.moveTo(px, py) else drawPath.lineTo(px, py)
                    }
                    drawPath(
                        path = drawPath,
                        color = Color(0xFFD4AF37),
                        style = Stroke(width = 10f / scale)
                    )
                }
            }

            startPoint?.let { drawCircle(Color.Red, 15f / scale, it) }
            endPoint?.let { drawCircle(Color.Green, 15f / scale, it) }

            kmeansPoints.forEach { kp ->
                val color = when (kp.clusterNumber) {
                    0 -> Color.Blue
                    1 -> Color.Magenta
                    2 -> Color.Cyan
                    else -> Color.Gray
                }
                drawCircle(color, 12f / scale, Offset(kp.x.toFloat(), kp.y.toFloat()))
            }
        }
    }
}
