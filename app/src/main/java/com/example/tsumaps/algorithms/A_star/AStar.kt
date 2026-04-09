package com.example.tsumaps.algorithms.A_star

import java.util.PriorityQueue
import kotlin.math.abs

class AStar(private val gridMap: Array<IntArray>) {
    private val map = object {
        val rows = gridMap.size
        val cols = gridMap[0].size

        fun isValid(x: Int, y: Int): Boolean {
            return x in 0 until cols && y in 0 until rows && gridMap[y][x] < 999999
        }

        fun getWeight(x: Int, y: Int): Int = gridMap[y][x]

        fun getNeighbors(node: Node): List<Node> {
            val neighbors = mutableListOf<Node>()
            val directions = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
            for (dir in directions) {
                val newX = node.x + dir.first
                val newY = node.y + dir.second
                if (isValid(newX, newY)) neighbors.add(Node(newX, newY))
            }
            return neighbors
        }
    }

    private fun heuristic(a: Node, b: Node): Int = abs(a.x - b.x) + abs(a.y - b.y)

    fun findPath(startX: Int, startY: Int, endX: Int, endY: Int): List<IntArray>? {
        val searchQueue = PriorityQueue<Node>(compareBy { it.totalCost })
        val visited = Array(map.rows) { BooleanArray(map.cols) { false } }

        val startNode = Node(startX, startY).apply {
            costFromStart = 0
            heuristicValue = heuristic(this, Node(endX, endY))
        }

        searchQueue.add(startNode)

        while (searchQueue.isNotEmpty()) {
            val currentNode = searchQueue.poll() ?: break

            if (currentNode.x == endNodeX(endX) && currentNode.y == endNodeY(endY)) {
                return buildPath(currentNode)
            }

            if (visited[currentNode.y][currentNode.x]) continue
            visited[currentNode.y][currentNode.x] = true

            for (nextNode in map.getNeighbors(currentNode)) {
                if (visited[nextNode.y][nextNode.x]) continue

                val newG = currentNode.costFromStart + map.getWeight(nextNode.x, nextNode.y)

                if (newG < nextNode.costFromStart) {
                    nextNode.costFromStart = newG
                    nextNode.heuristicValue = heuristic(nextNode, Node(endX, endY))
                    nextNode.parent = currentNode
                    searchQueue.add(nextNode)
                }
            }
        }
        return null
    }

    private fun endNodeX(x: Int) = x
    private fun endNodeY(y: Int) = y

    private fun buildPath(node: Node): List<IntArray> {
        val path = mutableListOf<IntArray>()
        var curr: Node? = node
        while (curr != null) {
            path.add(intArrayOf(curr.x, curr.y))
            curr = curr.parent
        }
        return path.asReversed()
    }
}