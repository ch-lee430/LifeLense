package com.example.test.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.test.data.dao.CalendarDao
import com.example.test.data.dao.CallDao
import com.example.test.data.dao.MessageDao
import com.example.test.data.entity.CalendarEntity
import com.example.test.data.entity.CallEntity
import com.example.test.data.entity.MessageEntity


@Database(
    entities = [CalendarEntity::class,
                CallEntity::class,
                MessageEntity::class], // 여러 개 등록 가능
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // 외부에서 이 함수를 호출해 DAO를 가져갑니다.
    abstract fun calendarDao(): CalendarDao
    abstract fun callDao(): CallDao
    abstract fun messageDao(): MessageDao
}