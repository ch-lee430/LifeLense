package com.example.test.data.source

import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.example.test.data.model.CalendarData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalendarDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun getCalendarEvents(startTime: Long, endTime: Long): List<CalendarData> = withContext(Dispatchers.IO) {
        val calendarList = mutableListOf<CalendarData>()
        val uri: Uri = CalendarContract.Events.CONTENT_URI

        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.EVENT_LOCATION
        )

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} DESC"
        )?.use { cursor ->
            val titleColumn = cursor.getColumnIndex(CalendarContract.Events.TITLE)
            val descColumn = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val dateColumn = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
            val locationColumn = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)

            while (cursor.moveToNext()) {
                val title = cursor.getString(titleColumn)
                val description = cursor.getString(descColumn)
                val date = cursor.getLong(dateColumn)
                val location = cursor.getString(locationColumn)
                calendarList.add(CalendarData(title, description, date, location))
            }
        }
        calendarList
    }
}