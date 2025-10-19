package com.example.test.data.model

// 갤러리 이미지/동영상 정보를 담는 데이터 모델 (uri, 날짜 포함)
data class MediaData(
    val uri: String,
    val date: Long,
    val mimeType: String // 파일 유형은 내부 처리 및 구분을 위해 유지
)

// 문자 메시지 정보를 담는 데이터 모델 (전화 번호, 상대방, 내용, 날짜)
data class SmsData(
    val body: String, // 내용
    val phoneNumber: String, // 전화 번호
    val contactName: String, // 상대방 이름 (저장된 이름, 없으면 '알 수 없음')
    val date: Long
)

// 통화 기록 정보를 담는 데이터 모델 (전화 번호, 상대방, 내용/길이, 날짜)
data class CallLogData(
    val phoneNumber: String, // 전화 번호
    val contactName: String, // 상대방 이름 (저장된 이름, 없으면 '알 수 없음')
    val date: Long,
    val duration: Long // 통화 내용/길이 (초 단위)
)

// 캘린더 일정 정보를 담는 데이터 모델 (유지)
data class CalendarData(
    val title: String,
    val description: String?,
    val date: Long,
    val location: String?
)
