package net.thunderbird.core.preference.display.visualSettings.message.tags

import net.thunderbird.core.preference.PreferenceManager

enum class MessageTagSettingKey(val value: String) {
    MessageTags("messageTags"),
}

interface MessageTagSettingsPreferenceManager : PreferenceManager<MessageTagSettings>
