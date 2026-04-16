package com.example.tsumaps.algorithms.genetic

import kotlin.math.hypot

class Individual (val path: List<Place>)
{
    var score: Double = 0.0
    var totalDistance: Int = 0
    private val METERS_PER_UNIT = 5.0

    fun calculateFitness(startX: Int, startY: Int, currentTime: Int = 720, neededFood: List<String> = emptyList()) {
        var distance = 0.0
        var currentX = startX.toDouble()
        var currentY = startY.toDouble()
        var currentTimeMinutes = currentTime.toDouble()

        val collectedFood = mutableSetOf<String>()
        var penalty = 1.0

        for (place in path)
        {
            if (collectedFood.size == neededFood.size) break

            val unitDistance = hypot(place.x - currentX, place.y - currentY)
            val segmentMeters = unitDistance * METERS_PER_UNIT
            distance += segmentMeters
            currentTimeMinutes += segmentMeters / 83.3

            if (currentTimeMinutes >= place.openTime && currentTimeMinutes <= place.closeTime)
            {
                place.menu.forEach { item ->
                    if (neededFood.contains(item))
                    {
                        collectedFood.add(item)
                    }
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

        val foodScore = collectedFood.size * 1000.0
        val distanceScore = 1.0 / (distance + 1.0)

        this.score = (foodScore + distanceScore) * penalty
    }
}