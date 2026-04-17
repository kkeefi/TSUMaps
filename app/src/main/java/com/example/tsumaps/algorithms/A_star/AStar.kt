package com.example.tsumaps.algorithms.A_star

import java.util.PriorityQueue
import kotlin.math.abs

class AStar(private val gridMap: Array<IntArray>) {

    private val rows = gridMap.size
    private val cols = gridMap[0].size

    private fun isValid(x: Int, y: Int): Boolean {
        return x in 0 until cols && y in 0 until rows && gridMap[y][x] < 999999
    }

    private fun getWeight(x: Int, y: Int): Int = gridMap[y][x]

    private fun getNeighbors(node: Node): List<Node> {
        val neighbors = mutableListOf<Node>()
        val directions = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        for (dir in directions) {
            val nx = node.x + dir.first
            val ny = node.y + dir.second
            if (isValid(nx, ny)) neighbors.add(Node(nx, ny))
        }
        return neighbors
    }

    private fun heuristic(a: Node, b: Node): Int = abs(a.x - b.x) + abs(a.y - b.y)

    fun findPath(startX: Int, startY: Int, endX: Int, endY: Int): List<IntArray>? {
        val queue = PriorityQueue<Node>(compareBy { it.totalCost })
        val visited = Array(rows) { BooleanArray(cols) }

        val start = Node(startX, startY).apply {
            costFromStart = 0
            heuristicValue = heuristic(this, Node(endX, endY))
        }
        queue.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            if (current.x == endX && current.y == endY) return buildPath(current)
            if (visited[current.y][current.x]) continue
            visited[current.y][current.x] = true

            for (next in getNeighbors(current)) {
                if (visited[next.y][next.x]) continue
                val newG = current.costFromStart + getWeight(next.x, next.y)
                if (newG < next.costFromStart) {
                    next.costFromStart = newG
                    next.heuristicValue = heuristic(next, Node(endX, endY))
                    next.parent = current
                    queue.add(next)
                }
            }
        }
        return null
    }

    fun findPathAnimated(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        onStep: (visited: Set<Pair<Int,Int>>, frontier: Set<Pair<Int,Int>>, current: Pair<Int,Int>?) -> Unit
    ): List<IntArray>? {
        val queue = PriorityQueue<Node>(compareBy { it.totalCost })
        val visited = Array(rows) { BooleanArray(cols) }
        val visitedSet = mutableSetOf<Pair<Int,Int>>()
        val frontierSet = mutableSetOf<Pair<Int,Int>>()

        val start = Node(startX, startY).apply {
            costFromStart = 0
            heuristicValue = heuristic(this, Node(endX, endY))
        }
        queue.add(start)
        frontierSet.add(startX to startY)

        var stepCount = 0

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            if (visited[current.y][current.x]) continue
            visited[current.y][current.x] = true
            visitedSet.add(current.x to current.y)
            frontierSet.remove(current.x to current.y)

            stepCount++
            if (stepCount % 30 == 0) {
                onStep(visitedSet.toSet(), frontierSet.toSet(), current.x to current.y)
            }

            if (current.x == endX && current.y == endY) {
                onStep(visitedSet.toSet(), frontierSet.toSet(), null)
                return buildPath(current)
            }

            for (next in getNeighbors(current)) {
                if (visited[next.y][next.x]) continue
                val newG = current.costFromStart + getWeight(next.x, next.y)
                if (newG < next.costFromStart) {
                    next.costFromStart = newG
                    next.heuristicValue = heuristic(next, Node(endX, endY))
                    next.parent = current
                    queue.add(next)
                    frontierSet.add(next.x to next.y)
                }
            }
        }
        return null
    }

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
