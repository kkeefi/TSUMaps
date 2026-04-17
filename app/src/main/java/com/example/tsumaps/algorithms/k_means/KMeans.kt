package com.example.tsumaps.algorithms.k_means

import kotlin.math.abs
import kotlin.math.sqrt

enum class KMeansMetric { EUCLIDEAN, MANHATTAN }

class KMeans(val n: Int, val points: List<Point>, val metric: KMeansMetric = KMeansMetric.EUCLIDEAN) {

    val centroids = ArrayList<Centroid>()

    private fun distance(p: Point, c: Centroid): Double = when (metric) {
        KMeansMetric.EUCLIDEAN -> {
            val dx = p.x - c.x; val dy = p.y - c.y
            sqrt(dx * dx + dy * dy)
        }
        KMeansMetric.MANHATTAN -> abs(p.x - c.x) + abs(p.y - c.y)
    }

    fun run() {
        centroids.clear()
        for (i in 0 until minOf(n, points.size)) {
            centroids.add(Centroid(points[i].x, points[i].y))
        }

        var changed = true
        while (changed) {
            changed = false
            for (p in points) {
                var minDist = Double.MAX_VALUE
                var best = -1
                for (i in centroids.indices) {
                    val d = distance(p, centroids[i])
                    if (d < minDist) { minDist = d; best = i }
                }
                if (p.clusterNumber != best) { p.clusterNumber = best; changed = true }
            }

            if (changed) {
                for (i in centroids.indices) {
                    val group = points.filter { it.clusterNumber == i }
                    if (group.isNotEmpty()) {
                        centroids[i].x = group.sumOf { it.x } / group.size
                        centroids[i].y = group.sumOf { it.y } / group.size
                    }
                }
            }
        }
    }
}
