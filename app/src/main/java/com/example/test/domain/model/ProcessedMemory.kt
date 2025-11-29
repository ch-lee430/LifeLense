package com.example.test.domain.model

data class ProcessedMemory(
    val title: String,
    val date: Long,
    val content: String,
    val referenceData: List<ReferenceItem>
)

data class ProcessedCalendar(
    val title: String,
    val date: Long
)

data class ProcessedCall(
    val date: Long,
    val personName: String,
    val summary: String
)

data class ProcessedMessage(
    val date: Long,
    val personName: String,
    val summary: String
)
