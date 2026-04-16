package com.example.tsumaps.algorithms.neural

import android.content.Context
import org.json.JSONObject
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

class NeuralNetwork {

    private var inputSize = 2500
    private var hiddenSize = 128
    private var outputSize = 10

    private var w1: Array<DoubleArray> = emptyArray()
    private var b1: DoubleArray = DoubleArray(0)
    private var w2: Array<DoubleArray> = emptyArray()
    private var b2: DoubleArray = DoubleArray(0)

    private var loaded = false

    fun loadWeights(context: Context) {
        val json = context.assets.open("weights.json").bufferedReader().use { it.readText() }
        val obj = JSONObject(json)

        inputSize = obj.getInt("input_size")
        hiddenSize = obj.getInt("hidden_size")
        outputSize = obj.getInt("output_size")

        val jsonW1 = obj.getJSONArray("w1")
        w1 = Array(hiddenSize) { i ->
            val row = jsonW1.getJSONArray(i)
            DoubleArray(inputSize) { j -> row.getDouble(j) }
        }

        val jsonB1 = obj.getJSONArray("b1")
        b1 = DoubleArray(hiddenSize) { i -> jsonB1.getDouble(i) }

        val jsonW2 = obj.getJSONArray("w2")
        w2 = Array(outputSize) { i ->
            val row = jsonW2.getJSONArray(i)
            DoubleArray(hiddenSize) { j -> row.getDouble(j) }
        }

        val jsonB2 = obj.getJSONArray("b2")
        b2 = DoubleArray(outputSize) { i -> jsonB2.getDouble(i) }

        loaded = true
    }

    fun predict(pixels: List<Float>): Pair<Int, Float> {
        if (!loaded) return Pair(0, 0f)

        val input = DoubleArray(pixels.size) { pixels[it].toDouble() }

        val hidden = DoubleArray(hiddenSize) { i ->
            var sum = b1[i]
            for (j in input.indices) {
                sum += w1[i][j] * input[j]
            }
            if (sum > 0) sum else 0.0
        }

        val output = DoubleArray(outputSize) { i ->
            var sum = b2[i]
            for (j in hidden.indices) {
                sum += w2[i][j] * hidden[j]
            }
            sum
        }

        val maxVal = output.max()
        val exps = DoubleArray(outputSize) { exp(output[it] - maxVal) }
        val total = exps.sum()
        val probs = DoubleArray(outputSize) { exps[it] / total }

        val predicted = probs.indices.maxByOrNull { probs[it] } ?: 0
        return Pair(predicted, probs[predicted].toFloat())
    }
}
