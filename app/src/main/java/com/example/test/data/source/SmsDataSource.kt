package com.example.test.data.source

import android.content.Context
import android.net.Uri
import com.example.test.data.model.SmsData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    // Note: Resolving contactName from phoneNumber requires an additional ContentResolver query (ContactsContract)
    // and the READ_CONTACTS permission, which is not strictly handled here for simplicity.
    // We will use a basic placeholder for contactName resolution if actual name is not available.
    private fun getContactName(phoneNumber: String): String {
        // 실제 앱에서는 ContactsContract 쿼리를 통해 이름을 조회해야 합니다.
        // 여기서는 더미 로직으로 '알 수 없음'을 반환합니다.
        if (phoneNumber.length < 5) return phoneNumber // 서비스 번호는 그대로 사용
        return "알 수 없음" // 기본값
    }

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
                val phoneNumber = cursor.getString(addressColumn)
                val body = cursor.getString(bodyColumn)
                val date = cursor.getLong(dateColumn)
                val type = cursor.getInt(typeColumn)

                val contactName = getContactName(phoneNumber)

                smsList.add(
                    SmsData(
                        body = body,
                        phoneNumber = phoneNumber,
                        contactName = contactName,
                        date = date
                    )
                )
            }
        }
        smsList
    }
}