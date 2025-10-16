package com.example.test.domain.usecase

import com.example.test.data.model.CallLogData
import com.example.test.data.model.CalendarData
import com.example.test.data.model.MediaData
import com.example.test.data.model.SmsData
import com.example.test.domain.model.Memory
import com.example.test.domain.model.ReferenceItem
import com.example.test.domain.model.ReferenceType
import com.example.test.domain.repository.MemoryRepository
import javax.inject.Inject
import java.util.concurrent.TimeUnit

class GetPastMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(question: String): Memory {
        // 1. 질문에서 날짜/시간 정보를 추출하는 로직 (Gemini API 또는 NLP 라이브러리 활용)
        // 현재는 예시를 위해 고정된 날짜를 사용
        val queryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365) // 1년 전
        val startTime = queryDate
        val endTime = queryDate + TimeUnit.DAYS.toMillis(1)

        // 2. Repository를 통해 Data Layer로부터 모든 관련 데이터를 가져옴
        val photos = memoryRepository.getGalleryPhotos(startTime, endTime)
        val sms = memoryRepository.getSmsMessages(startTime, endTime)
        val callLogs = memoryRepository.getCallLogs(startTime, endTime)
        val calendarEvents = memoryRepository.getCalendarEvents(startTime, endTime)

        // 3. 모든 데이터를 통합하고 분석하여 답변 생성
        val combinedData = combineData(photos, sms, callLogs, calendarEvents)
        val combinedText = combinedData.joinToString("\n")
        val summary = generateSummaryFromText(combinedText)

        // 4. 참조 데이터 리스트를 생성
        val referenceItems = mutableListOf<ReferenceItem>()
        photos.forEach { referenceItems.add(ReferenceItem(ReferenceType.PHOTO, it.uri)) }
        sms.forEach { referenceItems.add(ReferenceItem(ReferenceType.SMS, "${it.sender}: ${it.body}")) }
        callLogs.forEach { referenceItems.add(ReferenceItem(ReferenceType.CALL, "${it.phoneNumber} (${it.type})")) }
        calendarEvents.forEach { referenceItems.add(ReferenceItem(ReferenceType.CALENDAR, it.title)) }

        return Memory(
            title = "과거의 기억 (${java.text.SimpleDateFormat("yyyy년 MM월 dd일").format(queryDate)})",
            date = queryDate,
            content = summary,
            referenceData = referenceItems
        )
    }

    // 가져온 데이터를 하나의 텍스트로 합치는 로직 (Gemini API 활용)
    private fun combineData(
        photos: List<MediaData>,
        sms: List<SmsData>,
        callLogs: List<CallLogData>,
        calendarEvents: List<CalendarData>
    ): List<String> {
        val list = mutableListOf<String>()
        list.add("사진: ${photos.size}장")
        list.addAll(sms.map { "문자: ${it.body}" })
        list.addAll(callLogs.map { "통화: ${it.phoneNumber}" })
        list.addAll(calendarEvents.map { "일정: ${it.title}" })
        return list
    }

    // 텍스트 요약 로직 (Gemini API 활용)
    private fun generateSummaryFromText(text: String): String {
        // Gemini API를 사용하여 텍스트를 요약하는 로직을 구현해야 합니다.
        // 현재는 예시 텍스트를 반환합니다.
        return "당신의 1년 전 기억을 찾았습니다. ${text.lines().joinToString(" ") { it }}"
    }
}