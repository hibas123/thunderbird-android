package com.fsck.k9.ui.messagelist.item

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSetting
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettings

class MessageTagResolverTest {
    private val testSubject = MessageTagResolver(
        defaultTagNames = listOf("Important", "Work", "Personal", "To Do", "Later"),
    )

    @Test
    fun `without keywords should return no tags`() {
        val result = testSubject.resolve(emptySet())

        assertThat(result).isEmpty()
    }

    @Test
    fun `default label keywords should use localized name and desktop color`() {
        val result = testSubject.resolve(setOf("\$label1", "\$label4"))

        assertThat(result).containsExactly(
            MessageTag(keyword = "\$label1", name = "Important", color = 0xFFFF0000.toInt()),
            MessageTag(keyword = "\$label4", name = "To Do", color = 0xFF3333FF.toInt()),
        )
    }

    @Test
    fun `label keywords should be matched case insensitively`() {
        val result = testSubject.resolve(setOf("\$Label2"))

        assertThat(result.single().name).isEqualTo("Work")
    }

    @Test
    fun `unknown keyword should use keyword as name`() {
        val result = testSubject.resolve(setOf("Project-X"))

        assertThat(result.single().name).isEqualTo("Project-X")
    }

    @Test
    fun `label keyword outside of the default range should be hidden unless declared`() {
        assertThat(testSubject.resolve(setOf("\$label6"))).isEmpty()

        val settings = MessageTagSettings(listOf(MessageTagSetting(keyword = "\$label6")))
        assertThat(testSubject.resolve(setOf("\$label6"), settings).single().name).isEqualTo("\$label6")
    }

    @Test
    fun `tags should be sorted by default label first and custom keywords alphabetically`() {
        val result = testSubject.resolve(setOf("zebra", "\$label3", "Apple", "\$label1"))

        assertThat(result.map { it.name }).containsExactly("Important", "Personal", "Apple", "zebra")
    }

    @Test
    fun `customized name and color should override defaults`() {
        val settings = MessageTagSettings(
            listOf(MessageTagSetting(keyword = "\$label1", name = "Urgent", color = 0xFF112233.toInt())),
        )

        val result = testSubject.resolve(setOf("\$label1"), settings)

        assertThat(result).containsExactly(
            MessageTag(keyword = "\$label1", name = "Urgent", color = 0xFF112233.toInt()),
        )
    }

    @Test
    fun `customization with only a color should keep the default name`() {
        val settings = MessageTagSettings(listOf(MessageTagSetting(keyword = "\$label2", color = 0xFF010203.toInt())))

        val result = testSubject.resolve(setOf("\$label2"), settings)

        assertThat(result.single().name).isEqualTo("Work")
        assertThat(result.single().color).isEqualTo(0xFF010203.toInt())
    }

    @Test
    fun `customization should apply to custom keywords ignoring case`() {
        val settings = MessageTagSettings(listOf(MessageTagSetting(keyword = "project-x", name = "Project X")))

        val result = testSubject.resolve(setOf("Project-X"), settings)

        assertThat(result.single().name).isEqualTo("Project X")
    }

    @Test
    fun `keywords starting with dollar sign should be hidden unless they are default tags`() {
        val result = testSubject.resolve(setOf("\$Junk", "\$hasattachment", "\$Important", "\$label3", "Visible"))

        assertThat(result.map { it.keyword }).containsExactly("\$label3", "Visible")
    }

    @Test
    fun `keyword starting with dollar sign should be shown when declared in settings`() {
        val settings = MessageTagSettings(listOf(MessageTagSetting(keyword = "\$important", name = "Priority")))

        val result = testSubject.resolve(setOf("\$Important", "\$Junk"), settings)

        assertThat(result).containsExactly(
            MessageTag(keyword = "\$Important", name = "Priority", color = 0xFF607D8B.toInt()),
        )
    }

    @Test
    fun `hidden keyword should not affect default label keywords of other casing`() {
        val result = testSubject.resolve(setOf("\$Label5"))

        assertThat(result.single().name).isEqualTo("Later")
    }

    @Test
    fun `wrong number of default names should fail`() {
        assertFailure {
            MessageTagResolver(defaultTagNames = listOf("Only one"))
        }.isInstanceOf<IllegalArgumentException>()
    }
}
