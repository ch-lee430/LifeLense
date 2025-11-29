package com.example.test.domain.repository

import com.example.test.domain.model.ProcessedCalendar
import com.example.test.domain.model.ProcessedCall
import com.example.test.domain.model.ProcessedMessage

interface ProcessedDataRepository {
    //Calendar
    suspend fun getProcessedCalendar(): List<ProcessedCalendar>
    suspend fun deleteAllProcessedCalendar()
    suspend fun insertProcessedCalendar(processedCalendar: ProcessedCalendar)

    //Call
    suspend fun getProcessedCall(): List<ProcessedCall>
    suspend fun deleteAllProcessedCall()
    suspend fun insertProcessedCall(processedCall: ProcessedCall)

    //Message
    suspend fun getProcessedMessage(): List<ProcessedMessage>
    suspend fun deleteAllProcessedMessage()
    suspend fun insertProcessedMessage(processedMessage: ProcessedMessage)
}