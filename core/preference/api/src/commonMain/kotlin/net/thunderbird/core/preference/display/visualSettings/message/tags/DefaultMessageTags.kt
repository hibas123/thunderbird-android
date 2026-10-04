package net.thunderbird.core.preference.display.visualSettings.message.tags

/**
 * A tag that is available by default. These are the tags Thunderbird for desktop creates, stored as the IMAP
 * keywords `$label1` to `$label5`.
 *
 * The display name is localized and therefore not part of this definition.
 *
 * @param keyword The IMAP keyword.
 * @param color The default color as ARGB value.
 */
data class DefaultMessageTag(
    val keyword: String,
    val color: Int,
)

object DefaultMessageTags {
    /**
     * The default tags in display order: Important, Work, Personal, To Do, Later.
     */
    val tags: List<DefaultMessageTag> = listOf(
        DefaultMessageTag(keyword = "\$label1", color = 0xFFFF0000.toInt()),
        DefaultMessageTag(keyword = "\$label2", color = 0xFFFF9900.toInt()),
        DefaultMessageTag(keyword = "\$label3", color = 0xFF009900.toInt()),
        DefaultMessageTag(keyword = "\$label4", color = 0xFF3333FF.toInt()),
        DefaultMessageTag(keyword = "\$label5", color = 0xFF993399.toInt()),
    )

    /**
     * The color for tags that are not one of the [tags] and don't have a customized color.
     */
    const val FALLBACK_COLOR: Int = 0xFF607D8B.toInt()

    /**
     * Returns the zero-based index of the default tag with the given [keyword] in [tags], or `null`.
     *
     * IMAP keywords are case-insensitive.
     */
    fun indexOf(keyword: String): Int? {
        return tags.indexOfFirst { it.keyword.equals(keyword, ignoreCase = true) }.takeIf { it >= 0 }
    }
}
