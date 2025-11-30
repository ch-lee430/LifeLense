package com.example.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long =0,
    val uri: String,
    val date: Long,
    val mimeType: String, // 파일 유형은 내부 처리 및 구분을 위해 유지
    val summary: String
)