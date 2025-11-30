package com.example.test.domain.repository

import com.example.test.domain.model.DateRangeAnalysis
import com.example.test.domain.model.Memory

interface GeminiRepository {
    suspend fun generateAnswer(question: String) : String
    suspend fun extractDateRange(question: String, currentTimestamp: Long): DateRangeAnalysis

    suspend fun generateMemoryResponse(question:String, contextString: String): Memory?
}