package com.example.tsumaps.algorithms.Decision_tree

import kotlin.math.log2

data class DataRecord(
    val features: Map<String, String>,
    val label: String
)

data class Node(
    val feature: String? = null,
    val branches: Map<String, Node>? = null,
    val label: String? = null,
    val isLeaf: Boolean = false
)

fun calculateEntropy(data: List<DataRecord>): Double {
    if (data.isEmpty()) return 0.0
    val total = data.size.toDouble()
    val counts = data.groupBy { it.label }.mapValues { it.value.size }
    return counts.values.sumOf { count ->
        val probability = count / total
        -probability * log2(probability)
    }
}

fun calculateInformationGain(data: List<DataRecord>, feature: String): Double {
    val totalEntropy = calculateEntropy(data)
    val totalSize = data.size.toDouble()
    val groups = data.groupBy { it.features[feature] }

    val weightedEntropy = groups.values.sumOf { group ->
        (group.size / totalSize) * calculateEntropy(group)
    }
    return totalEntropy - weightedEntropy
}

fun buildDecisionTree(data: List<DataRecord>, features: Set<String>): Node {
    val labels = data.map { it.label }.distinct()
    if (labels.size == 1) return Node(label = labels[0], isLeaf = true)
    if (features.isEmpty()) {
        val mostCommonLabel = data.groupBy { it.label }.maxByOrNull { it.value.size }?.key
        return Node(label = mostCommonLabel, isLeaf = true)
    }
    val bestFeature = features.maxByOrNull { calculateInformationGain(data, it) } ?: features.first()
    val branches = data.groupBy { it.features[bestFeature] ?: "unknown" }
        .mapValues { entry ->
            buildDecisionTree(entry.value, features - bestFeature)
        }
    return Node(feature = bestFeature, branches = branches)
}

fun parseCSV(text: String): Pair<List<String>, List<DataRecord>> {
    val lines = text.trim().lines().filter {it.isNotBlank() }
    val headers = lines[0].split(";").map { it.trim() }
    val featureNames = headers.dropLast(1)
    val data = lines.drop(1).map {line ->
        val values = line.split(";").map { it.trim() }
        val featureMap = featureNames.zip(values.dropLast(1)).toMap()
        DataRecord(featureMap, values.last())
    }
    return featureNames to data
}