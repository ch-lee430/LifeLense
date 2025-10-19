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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Simple date formatting helper for UI reference text
private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))
}

class GetPastMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(question: String): Memory {
        // ... (날짜 설정 및 Repository 호출 로직 생략)
        val queryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365) // 1년 전
        val startTime = queryDate
        val endTime = queryDate + TimeUnit.DAYS.toMillis(1)

        val photos = memoryRepository.getGalleryPhotos(startTime, endTime)
        val sms = memoryRepository.getSmsMessages(startTime, endTime)
        val callLogs = memoryRepository.getCallLogs(startTime, endTime)
        val calendarEvents = memoryRepository.getCalendarEvents(startTime, endTime)
        // ... (분석 및 요약 로직 생략)

        val combinedData = combineData(photos, sms, callLogs, calendarEvents)
        val combinedText = combinedData.joinToString("\n")
        val summary = generateSummaryFromText(combinedText)

        // 4. 참조 데이터 리스트를 생성 (displayValue와 functionalValue 분리)
        val referenceItems = mutableListOf<ReferenceItem>()
        photos.forEach {
            // PHOTO: display/functional 모두 URI 사용
            referenceItems.add(ReferenceItem(ReferenceType.PHOTO, it.uri, it.uri, it.date))
        }

        sms.forEach {
            // SMS: display는 이름(없으면 번호), functional은 전화번호 사용
            val display = if (it.contactName == "알 수 없음") it.phoneNumber else it.contactName
            referenceItems.add(ReferenceItem(ReferenceType.SMS, display, it.phoneNumber, it.date))
        }

        callLogs.forEach {
            // CALL: display는 이름(없으면 번호), functional은 전화번호 사용
            val display = if (it.contactName == "알 수 없음") it.phoneNumber else it.contactName
            referenceItems.add(ReferenceItem(ReferenceType.CALL, display, it.phoneNumber, it.date))
        }

        calendarEvents.forEach {
            // CALENDAR: display/functional 모두 제목 사용
            val title = it.title ?: "제목 없음"
            referenceItems.add(ReferenceItem(ReferenceType.CALENDAR, title, title, it.date))
        }

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
        list.addAll(sms.map { "문자: ${it.contactName}: ${it.body}" })
        list.addAll(callLogs.map { "통화: ${it.contactName}와 ${it.duration}초 통화" })
        list.addAll(calendarEvents.map { "일정: ${it.title}" })
        return list
    }

    // 텍스트 요약 로직 (Gemini API 활용)
    private fun generateSummaryFromText(text: String): String {
        return "당신의 1년 전 기억을 찾았습니다. ${text.lines().joinToString(" ") { it }}"
    }
}