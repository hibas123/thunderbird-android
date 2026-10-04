package net.thunderbird.core.preference.display.visualSettings.message.tags

const val MESSAGE_TAG_NAME_MAX_LENGTH = 64

/**
 * The user's customizations of how message tags (IMAP keywords) are displayed.
 *
 * Only customizations are stored. Tags without an entry (e.g. the default tags `$label1` to `$label5`) are displayed
 * using their built-in name and color.
 */
data class MessageTagSettings(
    val tags: List<MessageTagSetting> = emptyList(),
) {
    /**
     * Returns the customization for the given IMAP [keyword], or `null` if there is none.
     *
     * IMAP keywords are case-insensitive.
     */
    fun find(keyword: String): MessageTagSetting? = tags.firstOrNull { it.keyword.equals(keyword, ignoreCase = true) }

    /**
     * Returns a copy with [tag] added or, if there already is a customization for the same keyword, replaced.
     */
    fun withTag(tag: MessageTagSetting): MessageTagSettings {
        val others = tags.filterNot { it.keyword.equals(tag.keyword, ignoreCase = true) }
        return copy(tags = others + tag)
    }

    /**
     * Returns a copy without the customization for the given [keyword].
     */
    fun withoutTag(keyword: String): MessageTagSettings {
        return copy(tags = tags.filterNot { it.keyword.equals(keyword, ignoreCase = true) })
    }
}

/**
 * Customization of a single tag.
 *
 * @param keyword The IMAP keyword this customization applies to, e.g. `$label1`.
 * @param name The name to display, or `null` to use the default name.
 * @param color The color to display as ARGB value, or `null` to use the default color.
 */
data class MessageTagSetting(
    val keyword: String,
    val name: String? = null,
    val color: Int? = null,
) {
    init {
        require(keyword.isNotBlank()) { "keyword must not be blank" }
        require(name == null || name.isNotBlank()) { "name must be null or not blank" }
        require(name == null || name.length <= MESSAGE_TAG_NAME_MAX_LENGTH) {
            "name must not be longer than $MESSAGE_TAG_NAME_MAX_LENGTH characters"
        }
    }
}
