package com.example.tsumaps.algorithms.genetic

import kotlin.random.Random

class GeneticAlgorithm (
    allPlaces: List<Place>,
    val neededFood: List<String>,
    private val populationSize: Int = 50,
    private val mutationProbability: Double = 0.05,
    private val startX: Int,
    private val startY: Int
) {
    private val suitablePlaces = allPlaces.filter { place ->
        place.menu.intersect(neededFood.toSet()).isNotEmpty()
    }
    private var population = mutableListOf<Individual>()

    init {
        for (i in 0 until populationSize) {
            population.add(Individual(suitablePlaces.shuffled()))
        }
    }

    fun execute(generations: Int, currentTime: Int): Individual
    {
        for (g in 0 until generations)
        {
            population.forEach { it.calculateFitness(startX, startY, currentTime, neededFood)
            }

            population.sortBy { it.score }

            val nextGeneration = mutableListOf<Individual>()

            nextGeneration.add(population[0])
            nextGeneration.add(population[1])

            while (nextGeneration.size < populationSize)
            {
                val parent1 = selectParent()
                val parent2 = selectParent()

                var childRoute = combineParents(parent1, parent2)

                if (Random.nextDouble() < mutationProbability)
                {
                    childRoute = mutate(childRoute)
                }

                nextGeneration.add(Individual(childRoute))
            }
            population = nextGeneration
        }
        return population[0]
    }

    private fun selectParent(): Individual {
        val tournament = List(3) { population[Random.nextInt(population.size)] }
        return tournament.maxByOrNull { it.score }!!
    }

    private fun combineParents(p1: Individual, p2: Individual): List<Place> {
        val size = p1.path.size

        if (size <= 1) return p1.path

        val start = Random.nextInt(size)
        val end = if (start < size - 1) Random.nextInt(start + 1, size) else start

        val childArray = arrayOfNulls<Place>(size)

        for (i in start..end)
        {
            childArray[i] = p1.path[i]
        }

        var currentIndex = 0
        for (i in 0 until size)
        {
            val placeFromP2 = p2.path[i]

            val alreadyInChild = childArray.any { it?.id == placeFromP2.id }

            if (!alreadyInChild)
            {
                while (currentIndex < size && childArray[currentIndex] != null)
                {
                    currentIndex++
                }

                if (currentIndex < size)
                {
                    childArray[currentIndex] = placeFromP2
                }
            }
        }

        return childArray.filterNotNull()
    }

    private fun mutate(route: List<Place>): List<Place> {
        val mutated = route.toMutableList()
        val idx1 = Random.nextInt(mutated.size)
        val idx2 = Random.nextInt(mutated.size)

        val temp = mutated[idx1]
        mutated[idx1] = mutated[idx2]
        mutated[idx2] = temp

        return mutated
    }

}