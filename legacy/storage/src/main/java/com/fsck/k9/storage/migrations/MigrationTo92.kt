package com.fsck.k9.storage.migrations

import android.database.sqlite.SQLiteDatabase

/**
 * Migration to version 92.
 *
 * Adds the `message_keywords` table to store IMAP keywords (user defined tags) of messages.
 */
internal class MigrationTo92(private val db: SQLiteDatabase) {
    fun addMessageKeywordsTable() {
        db.execSQL("DROP TABLE IF EXISTS message_keywords")
        db.execSQL(
            "CREATE TABLE message_keywords (" +
                "message_id INTEGER NOT NULL REFERENCES messages(id) ON DELETE CASCADE, " +
                "keyword TEXT NOT NULL, " +
                "PRIMARY KEY (message_id, keyword)" +
                ")",
        )
    }
}
