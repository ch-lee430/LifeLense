package com.example.test.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.entity.ImageEntity

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageEntity(imageEntity: ImageEntity)

    @Query("SELECT * FROM image_table")
    fun getAllImageEntity(): List<ImageEntity>

    @Query("DELETE FROM image_table")
    suspend fun deleteAllImageTable()

    @Query("SELECT MAX(date) FROM image_table")
    suspend fun getLatestMaxDate(): Long?
}