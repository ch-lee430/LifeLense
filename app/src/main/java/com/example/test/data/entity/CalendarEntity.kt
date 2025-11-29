package com.example.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_table")
data class CalendarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long =0,
    val date: Long,
    val title: String
)