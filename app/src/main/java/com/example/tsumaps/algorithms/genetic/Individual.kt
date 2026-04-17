package com.example.tsumaps.algorithms.genetic

import kotlin.math.hypot
import kotlin.math.roundToInt

class Individual(val path: List<Place>) {
    var score: Double = 0.0
    var totalDistance: Int = 0
    var finalRoute: List<Place> = emptyList()

    private val metersPerUnit = 15.0
    private val walkSpeedMetersPerMinute = 83.3
    private val missingItemPenalty = 8000.0
    private val closedPlacePenalty = 5000.0

    fun calculateFitness(startX: Int, startY: Int, currentTime: Int, neededFood: List<String> = emptyList()) {
        val neededNormalized = neededFood.map { it.lowercase().trim() }
        val stillMissingItems = neededNormalized.toMutableList()
        val collectedItemsNow = mutableListOf<String>()

        var currentX = startX.toDouble()
        var currentY = startY.toDouble()
        var nowMinute = currentTime.toDouble()
        var travelMinutes = 0.0
        var totalMeters = 0.0
        var penalties = 0.0

        val visitedPath = mutableListOf<Place>()

        for (place in path) {
            if (stillMissingItems.isEmpty()) break

            val unitDistance = hypot(place.x - currentX, place.y - currentY)
            val stepMeters = unitDistance * metersPerUnit
            val stepMinutes = stepMeters / walkSpeedMetersPerMinute

            totalMeters += stepMeters
            travelMinutes += stepMinutes
            nowMinute += stepMinutes

            val minuteOfDay = (nowMinute.roundToInt() % 1440)
            val isOpen = if (place.openTime < place.closeTime) {
                minuteOfDay in place.openTime until place.closeTime
            } else {
                minuteOfDay >= place.openTime || minuteOfDay < place.closeTime
            }

            if (isOpen) {
                var boughtHere = false
                val menuNormalized = place.menu.map { it.lowercase().trim() }

                val iterator = stillMissingItems.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (menuNormalized .contains(item)) {
                        collectedItemsNow.add(item)
                        iterator.remove()
                        boughtHere = true
                    }
                }

                if (boughtHere) {
                    visitedPath.add(place)
                }
            } else {
                val hasNeededFood = place.menu.any { m -> neededNormalized.contains(m.lowercase().trim()) }
                if (hasNeededFood) {
                    penalties += closedPlacePenalty
                }
            }

            currentX = place.x.toDouble()
            currentY = place.y.toDouble()
        }
        val returnDist = hypot(currentX - startX, currentY - startY) * metersPerUnit
        totalMeters += returnDist
        travelMinutes += (returnDist / walkSpeedMetersPerMinute)

        val missingPenalty = stillMissingItems.size * missingItemPenalty

        this.totalDistance = totalMeters.toInt()
        this.finalRoute = visitedPath

        this.score = travelMinutes + penalties + missingPenalty
    }
}