package com.example.test.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.test.data.dao.CalendarDao
import com.example.test.data.dao.CallDao
import com.example.test.data.dao.ImageDao
import com.example.test.data.dao.MessageDao
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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // 방법 A: SQL 쿼리로 직접 넣기 (Hilt 순환 참조 문제 없이 가장 깔끔함)
                // 현재 시간을 구해서 Long 타입으로 변환
                val now = System.currentTimeMillis()
                val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA)

                // 날짜 변환 헬퍼 함수 (파싱 실패 시 현재 시간 반환)
                fun getDate(dateString: String): Long {
                    return sdf.parse(dateString)?.time ?: System.currentTimeMillis()
                }

                // ---------------------------------------------------------
                // 1. MessageEntity (문자) 데이터 삽입
                // ---------------------------------------------------------
                // 2025.11.28 - 배재준 (프로젝트 ppt 개발)
                // ※ MessageEntity에는 전화번호 필드가 없으므로 personName에 이름만 넣거나, 필요시 '배재준(010...)' 처럼 병기 가능합니다.
                // 여기서는 이름만 넣겠습니다.
                val date1 = getDate("2025.11.28")
                db.execSQL("""
            INSERT INTO message_table (date, personName, summary) 
            VALUES ($date1, '배재준', '프로젝트 ppt 개발에 대한 내용')
        """.trimIndent())

                // 2025.11.14 - 경북대학교 (졸업자격 인정원 접수안내)
                val date2 = getDate("2025.11.14")
                db.execSQL("""
            INSERT INTO message_table (date, personName, summary) 
            VALUES ($date2, '경북대학교', '졸업자격 인정원 접수안내 (문의: 053-950-6508)')
        """.trimIndent())


                // ---------------------------------------------------------
                // 2. CalendarEntity (일정) 데이터 삽입
                // ---------------------------------------------------------
                // 2025.11.29 - 미용실 가기
                val date3 = getDate("2025.11.29")
                db.execSQL("""
            INSERT INTO calendar_table (date, title) 
            VALUES ($date3, '미용실 가기')
        """.trimIndent())
            }
        })
            .fallbackToDestructiveMigration()
            .build()


    }

    // 2. DAO 제공 (Repository가 이걸 가져다 씀)
    @Provides
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
    @Provides
    fun provideCallDao(database: AppDatabase): CallDao {
        return database.callDao()
    }
    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao {
        return database.imageDao()
    }
}