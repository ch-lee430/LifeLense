package com.example.test.domain.repository

import com.example.test.domain.model.DateRangeAnalysis

interface GeminiRepository {
    suspend fun generateAnswer(question: String) : String
    suspend fun extractDateRange(question: String, currentTimestamp: Long): DateRangeAnalysis
}