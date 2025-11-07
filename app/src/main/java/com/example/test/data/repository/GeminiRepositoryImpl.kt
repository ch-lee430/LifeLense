package com.example.test.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.test.data.source.GeminiSource
import com.example.test.domain.repository.GeminiRepository
import com.example.test.domain.model.DateRangeAnalysis
import com.google.gson.Gson
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiRepositoryImpl @Inject constructor(
    private val geminiSource: GeminiSource,
    private val gson: Gson
): GeminiRepository{
    private fun formatCurrentTime(timestamp: Long): String {
        return SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREA).format(Date(timestamp))
    }

    override suspend fun generateAnswer(question: String): String {
        return geminiSource.generateGeminiAnswer(question)
    }

    override suspend fun extractDateRange(question: String, currentTimestamp: Long): DateRangeAnalysis {
        val formattedTime = formatCurrentTime(currentTimestamp)
        Log.d(TAG, "현재 시간:\n$formattedTime")
        // JSON 응답 스키마 정의
        val jsonSchema = gson.toJson(
            mapOf(
                "start_timestamp" to "long (Unix time in milliseconds)",
                "end_timestamp" to "long (Unix time in milliseconds)",
                "is_specific" to "boolean (true if specific period was identified)"
            )
        )

        // Gemini에게 보낼 프롬프트 구성 (현재 시점 컨텍스트 제공)
        val prompt = """
            제공된 '현재 시간 정보'와 '사용자 질문'을 분석하여, 사용자가 요청한 정확한 시간 범위(시작 및 종료 타임스탬프)를 결정해야 합니다.

            규칙:
            1. '어제', '지난주', '작년'과 같은 상대적인 시간 표현을 해석할 때는 반드시 '현재 시간 정보'를 사용하십시오.
            2. **가장 중요한 규칙**: 사용자 질문에 **'yyyy년'** 또는 **'yy년'** 등 연도가 명시되어 있다면, **다른 규칙을 무시하고** 해당 **명시된 연도를 타임스탬프 계산에 최우선**으로 적용하십시오.
            3. 연도가 명시되지 않은 경우("10월 8일")에만 현재 시간($formattedTime)을 기준으로 가장 최근에 지난 해당 날짜를 선택하십시오. (미래 날짜는 작년으로 간주).
            4. 날짜 범위가 하루인 경우, 시작은 00:00:00.000, 종료는 23:59:59.999가 되어야 합니다.
            5. 질문이 모호하거나 전체 기간을 의미하는 경우 (예: "뭐 했어?"), start_timestamp는 0L, end_timestamp는 현재 시간($currentTimestamp)으로 설정하십시오.
            6. start_timestamp는 end_timestamp보다 작거나 같아야 합니다. (시작이 종료보다 앞서야 합니다.)
            7. 결과는 반드시 아래의 JSON 스키마를 따르는 단일 JSON 객체로 반환해야 합니다.

            현재 시간 정보: $0 ms ($formattedTime KST)
            사용자 질문: "$question"

            JSON Schema: $jsonSchema
        """.trimIndent()

        // GeminiSource를 통해 날짜 분석 요청
        Log.d(TAG, "Prompt for Date Extraction:\n$prompt")

        // GeminiSource를 통해 날짜 분석 요청
        val jsonResponse = geminiSource.generateGeminiAnswer(prompt)

        // 🌟🌟🌟 응답 로그 추가 🌟🌟🌟
        Log.d(TAG, "Raw JSON Response for Date: $jsonResponse")

        // JSON 파싱 및 반환
        return try {
            val dateRange = gson.fromJson(jsonResponse, DateRangeAnalysis::class.java)

            // 🌟🌟🌟 변환된 날짜/시간 정보를 포함한 최종 로그 추가 🌟🌟🌟
            val formattedStart = formatCurrentTime(dateRange.startTimestamp)
            val formattedEnd = formatCurrentTime(dateRange.endTimestamp)

            Log.d(TAG, "✅ Parsed Date Range (KST):")
            Log.d(TAG, "  START: ${dateRange.startTimestamp} ms -> $formattedStart")
            Log.d(TAG, "  END:   ${dateRange.endTimestamp} ms -> $formattedEnd")
            Log.d(TAG, "  Is Specific: ${dateRange.isSpecific}")

            dateRange
        } catch (e: Exception) {
            // 파싱 실패 시, 안전하게 전체 기간을 반환 (Fallback)
            DateRangeAnalysis(
                startTimestamp = 0L,
                endTimestamp = currentTimestamp,
                isSpecific = false
            )
        }
    }
}