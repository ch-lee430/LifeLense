package com.example.test.data.source

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.example.test.BuildConfig

class GeminiSource @Inject constructor(@ApplicationContext private val context: Context){

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateGeminiAnswer(question: String): String {
        try {
            val response = generativeModel.generateContent(question)

            Log.d("tag", response.text?: "no answer")
            return response.text ?: "No response text found."

        } catch (e: Exception) {
            Log.e("GeminiSource", "Error generating answer: ${e.message}", e)
            return "An error occurred: ${e.localizedMessage}"
        }
    }
}