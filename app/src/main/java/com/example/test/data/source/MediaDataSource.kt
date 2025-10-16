package com.example.test.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.test.data.model.MediaData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MediaDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun getMedia(startTime: Long, endTime: Long): List<MediaData> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaData>()
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.DATE_ADDED} >= ? AND ${MediaStore.Files.FileColumns.DATE_ADDED} <= ? AND (${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'video/%')"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.toSeconds(startTime).toString(),
            TimeUnit.MILLISECONDS.toSeconds(endTime).toString()
        )

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val date = cursor.getLong(dateColumn) * 1000L // 초를 밀리초로 변환
                val mimeType = cursor.getString(mimeTypeColumn)
                val contentUri: Uri = ContentUris.withAppendedId(collection, id)

                mediaList.add(MediaData(contentUri.toString(), date, mimeType))
            }
        }
        mediaList
    }
}