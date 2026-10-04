package com.fsck.k9.mail.store.imap

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class ImapKeywordsTest {
    @Test
    fun `user defined keywords should be visible`() {
        assertThat(ImapKeywords.isUserVisibleKeyword("\$label1")).isTrue()
        assertThat(ImapKeywords.isUserVisibleKeyword("Important")).isTrue()
    }

    @Test
    fun `system flags should not be visible`() {
        assertThat(ImapKeywords.isUserVisibleKeyword("\\Recent")).isFalse()
        assertThat(ImapKeywords.isUserVisibleKeyword("\\Seen")).isFalse()
    }

    @Test
    fun `protocol keywords should not be visible regardless of case`() {
        assertThat(ImapKeywords.isUserVisibleKeyword("\$MDNSent")).isFalse()
        assertThat(ImapKeywords.isUserVisibleKeyword("\$Junk")).isFalse()
        assertThat(ImapKeywords.isUserVisibleKeyword("NonJunk")).isFalse()
    }

    @Test
    fun `empty keyword should not be visible`() {
        assertThat(ImapKeywords.isUserVisibleKeyword("")).isFalse()
    }
}
