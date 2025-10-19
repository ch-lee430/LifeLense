package com.example.test.domain.repository

import com.example.test.data.model.CallLogData
import com.example.test.data.model.CalendarData
import com.example.test.data.model.MediaData
import com.example.test.data.model.SmsData

// Domain Layer가 의존할 인터페이스
interface MemoryRepository {
    suspend fun getGalleryPhotos(startTime: Long, endTime: Long): List<MediaData>
    suspend fun getSmsMessages(startTime: Long, endTime: Long): List<SmsData>
    suspend fun getCallLogs(startTime: Long, endTime: Long): List<CallLogData>
    suspend fun getCalendarEvents(startTime: Long, endTime: Long): List<CalendarData>
}