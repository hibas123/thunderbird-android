package net.thunderbird.core.preference.display.visualSettings.message.tags

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class MessageTagSettingsCodecTest {
    @Test
    fun `decode of null should return empty settings`() {
        assertThat(MessageTagSettingsCodec.decode(null).tags).isEmpty()
    }

    @Test
    fun `decode of empty string should return empty settings`() {
        assertThat(MessageTagSettingsCodec.decode("").tags).isEmpty()
    }

    @Test
    fun `encoded settings should be decoded to the same settings`() {
        val settings = MessageTagSettings(
            tags = listOf(
                MessageTagSetting(keyword = "\$label1", name = "Urgent", color = 0xFFFF0000.toInt()),
                MessageTagSetting(keyword = "\$label2", name = null, color = 0xFF00FF00.toInt()),
                MessageTagSetting(keyword = "Project-X", name = "Project X", color = null),
            ),
        )

        val result = MessageTagSettingsCodec.decode(MessageTagSettingsCodec.encode(settings))

        assertThat(result).isEqualTo(settings)
    }

    @Test
    fun `names with separator and escape characters should survive a round trip`() {
        val settings = MessageTagSettings(
            tags = listOf(MessageTagSetting(keyword = "tag", name = "a\tb\nc\rd\\e\\n", color = null)),
        )

        val result = MessageTagSettingsCodec.decode(MessageTagSettingsCodec.encode(settings))

        assertThat(result).isEqualTo(settings)
    }

    @Test
    fun `decode should skip lines with wrong number of fields`() {
        val result = MessageTagSettingsCodec.decode("only-keyword\nkeyword\tname\nok\tName\t1")

        assertThat(result.tags).isEqualTo(listOf(MessageTagSetting("ok", "Name", 1)))
    }

    @Test
    fun `decode should skip lines with invalid color`() {
        val result = MessageTagSettingsCodec.decode("bad\tName\tred\ngood\tName\t")

        assertThat(result.tags).isEqualTo(listOf(MessageTagSetting("good", "Name", null)))
    }

    @Test
    fun `decode should skip lines with invalid keyword`() {
        val result = MessageTagSettingsCodec.decode("\t\t\nwith space\tName\t\nok\tName\t")

        assertThat(result.tags).isEqualTo(listOf(MessageTagSetting("ok", "Name", null)))
    }

    @Test
    fun `decode should treat blank name as no customization`() {
        val result = MessageTagSettingsCodec.decode("tag\t   \t5")

        assertThat(result.tags.single().name).isNull()
    }

    @Test
    fun `decode should truncate too long names`() {
        val longName = "x".repeat(MESSAGE_TAG_NAME_MAX_LENGTH + 10)

        val result = MessageTagSettingsCodec.decode("tag\t$longName\t")

        assertThat(result.tags.single().name).isEqualTo("x".repeat(MESSAGE_TAG_NAME_MAX_LENGTH))
    }

    @Test
    fun `decode should use last entry for duplicate keywords ignoring case`() {
        val result = MessageTagSettingsCodec.decode("\$Label1\tFirst\t\n\$label1\tSecond\t")

        assertThat(result.tags).isEqualTo(listOf(MessageTagSetting("\$label1", "Second", null)))
    }

    @Test
    fun `decode should limit the number of tags`() {
        val value = (1..MESSAGE_TAG_SETTINGS_MAX_TAGS + 20).joinToString("\n") { "tag$it\tName\t" }

        val result = MessageTagSettingsCodec.decode(value)

        assertThat(result.tags).hasSize(MESSAGE_TAG_SETTINGS_MAX_TAGS)
    }
}
