package com.fsck.k9.mail.store.imap

import com.fsck.k9.mail.internet.MimeMessage

class ImapMessage(uid: String) : MimeMessage() {
    init {
        this.mUid = uid
    }

    /**
     * IMAP keywords (user defined flags, e.g. `$label1`) set on the message.
     *
     * System flags and keywords that are mapped to a [net.thunderbird.core.common.mail.Flag] are not included.
     */
    var keywords: Set<String> = emptySet()

    fun setSize(size: Int) {
        this.mSize = size
    }
}
