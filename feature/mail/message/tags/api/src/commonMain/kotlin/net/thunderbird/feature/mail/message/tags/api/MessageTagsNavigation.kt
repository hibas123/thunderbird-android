package net.thunderbird.feature.mail.message.tags.api

import kotlinx.serialization.Serializable
import net.thunderbird.core.ui.navigation.Navigation
import net.thunderbird.core.ui.navigation.Route

const val MESSAGE_TAGS_BASE_DEEP_LINK = "app://feature/mail/message/tags"

/**
 * Route to the screen that allows customizing the names and colors of message tags.
 */
@Serializable
data object MessageTagsRoute : Route {
    override val basePath: String = MESSAGE_TAGS_BASE_DEEP_LINK

    override fun route(): String = basePath
}

interface MessageTagsNavigation : Navigation<MessageTagsRoute>
