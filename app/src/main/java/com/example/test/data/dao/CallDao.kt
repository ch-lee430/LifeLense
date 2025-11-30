package com.example.test.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.entity.CallEntity


@Dao
interface CallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallEntity(callEntity: CallEntity)

    @Query("SELECT * FROM call_table")
    fun getAllCallEntity(): List<CallEntity>

    @Query("DELETE FROM call_table")
    suspend fun deleteAllCallTable()
}