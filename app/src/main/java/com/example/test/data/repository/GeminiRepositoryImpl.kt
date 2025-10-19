package com.example.test.data.repository

import com.example.test.data.source.GeminiSource
import com.example.test.domain.repository.GeminiRepository
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val geminiSource: GeminiSource
): GeminiRepository{
    override suspend fun generateAnswer(question: String): String {
       return geminiSource.generateGeminiAnswer(question)
    }
}