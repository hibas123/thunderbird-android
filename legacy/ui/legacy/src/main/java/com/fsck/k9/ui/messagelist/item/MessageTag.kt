package com.fsck.k9.ui.messagelist.item

import androidx.annotation.ColorInt
import net.thunderbird.core.preference.display.visualSettings.message.tags.DefaultMessageTags
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettings

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
 * name and a neutral color. Name and color can be customized by the user, see [MessageTagSettings].
 *
 * @param defaultTagNames Display names for `$label1` to `$label5`, in that order.
 */
internal class MessageTagResolver(private val defaultTagNames: List<String>) {
    init {
        require(defaultTagNames.size == DefaultMessageTags.tags.size) {
            "Expected ${DefaultMessageTags.tags.size} default tag names, got ${defaultTagNames.size}"
        }
    }

    fun resolve(keywords: Set<String>, settings: MessageTagSettings = MessageTagSettings()): List<MessageTag> {
        return keywords
            .map { keyword -> createTag(keyword, settings) }
            .sortedWith(compareBy({ it.sortGroup }, { it.sortKey }))
            .map { it.tag }
    }

    private fun createTag(keyword: String, settings: MessageTagSettings): SortableTag {
        val labelIndex = DefaultMessageTags.indexOf(keyword)
        val customization = settings.find(keyword)

        val defaultName = if (labelIndex != null) defaultTagNames[labelIndex] else keyword
        val defaultColor = if (labelIndex != null) {
            DefaultMessageTags.tags[labelIndex].color
        } else {
            DefaultMessageTags.FALLBACK_COLOR
        }
        val tag = MessageTag(
            keyword = keyword,
            name = customization?.name ?: defaultName,
            color = customization?.color ?: defaultColor,
        )

        return if (labelIndex != null) {
            SortableTag(tag, sortGroup = 0, sortKey = labelIndex.toString())
        } else {
            SortableTag(tag, sortGroup = 1, sortKey = keyword.lowercase())
        }
    }

    private class SortableTag(val tag: MessageTag, val sortGroup: Int, val sortKey: String)
}
