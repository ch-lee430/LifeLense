package com.example.test.domain.repository

interface GeminiRepository {
    suspend fun generateAnswer(question: String) : String
}