package com.example.test.domain.model

data class ProcessedMemory(
    val title: String,
    val content: String,
    val referenceData: List<ReferenceItem>
)

data class ProcessedCalendar(
    val id: Long,
    val title: String,
    val date: Long
)

data class ProcessedCall(
    val date: Long,
    val personName: String,
    val summary: String
)

data class ProcessedMessage(
    val id: Long,
    val date: Long,
    val personName: String,
    val summary: String
)

data class ProcessedImage(
    val uri: String,
    val date: Long,
    val mimeType: String, // 파일 유형은 내부 처리 및 구분을 위해 유지
    val summary: String
)