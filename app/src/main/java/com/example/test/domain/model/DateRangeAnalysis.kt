package com.example.test.domain.model

import com.google.gson.annotations.SerializedName

//Gemini가 사용자 질문을 분석하여 반환할 날짜 범위 모델입니다.
data class DateRangeAnalysis(
    @SerializedName("start_timestamp")
    val startTimestamp: Long, // Unix timestamp in milliseconds (0L for no start date limit)
    @SerializedName("end_timestamp")
    val endTimestamp: Long,   // Unix timestamp in milliseconds (System.currentTimeMillis() for no end date limit)
    @SerializedName("is_specific")
    val isSpecific: Boolean   // 사용자가 특정 날짜/기간을 언급했는지 여부 (Vague 질문은 false)
)