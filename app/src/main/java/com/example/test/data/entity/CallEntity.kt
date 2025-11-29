package com.example.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "call_table")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long =0,
    val date: Long,
    val personName: String,
    val summary: String
)