package com.example.tsumaps.algorithms.neural

import android.content.Context
import kotlin.math.exp

class NeuralNetwork {
    private var loaded = false
    private lateinit var w1: Array<DoubleArray>
    private lateinit var b1: DoubleArray
    private lateinit var w2: Array<DoubleArray>
    private lateinit var b2: DoubleArray
    private lateinit var w3: Array<DoubleArray>
    private lateinit var b3: DoubleArray

    fun loadWeights(context: Context) {
        try {
            val text = context.assets.open("weights.json").bufferedReader().use { it.readText() }
            val numbers = text.split(" ").filter { it.isNotEmpty() }.map { it.toDouble() }
            var cursor = 0

            w1 = Array(128) { DoubleArray(2500) }
            for (i in 0 until 128) for (j in 0 until 2500) w1[i][j] = numbers[cursor++]
            b1 = DoubleArray(128) { numbers[cursor++] }

            w2 = Array(64) { DoubleArray(128) }
            for (i in 0 until 64) for (j in 0 until 128) w2[i][j] = numbers[cursor++]
            b2 = DoubleArray(64) { numbers[cursor++] }

            w3 = Array(10) { DoubleArray(64) }
            for (i in 0 until 10) for (j in 0 until 64) w3[i][j] = numbers[cursor++]
            b3 = DoubleArray(10) { numbers[cursor++] }

            loaded = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun predict(input: DoubleArray): Pair<Int, Double> {
        if (!loaded) return Pair(-1, 0.0)

        val h1 = DoubleArray(128) { i ->
            var s = b1[i]
            for (j in 0 until 2500) s += w1[i][j] * input[j]
            if (s > 0) s else 0.0
        }

        val h2 = DoubleArray(64) { i ->
            var s = b2[i]
            for (j in 0 until 128) s += w2[i][j] * h1[j]
            if (s > 0) s else 0.0
        }

        val out = DoubleArray(10) { i ->
            var s = b3[i]
            for (j in 0 until 64) s += w3[i][j] * h2[j]
            s
        }

        val maxVal = out.maxOrNull() ?: 0.0
        val exps = out.map { exp(it - maxVal) }
        val sumExps = exps.sum()
        val probs = exps.map { it / sumExps }

        val bestDigit = probs.indices.maxByOrNull { probs[it] } ?: 0
        return Pair(bestDigit, probs[bestDigit])
    }
}