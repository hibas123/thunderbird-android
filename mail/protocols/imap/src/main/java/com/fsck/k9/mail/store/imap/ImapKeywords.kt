package com.fsck.k9.mail.store.imap

internal object ImapKeywords {
    /**
     * Keywords that carry protocol-level meaning and are not meant to be shown to the user as tags.
     */
    private val hiddenKeywords = setOf(
        "\$mdnsent",
        "\$junk",
        "\$notjunk",
        "junk",
        "nonjunk",
        "\$phishing",
        "\$submitpending",
        "\$submitted",
    )

    /**
     * Returns `true` if [flag] is a user-defined keyword (i.e. not a `\\`-prefixed system flag) that should be shown
     * to the user as a tag.
     */
    fun isUserVisibleKeyword(flag: String): Boolean {
        return flag.isNotEmpty() && !flag.startsWith("\\") && flag.lowercase() !in hiddenKeywords
    }
}
