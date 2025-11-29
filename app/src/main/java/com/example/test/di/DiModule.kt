package com.example.test.di

import com.example.test.data.repositoryImpl.GeminiRepositoryImpl
import com.example.test.data.repositoryImpl.MemoryRepositoryImpl
import com.example.test.data.repositoryImpl.ProcessedMemoryRepositoryImpl
import com.example.test.domain.repository.GeminiRepository
import com.example.test.domain.repository.MemoryRepository
import com.example.test.domain.repository.ProcessedDataRepository
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

    @Binds
    @Singleton
    abstract fun bindProcessedDataRepository(
        impl: ProcessedMemoryRepositoryImpl
    ): ProcessedDataRepository
}