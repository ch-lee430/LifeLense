package com.example.test.domain.model

// 최종적으로 사용자에게 보여줄 통합된 기억 정보를 담는 모델
data class Memory(
    val title: String,
    val date: Long,
    val content: String,
    val referenceData: List<ReferenceItem>
)

// 참고 데이터의 종류(이미지, 문자 등)와 내용을 담는 모델
data class ReferenceItem(
    val type: ReferenceType,
    val value: String
)

enum class ReferenceType {
    PHOTO, SMS, CALL, CALENDAR
}