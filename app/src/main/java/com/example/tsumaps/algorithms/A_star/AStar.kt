package com.example.tsumaps.algorithms.A_star

import java.util.PriorityQueue
import kotlin.math.abs

class AStar(private val gridMap: Array<IntArray>)
{
    private val map = MapGrid(gridMap)

    private fun heuristic(a: Node, b: Node): Int
    {
        return abs(a.x - b.x) + abs(a.y - b.y)
    }

    fun findPath(startX: Int, startY: Int, endX: Int, endY: Int): List<IntArray>?
    {
        val searchQueue = PriorityQueue<Node>(compareBy { it.totalCost })

        val rows = gridMap.size
        val cols = gridMap[0].size
        val visited = Array(rows) { BooleanArray(cols) { false } }
        val startNode = Node(startX, startY)
        val endNode = Node(endX, endY)

        startNode.costFromStart = 0
        startNode.heuristicValue = heuristic(startNode, endNode)

        searchQueue.add(startNode)

        while (searchQueue.isNotEmpty())
        {
            val currentNode = searchQueue.poll()

            if (currentNode.x == endNode.x && currentNode.y == endNode.y)
            {
                return buildPath(currentNode)
            }

            visited[currentNode.y][currentNode.x] = true
            val neighbors = map.getNeighbors(currentNode)

            for (nextNode in neighbors)
            {
                if (visited[nextNode.y][nextNode.x])
                    continue

                val newG = currentNode.costFromStart + 1

                if (newG < nextNode.costFromStart)
                {
                    nextNode.costFromStart = newG
                    nextNode.heuristicValue = heuristic(nextNode, endNode)
                    nextNode.parent = currentNode
                    searchQueue.add(nextNode)
                }
            }
        }
        return null
    }

    private fun buildPath(node: Node): List<IntArray>
    {
        val path = ArrayList<IntArray>()
        var currentNode: Node? = node

        while (currentNode != null)
        {
            path.add(intArrayOf(currentNode.x, currentNode.y))
            currentNode = currentNode.parent
        }

        val result = ArrayList<IntArray>()
        for (i in path.size - 1 downTo 0)
        {
            result.add(path[i])
        }
        return result
    }
}