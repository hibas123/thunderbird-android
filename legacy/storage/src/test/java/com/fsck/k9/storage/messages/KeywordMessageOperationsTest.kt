package com.fsck.k9.storage.messages

import android.database.sqlite.SQLiteDatabase
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.fsck.k9.storage.RobolectricTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class KeywordMessageOperationsTest : RobolectricTest() {
    private lateinit var sqliteDatabase: SQLiteDatabase
    private lateinit var testSubject: KeywordMessageOperations

    @Before
    fun setUp() {
        sqliteDatabase = createDatabase()
        testSubject = KeywordMessageOperations(createLockableDatabaseMock(sqliteDatabase))
    }

    @After
    fun tearDown() {
        sqliteDatabase.close()
    }

    @Test
    fun `getMessageKeywords() without keywords should return empty set`() {
        sqliteDatabase.createMessage(folderId = 1, uid = "uid1")

        val result = testSubject.getMessageKeywords(folderId = 1, messageServerId = "uid1")

        assertThat(result).isEmpty()
    }

    @Test
    fun `setMessageKeywords() should replace existing keywords`() {
        sqliteDatabase.createMessage(folderId = 1, uid = "uid1")
        testSubject.setMessageKeywords(1, "uid1", setOf("\$label1", "Old"))

        testSubject.setMessageKeywords(1, "uid1", setOf("\$label1", "New"))

        assertThat(testSubject.getMessageKeywords(1, "uid1")).isEqualTo(setOf("\$label1", "New"))
    }

    @Test
    fun `setMessageKeywords() should not affect other messages`() {
        sqliteDatabase.createMessage(folderId = 1, uid = "uid1")
        sqliteDatabase.createMessage(folderId = 1, uid = "uid2")
        testSubject.setMessageKeywords(1, "uid2", setOf("Keep"))

        testSubject.setMessageKeywords(1, "uid1", setOf("Other"))

        assertThat(testSubject.getMessageKeywords(1, "uid2")).isEqualTo(setOf("Keep"))
    }

    @Test
    fun `setMessageKeywords() with empty set should remove keywords`() {
        sqliteDatabase.createMessage(folderId = 1, uid = "uid1")
        testSubject.setMessageKeywords(1, "uid1", setOf("Old"))

        testSubject.setMessageKeywords(1, "uid1", emptySet())

        assertThat(testSubject.getMessageKeywords(1, "uid1")).isEmpty()
    }

    @Test
    fun `setMessageKeywords() for unknown message should fail`() {
        assertFailure {
            testSubject.setMessageKeywords(1, "unknown", setOf("Keyword"))
        }.isInstanceOf<IllegalStateException>()
    }

    @Test
    fun `deleting message should remove its keywords`() {
        sqliteDatabase.setForeignKeyConstraintsEnabled(true)
        val messageId = sqliteDatabase.createMessage(folderId = 1, uid = "uid1")
        testSubject.setMessageKeywords(1, "uid1", setOf("Keyword"))

        sqliteDatabase.delete("messages", "id = ?", arrayOf(messageId.toString()))

        val remaining = sqliteDatabase.rawQuery("SELECT COUNT(*) FROM message_keywords", null).use {
            it.moveToFirst()
            it.getInt(0)
        }
        assertThat(remaining).isEqualTo(0)
    }
}
