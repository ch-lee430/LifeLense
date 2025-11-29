package com.example.test.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageEntity(messageEntity: MessageEntity)

    @Query("SELECT * FROM message_table")
    fun getAllMessageEntity(): List<MessageEntity>

    @Query("DELETE FROM message_table")
    suspend fun deleteAllMessageTable()
}