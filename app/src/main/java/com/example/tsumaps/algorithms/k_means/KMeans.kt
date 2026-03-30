package com.example.tsumaps.algorithms.k_means

import kotlin.math.sqrt

class KMeans (val n: Int, val points: List<Point>)
{
    val centroids = ArrayList<Centroid>()

    fun findDistance(p: Point, c: Centroid): Double
    {
        val dx = p.x - c.x
        val dy = p.y - c.y
        return sqrt(dx * dx + dy * dy)
    }

    fun run()
    {
        centroids.clear()
        for (i in 0 until n)
        {
            if (i < points.size)
            {
                val p = points[i]
                centroids.add(Centroid(p.x, p.y))
            }
        }

        var changed = true
        while (changed)
        {
            changed = false
            for (p in points)
            {
                var minDist = Double.MAX_VALUE
                var bestClusterIndex = -1

                for (i in 0 until centroids.size)
                {
                    val dist = findDistance(p, centroids[i])
                    if (dist < minDist)
                    {
                        minDist = dist
                        bestClusterIndex = i
                    }
                }

                if (p.clusterNumber != bestClusterIndex)
                {
                    p.clusterNumber = bestClusterIndex
                    changed = true
                }
            }

            if (changed)
            {
                for (i in 0 until centroids.size) {
                    var sumX = 0.0
                    var sumY = 0.0
                    var count = 0

                    for (p in points) {
                        if (p.clusterNumber == i) {
                            sumX += p.x
                            sumY += p.y
                            count++
                        }
                    }

                    if (count > 0) {
                        centroids[i].x = sumX / count
                        centroids[i].y = sumY / count
                    }
                }
            }
        }
    }
}