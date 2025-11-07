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

            val responseText = response.text ?: ""

            val cleanedJson = responseText
                .trim()
                .removePrefix("```json") // 마크다운 코드 블록 시작 제거
                .removePrefix("```")     // 일반 코드 블록 시작 제거
                .removeSuffix("```")     // 코드 블록 끝 제거
                .trim()

            Log.d("GeminiSource", "Cleaned JSON Response: $cleanedJson")
            return cleanedJson

        } catch (e: Exception) {
            Log.e("GeminiSource", "Error generating answer: ${e.message}", e)
            // 에러 발생 시 파싱 가능하도록 에러 메시지를 JSON 형태로 반환
            return "{\"summary\":\"API 통신 실패: ${e.localizedMessage}\", \"reference_indices\":[]}"
        }
    }
}