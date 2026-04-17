package com.example.tsumaps.algorithms.ant_colony

import kotlin.math.pow
import kotlin.math.sqrt

data class AntPlace(
    val name: String,
    val x: Double,
    val y: Double
)

class AntColony(
    private val places: List<AntPlace>,
    private val numAnts: Int = 20,
    private val iterations: Int = 100,
    private val alpha: Double = 1.0,
    private val beta: Double = 2.0,
    private val evaporation: Double = 0.5,
    private val q: Double = 100.0
) {
    private val n = places.size
    private val distances = Array(n) { i -> DoubleArray(n) { j -> calcDist(i, j) } }
    private val pheromones = Array(n) { DoubleArray(n) { 1.0 } }

    private fun calcDist(i: Int, j: Int): Double {
        val dx = places[i].x - places[j].x
        val dy = places[i].y - places[j].y
        val d = sqrt(dx * dx + dy * dy)
        return if (d < 0.0001) 0.0001 else d
    }

    private fun routeLength(route: List<Int>): Double {
        var total = 0.0
        for (i in 0 until route.size - 1) {
            total += distances[route[i]][route[i + 1]]
        }
        return total
    }

    private fun buildRoute(): List<Int> {
        val visited = BooleanArray(n) { false }
        val route = mutableListOf<Int>()
        var current = (0 until n).random()
        visited[current] = true
        route.add(current)

        repeat(n - 1) {
            val candidates = (0 until n).filter { !visited[it] }
            val weights = candidates.map { next ->
                pheromones[current][next].pow(alpha) * (1.0 / distances[current][next]).pow(beta)
            }
            val total = weights.sum()
            var rand = Math.random() * total
            var chosen = candidates.last()
            for (i in candidates.indices) {
                rand -= weights[i]
                if (rand <= 0) {
                    chosen = candidates[i]
                    break
                }
            }
            visited[chosen] = true
            route.add(chosen)
            current = chosen
        }
        return route
    }

    fun run(onIteration: (iteration: Int, bestLength: Double) -> Unit): List<AntPlace> {
        if (n == 0) return emptyList()
        if (n == 1) return places

        var bestRoute = (0 until n).toList()
        var bestLength = routeLength(bestRoute)

        repeat(iterations) { iter ->
            val allRoutes = List(numAnts) { buildRoute() }

            for (i in 0 until n) {
                for (j in 0 until n) {
                    pheromones[i][j] *= (1.0 - evaporation)
                    if (pheromones[i][j] < 0.01) pheromones[i][j] = 0.01
                }
            }

            for (route in allRoutes) {
                val len = routeLength(route)
                val deposit = q / len
                for (k in 0 until route.size - 1) {
                    pheromones[route[k]][route[k + 1]] += deposit
                    pheromones[route[k + 1]][route[k]] += deposit
                }
                if (len < bestLength) {
                    bestLength = len
                    bestRoute = route
                }
            }

            onIteration(iter + 1, bestLength)
        }

        return bestRoute.map { places[it] }
    }
}
