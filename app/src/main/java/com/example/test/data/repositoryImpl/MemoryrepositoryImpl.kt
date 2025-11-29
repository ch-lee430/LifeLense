package com.example.test.data.repositoryImpl


import com.example.test.data.model.CalendarData
import com.example.test.data.model.CallLogData
import com.example.test.data.model.MediaData
import com.example.test.data.model.SmsData
import com.example.test.data.source.CalendarDataSource
import com.example.test.data.source.CallLogDataSource
import com.example.test.data.source.MediaDataSource
import com.example.test.data.source.SmsDataSource
import com.example.test.domain.repository.MemoryRepository
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val mediaDataSource: MediaDataSource,
    private val smsDataSource: SmsDataSource,
    private val callLogDataSource: CallLogDataSource,
    private val calendarDataSource: CalendarDataSource
) : MemoryRepository {

    override suspend fun getGalleryPhotos(startTime: Long, endTime: Long): List<MediaData> {
        return mediaDataSource.getMedia(startTime, endTime)
    }

    override suspend fun getSmsMessages(startTime: Long, endTime: Long): List<SmsData> {
        return smsDataSource.getSmsMessages(startTime, endTime)
    }

    override suspend fun getCallLogs(startTime: Long, endTime: Long): List<CallLogData> {
        return callLogDataSource.getCallLogs(startTime, endTime)
    }

    override suspend fun getCalendarEvents(startTime: Long, endTime: Long): List<CalendarData> {
        return calendarDataSource.getCalendarEvents(startTime, endTime)
    }
}