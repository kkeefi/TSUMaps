package com.example.tsumaps.algorithms.genetic

import kotlin.math.hypot

class Individual (val path: List<Place>)
{
    var score: Double = 0.0
    var totalDistance: Int = 0
    var finalRoute: List<Place> = emptyList()
    private val METERS_PER_UNIT = 5.0

    fun calculateFitness(startX: Int, startY: Int, currentTime: Int, neededFood: List<String> = emptyList()) {
        var distance = 0.0
        var currentX = startX.toDouble()
        var currentY = startY.toDouble()
        var currentTimeMinutes = currentTime.toDouble()

        val collectedFood = mutableSetOf<String>()
        var penalty = 1.0
        var timeBonus = 0.0
        val visitedPath = mutableListOf<Place>()

        for (place in path)
        {
            if (collectedFood.size == neededFood.size && neededFood.isNotEmpty()) break

            val unitDistance = hypot(place.x - currentX, place.y - currentY)
            val segmentMeters = unitDistance * METERS_PER_UNIT
            distance += segmentMeters
            currentTimeMinutes += segmentMeters / 83.3

            if (currentTimeMinutes >= place.openTime && currentTimeMinutes <= place.closeTime)
            {
                var foundNewFood = false
                place.menu.forEach { item ->
                    if (neededFood.contains(item) && !collectedFood.contains(item))
                    {
                        collectedFood.add(item)
                        foundNewFood = true
                    }
                }
                if (foundNewFood)
                {
                    visitedPath.add(place)
                    val minutesToClose = place.closeTime - currentTimeMinutes
                    if (minutesToClose < 30) timeBonus += 500.0
                }
            }
            else
            {
                penalty *= 0.1
            }

            currentX = place.x.toDouble()
            currentY = place.y.toDouble()
        }
        this.totalDistance = distance.toInt()
        this.finalRoute = visitedPath

        val foodScore = collectedFood.size * 2000.0
        val distanceScore = 5000.0 / (distance + 1.0)
        this.score = (foodScore + distanceScore + timeBonus) * penalty
    }
}