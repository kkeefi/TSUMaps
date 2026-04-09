package com.example.tsumaps

import android.content.Context
import org.json.JSONObject

object MapDataLoader {
    fun loadMatrix(context: Context, fileName: String): Array<IntArray> {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("matrix")

        val rows = jsonArray.length()
        val cols = jsonArray.getJSONArray(0).length()

        val matrix = Array(rows) { IntArray(cols) }

        for (i in 0 until rows) {
            val rowArray = jsonArray.getJSONArray(i)
            for (j in 0 until cols) {
                matrix[i][j] = rowArray.getInt(j)
            }
        }
        return matrix
    }
}