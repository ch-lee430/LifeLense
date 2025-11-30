package com.example.test.data.repositoryImpl

import com.example.test.data.dao.CalendarDao
import com.example.test.data.dao.CallDao
import com.example.test.data.dao.ImageDao
import com.example.test.data.dao.MessageDao
import com.example.test.domain.model.ProcessedCalendar
import com.example.test.domain.model.ProcessedCall
import com.example.test.domain.model.ProcessedImage
import com.example.test.domain.model.ProcessedMessage
import com.example.test.domain.repository.ProcessedDataRepository
import com.example.test.util.toCalendarEntity
import com.example.test.util.toCallEntity
import com.example.test.util.toImageEntity
import com.example.test.util.toMessageEntity
import com.example.test.util.toProcessedCalendar
import com.example.test.util.toProcessedCall
import com.example.test.util.toProcessedImage
import com.example.test.util.toProcessedMessage
import javax.inject.Inject

class ProcessedMemoryRepositoryImpl @Inject constructor(
    val calendarDao: CalendarDao,
    val callDao: CallDao,
    val messageDao: MessageDao,
    val imageDao: ImageDao
    ): ProcessedDataRepository {
    //Calendar
    override suspend fun getProcessedCalendar(): List<ProcessedCalendar> =
        calendarDao.getAllCalendarEntity().map { it.toProcessedCalendar() }
    override suspend fun deleteAllProcessedCalendar() = calendarDao.deleteAllCalendarTable()
    override suspend fun insertProcessedCalendar(processedCalendar: ProcessedCalendar) =
        calendarDao.insertCalendarEntity(processedCalendar.toCalendarEntity())

    //Call
    override suspend fun getProcessedCall(): List<ProcessedCall> =
        callDao.getAllCallEntity().map{ it.toProcessedCall() }
    override suspend fun deleteAllProcessedCall() = callDao.deleteAllCallTable()
    override suspend fun insertProcessedCall(processedCall: ProcessedCall) =
        callDao.insertCallEntity(processedCall.toCallEntity())

    //Message
    override suspend fun getProcessedMessage(): List<ProcessedMessage> =
        messageDao.getAllMessageEntity().map{it.toProcessedMessage()}
    override suspend fun deleteAllProcessedMessage() = messageDao.deleteAllMessageTable()
    override suspend fun insertProcessedMessage(processedMessage: ProcessedMessage) =
        messageDao.insertMessageEntity(processedMessage.toMessageEntity())
    override suspend fun getLatestMessageDate() : Long? = messageDao.getLatestMaxDate()

    //Image
    override suspend fun getProcessedImage(): List<ProcessedImage> =
        imageDao.getAllImageEntity().map{it.toProcessedImage()}
    override suspend fun deleteAllProcessedImage() = imageDao.deleteAllImageTable()
    override suspend fun insertProcessedImage(processedImage: ProcessedImage) =
        imageDao.insertImageEntity(processedImage.toImageEntity())
    override suspend fun getLatestImageDate() : Long? = imageDao.getLatestMaxDate()
}