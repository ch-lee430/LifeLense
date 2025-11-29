package com.example.test.di

import android.content.Context
import androidx.room.Room
import com.example.test.data.dao.CalendarDao
import com.example.test.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // 1. 데이터베이스 인스턴스 제공
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lifelens_database"
        ).build()
    }

    // 2. DAO 제공 (Repository가 이걸 가져다 씀)
    @Provides
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }
}