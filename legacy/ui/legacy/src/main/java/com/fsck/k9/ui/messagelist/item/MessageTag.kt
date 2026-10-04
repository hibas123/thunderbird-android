package com.fsck.k9.ui.messagelist.item

import androidx.annotation.ColorInt

/**
 * A tag (IMAP keyword) that is displayed for a message in the message list.
 */
internal data class MessageTag(
    val keyword: String,
    val name: String,
    @param:ColorInt val color: Int,
)

/**
 * Resolves the IMAP keywords of a message to [MessageTag]s that can be displayed.
 *
 * The keywords `$label1` to `$label5` are the tags Thunderbird for desktop creates by default. They get a
 * localized name and the same color that is used on desktop. All other keywords are displayed using the keyword as
 * name and a neutral color.
 *
 * @param defaultTagNames Display names for `$label1` to `$label5`, in that order.
 */
internal class MessageTagResolver(private val defaultTagNames: List<String>) {
    init {
        require(defaultTagNames.size == DEFAULT_TAG_COLORS.size) {
            "Expected ${DEFAULT_TAG_COLORS.size} default tag names, got ${defaultTagNames.size}"
        }
    }

    fun resolve(keywords: Set<String>): List<MessageTag> {
        return keywords
            .map { keyword -> createTag(keyword) }
            .sortedWith(compareBy({ it.sortGroup }, { it.sortKey }))
            .map { it.tag }
    }

    private fun createTag(keyword: String): SortableTag {
        val labelIndex = labelIndexOf(keyword)
        return if (labelIndex != null) {
            SortableTag(
                tag = MessageTag(keyword, defaultTagNames[labelIndex], DEFAULT_TAG_COLORS[labelIndex]),
                sortGroup = 0,
                sortKey = labelIndex.toString(),
            )
        } else {
            SortableTag(
                tag = MessageTag(keyword, keyword, FALLBACK_TAG_COLOR),
                sortGroup = 1,
                sortKey = keyword.lowercase(),
            )
        }
    }

    private fun labelIndexOf(keyword: String): Int? {
        val match = LABEL_KEYWORD_REGEX.matchEntire(keyword) ?: return null
        return match.groupValues[1].toInt() - 1
    }

    private class SortableTag(val tag: MessageTag, val sortGroup: Int, val sortKey: String)

    companion object {
        private val LABEL_KEYWORD_REGEX = Regex("""\${'$'}label([1-5])""", RegexOption.IGNORE_CASE)

        // Colors used by Thunderbird for desktop for its default tags: Important, Work, Personal, To Do, Later
        private val DEFAULT_TAG_COLORS = listOf(
            0xFFFF0000.toInt(),
            0xFFFF9900.toInt(),
            0xFF009900.toInt(),
            0xFF3333FF.toInt(),
            0xFF993399.toInt(),
        )
        private val FALLBACK_TAG_COLOR = 0xFF607D8B.toInt()
    }
}
