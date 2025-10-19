package com.example.test.di

import com.example.test.data.repository.GeminiRepositoryImpl
import com.example.test.data.repository.MemoryRepositoryImpl
import com.example.test.domain.repository.GeminiRepository
import com.example.test.domain.repository.MemoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMemoryRepository(
        impl: MemoryRepositoryImpl
    ): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(
        impl: GeminiRepositoryImpl
    ): GeminiRepository
}