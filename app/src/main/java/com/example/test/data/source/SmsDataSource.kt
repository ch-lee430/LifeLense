package com.example.test.data.source

import android.content.Context
import android.net.Uri
import com.example.test.data.model.SmsData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun getSmsMessages(startTime: Long, endTime: Long): List<SmsData> = withContext(Dispatchers.IO) {
        val smsList = mutableListOf<SmsData>()
        val uri = Uri.parse("content://sms")

        val projection = arrayOf("_id", "address", "body", "date", "type")
        val selection = "date >= ? AND date <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "date DESC"
        )?.use { cursor ->
            val addressColumn = cursor.getColumnIndex("address")
            val bodyColumn = cursor.getColumnIndex("body")
            val dateColumn = cursor.getColumnIndex("date")
            val typeColumn = cursor.getColumnIndex("type")

            while (cursor.moveToNext()) {
                val sender = cursor.getString(addressColumn)
                val body = cursor.getString(bodyColumn)
                val date = cursor.getLong(dateColumn)
                val type = cursor.getInt(typeColumn)
                smsList.add(SmsData(body, sender, date, type))
            }
        }
        smsList
    }
}