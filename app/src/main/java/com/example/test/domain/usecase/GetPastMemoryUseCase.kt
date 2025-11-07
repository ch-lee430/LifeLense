package com.example.test.domain.usecase

import com.example.test.data.model.CallLogData
import com.example.test.data.model.CalendarData
import com.example.test.data.model.MediaData
import com.example.test.data.model.SmsData
import com.example.test.domain.model.CalendarEvent
import com.example.test.domain.model.Memory
import com.example.test.domain.model.ReferenceItem
import com.example.test.domain.model.ReferenceType
import com.example.test.domain.repository.GeminiRepository
import com.example.test.domain.repository.MemoryRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

// Simple date formatting helper
private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))
}

// Gemini가 반환할 JSON 응답 스키마 정의 (메인 분석)
data class GeminiAnalysisResult(
    @SerializedName("summary") val summary: String,
    @SerializedName("reference_indices") val referenceIndices: List<String> // 근거 기록의 원본 목록 인덱스
)

class GetPastMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val geminiRepository: GeminiRepository
) {
//사용자 질문을 바탕으로 실제 데이터를 가져와 Gemini에게 분석을 요청하고 결과를 반환합니다.
    suspend operator fun invoke(question: String): Memory {
        // ★★★ 1. Gemini를 사용해 질문에서 날짜 범위 추출 ★★★
        val currentTime = System.currentTimeMillis()
        val dateRange = geminiRepository.extractDateRange(question, currentTime)


        val startTime = dateRange.startTimestamp
        val endTime = dateRange.endTimestamp
        // Memory 객체 생성을 위해 endTime을 대표 날짜로 사용
        val queryDate = endTime


        // 2. Repository를 통해 Data Layer로부터 실제 데이터를 가져옴 (이제 특정 기간만 쿼리)
        val photos = memoryRepository.getGalleryPhotos(startTime, endTime)
        val sms = memoryRepository.getSmsMessages(startTime, endTime)
        val callLogs = memoryRepository.getCallLogs(startTime, endTime)
        val calendarEvents = memoryRepository.getCalendarEvents(startTime, endTime)

        // ★★★ 2.1. 데이터가 비어있을 경우 예외 처리 ★★★
        if (photos.isEmpty() && sms.isEmpty() && callLogs.isEmpty() && calendarEvents.isEmpty()) {
            throw Exception("해당 기간(${formatDate(startTime)} ~ ${formatDate(endTime)}) 동안의 기록이 없습니다.")
        }

        // 3. 모든 원본 데이터를 ReferenceItem으로 통합하여 인덱스 참조 목록 생성
        val combinedDataText = combineDataForGemini(photos, sms, callLogs, calendarEvents)


        // 4. Gemini 요청 프롬프트 구성 (JSON 형식 요청 포함)
        val jsonSchema = Gson().toJson(
            mapOf("summary" to "string", "reference_indices" to "array of strings (e.g., [\"PHOTO_0\", \"SMS_1\" \"CALL_0\", \"CALENDAR_3\"])")
        )
        // 프롬프트에 데이터 기간 명시 추가
        val finalPrompt = """
            사용자 질문: "$question"
            데이터 기간: ${formatDate(startTime)} 부터 ${formatDate(endTime)} 까지

            아래 제공된 사용자 기록 목록을 분석하여 답변하십시오. 
            1. 위 데이터 기간 동안의 사용자 활동을 요약하십시오.
            2. 요약의 핵심 근거가 된 기록들을 식별하고, 해당 기록의 인덱스(타입과 0-기반 인덱스, 예: PHOTO_0, SMS_1)를 출력하십시오.
            3. 응답은 아래 JSON 스키마를 정확히 따르는 단일 JSON 객체로 반환하십시오.

            기록 항목 목록:
            ${combinedDataText.joinToString("\n") { it }}

            JSON Schema: $jsonSchema
        """.trimIndent()


        // 5. Gemini Repository 호출 및 JSON 응답 받기
        val jsonResponse = geminiRepository.generateAnswer(finalPrompt)

        // 6. JSON 파싱
        val analysisResult = try {
            Gson().fromJson(jsonResponse, GeminiAnalysisResult::class.java)
        } catch (e: Exception) {
            // 파싱 실패 시, Gemini 응답 전체를 요약으로 사용하고 참조는 비움
            GeminiAnalysisResult(summary = "분석 오류: 응답 파싱 실패. 원본 응답: $jsonResponse", referenceIndices = emptyList())
        }

        // 7. 참조 데이터 리스트 구성: Gemini가 지정한 인덱스만 필터링
        val referenceItems = mutableListOf<ReferenceItem>()

        // 원본 데이터 목록 Map을 생성하여 O(1) 조회 준비
        val rawDataMap = mapOf(
            ReferenceType.PHOTO to photos,
            ReferenceType.SMS to sms,
            ReferenceType.CALL to callLogs,
            ReferenceType.CALENDAR to calendarEvents
        )

        analysisResult.referenceIndices.distinct().forEach { typedIndex ->
            // 예: typedIndex = "SMS_1"
            val parts = typedIndex.split('_')
            if (parts.size == 2) {
                try {
                    val typeName = parts[0]
                    val index = parts[1].toInt()
                    val type = ReferenceType.valueOf(typeName) // String을 Enum으로 변환

                    val rawList = rawDataMap[type] ?: emptyList()
                    if (index >= 0 && index < rawList.size) {
                        val rawItem = rawList[index]

                        // 원본 데이터 모델을 ReferenceItem으로 변환하여 추가
                        val refItem = when (rawItem) {
                            is MediaData -> ReferenceItem(type, rawItem.uri, rawItem.uri, rawItem.date)
                            is SmsData -> {
                                val display = if (rawItem.contactName == "알 수 없음") rawItem.phoneNumber else rawItem.contactName
                                ReferenceItem(type, display, rawItem.phoneNumber, rawItem.date)
                            }
                            is CallLogData -> {
                                val display = if (rawItem.contactName == "알 수 없음") rawItem.phoneNumber else rawItem.contactName
                                ReferenceItem(type, display, rawItem.phoneNumber, rawItem.date)
                            }
                            is CalendarData -> {
                                val title = rawItem.title ?: "제목 없음"
                                ReferenceItem(type, title, title, rawItem.date)
                            }
                            else -> null
                        }

                        if (refItem != null) {
                            referenceItems.add(refItem)
                        }
                    }
                } catch (e: Exception) {
                    // 잘못된 형식의 인덱스 무시
                    // Log.e("MappingError", "Invalid typed index: $typedIndex", e)
                }
            }
        }

        // 8. 최종 Memory 객체 반환
        return Memory(
            title = "과거의 기억 (${formatDate(queryDate)})",
            date = queryDate,
            content = analysisResult.summary, // Gemini의 요약 답변 사용
            referenceData = referenceItems
        )
    }

    /**
     * 캘린더 Dialog에 표시할 해당 월의 이벤트를 제공합니다.
     */
    suspend fun getCalendarEventsForDialog(timestamp: Long): List<CalendarEvent> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 해당 월의 시작일 계산
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        // 해당 월의 종료일 계산
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        // Repository를 통해 해당 월의 모든 이벤트 가져오기 (Room DB 쿼리)
        val rawEvents = memoryRepository.getCalendarEvents(startOfMonth, endOfMonth)

        // CalendarData를 CalendarEvent로 변환하여 반환
        return rawEvents.map {
            CalendarEvent(it.date, it.title ?: "제목 없음")
        }
    }

    /**
     * Gemini 프롬프트 생성을 위해 텍스트로 합치는 함수 (분석 요청용).
     */
    private fun combineDataForGemini(
        photos: List<MediaData>,
        sms: List<SmsData>,
        callLogs: List<CallLogData>,
        calendarEvents: List<CalendarData>
    ): List<String> {
        val list = mutableListOf<String>()
        photos.withIndex().forEach { (i, item) ->
            list.add("PHOTO_${i}: Photo (URI: ${item.uri}) on ${formatDate(item.date)}")
        }
        sms.withIndex().forEach { (i, item) ->
            list.add("SMS_${i}: SMS with ${item.contactName} (${item.phoneNumber}): ${item.body}")
        }
        callLogs.withIndex().forEach { (i, item) ->
            list.add("CALL_${i}: Call with ${item.contactName} (${item.phoneNumber}) for ${item.duration} seconds")
        }
        calendarEvents.withIndex().forEach { (i, item) ->
            list.add("CALENDAR_${i}: Calendar Event: ${item.title} at ${item.location ?: "Unknown"}")
        }
        return list
    }
}