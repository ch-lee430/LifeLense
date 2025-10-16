package com.example.test.data.model

// 갤러리 이미지/동영상 정보를 담는 데이터 모델
data class MediaData(
    val uri: String,
    val date: Long,
    val mimeType: String // "image/jpeg", "video/mp4" 등
)

// 문자 메시지 정보를 담는 데이터 모델
data class SmsData(
    val body: String,
    val sender: String,
    val date: Long,
    val type: Int // 수신(1), 발신(2)
)

// 통화 기록 정보를 담는 데이터 모델
data class CallLogData(
    val phoneNumber: String,
    val date: Long,
    val type: Int // 수신(1), 발신(2), 부재중(3)
)

// 캘린더 일정 정보를 담는 데이터 모델
data class CalendarData(
    val title: String,
    val description: String?,
    val date: Long,
    val location: String?
)