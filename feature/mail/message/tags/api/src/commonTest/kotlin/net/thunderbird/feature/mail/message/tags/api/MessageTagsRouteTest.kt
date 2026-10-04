package net.thunderbird.feature.mail.message.tags.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MessageTagsRouteTest {
    @Test
    fun `route should be the base deep link`() {
        assertThat(MessageTagsRoute.basePath).isEqualTo("app://feature/mail/message/tags")
        assertThat(MessageTagsRoute.route()).isEqualTo(MessageTagsRoute.basePath)
    }
}
