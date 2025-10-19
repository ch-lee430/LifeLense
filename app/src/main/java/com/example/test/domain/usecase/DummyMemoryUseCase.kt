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
import javax.inject.Inject
import java.util.concurrent.TimeUnit

// --- 유즈케이스 ---
class DummyMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val geminiRepository: GeminiRepository
) {
    suspend operator fun invoke(question: String): Memory {
        // 1. 질문에서 날짜 추출 (현재는 1년 전으로 고정)
        val queryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
        val startTime = queryDate
        val endTime = queryDate + TimeUnit.DAYS.toMillis(1)

        // =======================================================
        // ★★★ 확인용 더미 데이터 생성 ★★★
        // =======================================================
        val fixedDate = 1696867200000L // 2023년 10월 9일 (예시 UTC 시간)

        val photos = listOf(
            MediaData("content://media/photo/123", fixedDate + 1000, "image/jpeg"),
            MediaData("content://media/photo/124", fixedDate + 2000, "image/jpeg"),
            MediaData("content://media/photo/125", fixedDate + 3000, "image/jpeg")
        )
        val sms = listOf(
            SmsData("점심 뭐 먹지?", "010-9666-1934", "엄마", fixedDate + 10000),
            SmsData("회의 1시 반으로 변경", "010-9876-5432", "직장B", fixedDate + 15000)
        )
        val callLogs = listOf(
            CallLogData("010-9876-5432", "직장B", fixedDate + 20000, 65L),
            CallLogData("010-1111-2222", "알 수 없음", fixedDate + 25000, 120L)
        )
        val calendarEvents = listOf(
            CalendarData("중요 프로젝트 마감", "코드 리뷰", fixedDate + 30000, "사무실")
        )
        // =======================================================


        // 3. 모든 데이터를 통합하고 분석하여 답변 생성
        val combinedData = combineData(photos, sms, callLogs, calendarEvents)
        val combinedText = combinedData.joinToString(" ")
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
            title = "과거의 기억 (${java.text.SimpleDateFormat("yyyy년 MM월 dd일").format(fixedDate)})",
            date = fixedDate,
            content = summary,
            referenceData = referenceItems
        )
    }

    // ★★★ 추가 함수: Dialog에 표시할 이벤트 목록을 제공 (임시 더미) ★★★
    fun getEventsByTimestamp(timestamp: Long): List<CalendarEvent> {
        // 현재는 더미 데이터를 반환하며, 실제 앱에서는 Repository를 통해 가져와야 합니다.
        // 이 함수는 'CalendarDialog'에서 해당 월의 이벤트를 채우는 데 사용됩니다.
        val baseDate = timestamp
        return listOf(
            CalendarEvent(baseDate, "중요 프로젝트 마감"),
            CalendarEvent(baseDate + 3600000, "친구와 저녁 약속"),
            CalendarEvent(baseDate + 7200000, "코드 리뷰 회의"),
            CalendarEvent(baseDate - 86400000, "어제: 병원 예약")
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
        list.add("사진 ${photos.size}장")
        list.addAll(sms.map { "문자: ${it.contactName}: ${it.body}" })
        list.addAll(callLogs.map { "통화: ${it.contactName}와 ${it.duration}초 통화" })
        list.addAll(calendarEvents.map { "일정: ${it.title}" })
        return list
    }

    // 텍스트 요약 로직 (Gemini API 활용)
    private suspend fun generateSummaryFromText(text: String): String {
        return geminiRepository.generateAnswer(text)
    }
}