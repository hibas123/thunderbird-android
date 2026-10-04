package net.thunderbird.feature.mail.message.tags.internal

import android.content.Context
import net.thunderbird.feature.mail.message.list.R as MessageListR

/**
 * Provides the localized names of the default tags.
 */
internal fun interface DefaultTagNameProvider {
    /**
     * @param index Zero-based index of the default tag, see
     * [net.thunderbird.core.preference.display.visualSettings.message.tags.DefaultMessageTags].
     */
    fun getName(index: Int): String
}

internal class ResourcesDefaultTagNameProvider(private val context: Context) : DefaultTagNameProvider {
    override fun getName(index: Int): String {
        return context.getString(NAME_RESOURCE_IDS[index])
    }

    private companion object {
        val NAME_RESOURCE_IDS = listOf(
            MessageListR.string.message_tag_important,
            MessageListR.string.message_tag_work,
            MessageListR.string.message_tag_personal,
            MessageListR.string.message_tag_to_do,
            MessageListR.string.message_tag_later,
        )
    }
}
