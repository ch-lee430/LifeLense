package com.example.test.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.entity.CalendarEntity

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEntity(calendarEntity: CalendarEntity)

    @Query("SELECT * FROM calendar_table")
    fun getAllCalendarEntity(): List<CalendarEntity>
    //Repo에서 suspend로 감싸서 호출

    @Query("DELETE FROM calendar_table")
    suspend fun deleteAllCalendarTable()
}