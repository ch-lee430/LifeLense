package com.example.test.data.source

import android.content.Context
import android.provider.CallLog
import com.example.test.data.model.CallLogData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CallLogDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun getCallLogs(startTime: Long, endTime: Long): List<CallLogData> = withContext(Dispatchers.IO) {
        val callLogList = mutableListOf<CallLogData>()
        val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE)
        val selection = "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.DATE} <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val numberColumn = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateColumn = cursor.getColumnIndex(CallLog.Calls.DATE)
            val typeColumn = cursor.getColumnIndex(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberColumn)
                val date = cursor.getLong(dateColumn)
                val type = cursor.getInt(typeColumn)
                callLogList.add(CallLogData(number, date, type))
            }
        }
        callLogList
    }
}