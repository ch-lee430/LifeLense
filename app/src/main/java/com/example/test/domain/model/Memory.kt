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
    val displayValue: String, // 카드에 표시될 값 (이름, URI, 이벤트 제목)
    val functionalValue: String, // 기능 실행에 필요한 값 (전화번호, URI 등)
    val date: Long // 기록의 날짜/시간
)

enum class ReferenceType {
    PHOTO, SMS, CALL, CALENDAR
}

// 캘린더 Dialog를 위한 새로운 데이터 모델 추가
data class CalendarEvent(
    val date: Long,
    val title: String // 이벤트 이름
)