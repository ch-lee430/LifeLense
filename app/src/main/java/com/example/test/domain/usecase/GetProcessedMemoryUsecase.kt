package com.example.test.domain.usecase

import android.util.Log
import com.example.test.domain.model.Memory
import com.example.test.domain.model.ProcessedCalendar
import com.example.test.domain.model.ProcessedMessage
import com.example.test.domain.repository.GeminiRepository
import com.example.test.domain.repository.ProcessedDataRepository
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class GetProcessedMemoryUseCase @Inject constructor(
    private val processedDataRepository: ProcessedDataRepository,
    private val geminiRepository: GeminiRepository
){
    suspend operator fun invoke(question: String): Memory{
        Log.d("entry","여기까진 진입완료")
        val messages = processedDataRepository.getProcessedMessage()
        val calendars = processedDataRepository.getProcessedCalendar()
        Log.d("entry","여기까진 진입완료2")
        val contextString = createGeminiContext(calendars, messages)
        // 3. Gemini Repository 호출
        // (Repository는 이제 List가 아니라 완성된 contextString을 받도록 수정되어야 합니다)
        val response = geminiRepository.generateMemoryResponse(question, contextString)

        // 4. 결과 반환 (만약 null이면 에러 메시지 반환)
        return response ?: Memory(
            title = "응답 불가",
            date = System.currentTimeMillis(),
            content = "죄송합니다. 답변을 생성할 수 없습니다. 네트워크를 확인해주세요.",
            referenceData = emptyList()
        )
    }


    // 날짜 포맷터 (전역 혹은 클래스 내부에 선언)
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:시 mm분", Locale.KOREA)

    private fun createGeminiContext(
        calendars: List<ProcessedCalendar>,
        messages: List<ProcessedMessage>
    ): String {
        val sb = StringBuilder()

        // 1. 캘린더 데이터 변환
        sb.appendLine("[사용자의 캘린더 일정]")
        if (calendars.isEmpty()) {
            sb.appendLine("(일정 없음)")
        } else {
            calendars.forEach { item ->
                val dateStr = dateFormat.format(Date(item.date))
                // 예: - [2025년 11월 29일 14시 00분] 미용실 가기
                sb.appendLine("- [$dateStr] ${item.title}")
            }
        }
        sb.appendLine() // 줄바꿈

        // 2. 메시지 데이터 변환
        sb.appendLine("[사용자의 최근 메시지 요약]")
        if (messages.isEmpty()) {
            sb.appendLine("(메시지 없음)")
        } else {
            messages.forEach { item ->
                val dateStr = dateFormat.format(Date(item.date))
                // 예: - [2025년 11월 28일 10시 30분] 배재준: 프로젝트 ppt 개발에 대한 내용
                sb.appendLine("- [$dateStr] 보낸사람: ${item.personName}, 내용: ${item.summary}")
            }
        }

        return sb.toString()
    }
}