package com.example.tsumaps.algorithms.A_star

class MapGrid(private val gridMap: Array<IntArray>) {
    private val rows = gridMap.size
    private val cols = gridMap[0].size

    fun isValid(x: Int, y: Int): Boolean {
        if (x !in 0 until cols || y !in 0 until rows) return false

        return gridMap[y][x] < 999999
    }

    fun getWeight(x: Int, y: Int): Int {
        return gridMap[y][x]
    }

    fun getNeighbors(node: Node): List<Node> {
        val neighbors = mutableListOf<Node>()
        val directions = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)

        for (dir in directions) {
            val newX = node.x + dir.first
            val newY = node.y + dir.second
            if (isValid(newX, newY)) {
                neighbors.add(Node(newX, newY))
            }
        }
        return neighbors
    }
}