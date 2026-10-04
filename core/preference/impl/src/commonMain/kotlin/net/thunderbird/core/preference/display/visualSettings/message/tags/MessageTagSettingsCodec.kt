package net.thunderbird.core.preference.display.visualSettings.message.tags

internal const val MESSAGE_TAG_SETTINGS_MAX_TAGS = 100

private const val FIELD_SEPARATOR = '\t'
private const val LINE_SEPARATOR = '\n'
private const val ESCAPE = '\\'
private const val FIELD_COUNT = 3

/**
 * Converts [MessageTagSettings] to and from the single string value that is stored in the preference storage.
 *
 * Format: One line per tag, fields are `keyword`, `name`, and `color`, separated by a tab character. `name` and
 * `color` are empty if there is no customization. Backslash, tab, carriage return, and newline inside of fields are
 * escaped with a backslash.
 *
 * The stored value can come from an imported settings file. Decoding is therefore lenient and skips invalid entries
 * instead of failing.
 */
internal object MessageTagSettingsCodec {
    fun encode(settings: MessageTagSettings): String {
        return settings.tags.joinToString(separator = LINE_SEPARATOR.toString()) { tag ->
            listOf(
                escape(tag.keyword),
                escape(tag.name.orEmpty()),
                tag.color?.toString().orEmpty(),
            ).joinToString(separator = FIELD_SEPARATOR.toString())
        }
    }

    fun decode(value: String?): MessageTagSettings {
        if (value.isNullOrEmpty()) return MessageTagSettings()

        return value
            .split(LINE_SEPARATOR)
            .asSequence()
            .mapNotNull { line -> decodeLine(line) }
            .fold(MessageTagSettings()) { settings, tag -> settings.withTag(tag) }
            .let { settings -> settings.copy(tags = settings.tags.take(MESSAGE_TAG_SETTINGS_MAX_TAGS)) }
    }

    private fun decodeLine(line: String): MessageTagSetting? {
        val fields = line.split(FIELD_SEPARATOR)
        if (fields.size != FIELD_COUNT) return null

        val keyword = unescape(fields[0]).trim()
        if (!isValidKeyword(keyword)) return null

        val name = unescape(fields[1])
            .trim()
            .take(MESSAGE_TAG_NAME_MAX_LENGTH)
            .ifBlank { null }

        val colorField = fields[2]
        val color = if (colorField.isEmpty()) null else colorField.toIntOrNull() ?: return null

        return MessageTagSetting(keyword, name, color)
    }

    private fun isValidKeyword(keyword: String): Boolean {
        return keyword.isNotEmpty() && keyword.none { it.isWhitespace() || it.isISOControl() }
    }

    private fun escape(value: String): String = buildString {
        for (char in value) {
            when (char) {
                ESCAPE -> append(ESCAPE).append(ESCAPE)
                FIELD_SEPARATOR -> append(ESCAPE).append('t')
                LINE_SEPARATOR -> append(ESCAPE).append('n')
                '\r' -> append(ESCAPE).append('r')
                else -> append(char)
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char == ESCAPE && index + 1 < value.length) {
                index++
                when (val escaped = value[index]) {
                    't' -> append(FIELD_SEPARATOR)
                    'n' -> append(LINE_SEPARATOR)
                    'r' -> append('\r')
                    else -> append(escaped)
                }
            } else {
                append(char)
            }
            index++
        }
    }
}
