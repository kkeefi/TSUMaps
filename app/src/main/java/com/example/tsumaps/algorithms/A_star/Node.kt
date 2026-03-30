    package com.example.tsumaps.algorithms.A_star

    class Node(val x: Int, val y: Int)
    {
        var costFromStart: Int = Int.MAX_VALUE
        var heuristicValue: Int = 0
        var parent: Node? = null
        val totalCost: Int
            get() = costFromStart + heuristicValue
    }