package com.fsck.k9.storage.messages

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.fsck.k9.mailstore.LockableDatabase

internal class KeywordMessageOperations(private val lockableDatabase: LockableDatabase) {

    fun getMessageKeywords(folderId: Long, messageServerId: String): Set<String> {
        return lockableDatabase.execute(false) { database ->
            database.rawQuery(
                """
SELECT keyword 
FROM message_keywords 
JOIN messages ON (messages.id = message_keywords.message_id)
WHERE messages.folder_id = ? AND messages.uid = ?
                """,
                arrayOf(folderId.toString(), messageServerId),
            ).use { cursor ->
                buildSet {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(0))
                    }
                }
            }
        }
    }

    fun setMessageKeywords(folderId: Long, messageServerId: String, keywords: Set<String>) {
        lockableDatabase.execute(true) { database ->
            val messageId = database.findMessageId(folderId, messageServerId)
                ?: error("Message not found $folderId:$messageServerId")

            database.delete("message_keywords", "message_id = ?", arrayOf(messageId.toString()))

            for (keyword in keywords) {
                val values = ContentValues().apply {
                    put("message_id", messageId)
                    put("keyword", keyword)
                }
                database.insertOrThrow("message_keywords", null, values)
            }
        }
    }

    private fun SQLiteDatabase.findMessageId(folderId: Long, messageServerId: String): Long? {
        return query(
            "messages",
            arrayOf("id"),
            "folder_id = ? AND uid = ?",
            arrayOf(folderId.toString(), messageServerId),
            null,
            null,
            null,
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }
}
