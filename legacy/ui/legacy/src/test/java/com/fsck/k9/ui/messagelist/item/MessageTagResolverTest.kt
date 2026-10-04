package com.fsck.k9.ui.messagelist.item

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

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
    fun `label keyword outside of the default range should be treated as custom keyword`() {
        val result = testSubject.resolve(setOf("\$label6"))

        assertThat(result.single().name).isEqualTo("\$label6")
    }

    @Test
    fun `tags should be sorted by default label first and custom keywords alphabetically`() {
        val result = testSubject.resolve(setOf("zebra", "\$label3", "Apple", "\$label1"))

        assertThat(result.map { it.name }).containsExactly("Important", "Personal", "Apple", "zebra")
    }

    @Test
    fun `wrong number of default names should fail`() {
        assertFailure {
            MessageTagResolver(defaultTagNames = listOf("Only one"))
        }.isInstanceOf<IllegalArgumentException>()
    }
}
