package com.example.test.domain.usecase

import com.example.test.data.model.SmsData
import com.example.test.domain.repository.MemoryRepository
import com.example.test.domain.repository.ProcessedDataRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class processRawDataUsecase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val processedDataRepository: ProcessedDataRepository,
    private val geminiRepository: MemoryRepository
){
    suspend operator fun invoke(){
        val currentTime = System.currentTimeMillis()
        val latestMessageDate = processedDataRepository.getLatestMessageDate()

        when {
            // 1번: 데이터가 아예 없는 경우
            latestMessageDate == null -> {
                val endTime = System.currentTimeMillis()
                val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000 //5일치만
                val startTime = endTime - fiveDaysInMillis

                val messages = memoryRepository.getSmsMessages(startTime, endTime)

            }
            isSameDay(currentTime, latestMessageDate) -> {
                val startTime = latestMessageDate + 1
                val endTime = currentTime
                val messages = memoryRepository.getSmsMessages(startTime, endTime)
            }
            // 2번: null은 아니지만, 날짜가 '다른' 경우 (즉, 과거의 데이터)
            else -> {
                val startTime = (latestMessageDate ?: 0L) + 1
                val endTime = currentTime
                val messages = memoryRepository.getSmsMessages(startTime, endTime)
            }
        }

    }
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val zoneId = ZoneId.systemDefault() // 사용자 기기의 시간대

        val date1 = Instant.ofEpochMilli(timestamp1).atZone(zoneId).toLocalDate()
        val date2 = Instant.ofEpochMilli(timestamp2).atZone(zoneId).toLocalDate()

        return date1 == date2 // 연, 월, 일이 모두 같으면 true
    }
    private fun groupSmsData(smsList: List<SmsData>): Map<Pair<java.time.LocalDate, String>, List<SmsData>> {

        return smsList.groupBy { sms ->
            // 1. Long -> LocalDate 변환 (시간 정보 제거, 날짜만 남김)
            val localDate = Instant.ofEpochMilli(sms.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // 2. (날짜, 이름)을 묶어서 그룹핑의 기준(Key)으로 설정
            Pair(localDate, sms.contactName)
        }
    }
}

