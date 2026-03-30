package com.example.tsumaps.algorithms.A_star

class MapGrid(private val gridMap: Array<IntArray>)
{
    private val rows = gridMap.size
    private val cols = gridMap[0].size

    fun isValid(x: Int, y: Int): Boolean
    {
        if (x < 0) return false
        if (x >= cols) return false
        if (y < 0) return false
        if (y >= rows) return false

        if (gridMap[y][x] == 0)
        {
            return true
        }
        else
        {
            return false
        }
    }

    fun getNeighbors(node: Node): List<Node>
    {
        val neighbors = ArrayList<Node>()

        if (isValid(node.x + 1, node.y))
        {
            neighbors.add(Node(node.x + 1, node.y))
        }
        if (isValid(node.x - 1, node.y))
        {
            neighbors.add(Node(node.x - 1, node.y))
        }
        if (isValid(node.x, node.y + 1))
        {
            neighbors.add(Node(node.x, node.y + 1))
        }
        if (isValid(node.x, node.y - 1))
        {
            neighbors.add(Node(node.x, node.y - 1))
        }
        return neighbors
    }
}