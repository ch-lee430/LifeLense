package com.example.test.util

import com.example.test.data.entity.CalendarEntity
import com.example.test.data.entity.CallEntity
import com.example.test.data.entity.MessageEntity
import com.example.test.domain.model.ProcessedCalendar
import com.example.test.domain.model.ProcessedCall
import com.example.test.domain.model.ProcessedMessage
import java.text.SimpleDateFormat
import java.util.Locale


fun CalendarEntity.toProcessedCalendar(): ProcessedCalendar =
    ProcessedCalendar(
        title = this.title,
        date  = this.date
    )

fun CallEntity.toProcessedCall(): ProcessedCall =
    ProcessedCall(
        date = this.date,
        personName = this.personName,
        summary = this.summary
    )

fun MessageEntity.toProcessedMessage(): ProcessedMessage =
    ProcessedMessage(
    date = this.date,
    personName = this.personName,
    summary = this.summary
    )


fun ProcessedCalendar.toCalendarEntity(): CalendarEntity
    = CalendarEntity(
    title = this.title,
    date = this.date
    )

fun ProcessedCall.toCallEntity(): CallEntity
    =  CallEntity(
    date = this.date,
    personName = this.personName,
    summary = this.summary
    )

fun ProcessedMessage.toMessageEntity(): MessageEntity
    = MessageEntity(
    date = this.date,
    personName = this.personName,
    summary = this.summary
    )



fun String.toTimestamp(): Long {
    try {
        // 입력하고 싶은 날짜 형식 (예: 2025-11-29 18:00)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        return dateFormat.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        // 혹시 오타가 나면 에러 내지 말고 그냥 '현재 시간'을 반환 (안전장치)
        return System.currentTimeMillis()
    }
}