package net.thunderbird.core.preference.display.visualSettings.message.tags

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import net.thunderbird.core.logging.Logger
import net.thunderbird.core.preference.PreferenceChangeBroker
import net.thunderbird.core.preference.PreferenceChangeSubscriber
import net.thunderbird.core.preference.PreferenceScope
import net.thunderbird.core.preference.storage.StorageEditor
import net.thunderbird.core.preference.storage.StoragePersister

private const val TAG = "DefaultMessageTagSettingsPreferenceManager"

class DefaultMessageTagSettingsPreferenceManager(
    private val logger: Logger,
    private val storagePersister: StoragePersister,
    private val storageEditor: StorageEditor,
    preferenceChangeBroker: PreferenceChangeBroker,
) : MessageTagSettingsPreferenceManager, PreferenceChangeSubscriber {

    init {
        preferenceChangeBroker.subscribe(this)
    }

    private val configState = MutableStateFlow(value = loadConfig())

    override fun save(config: MessageTagSettings) {
        logger.debug(TAG) { "save() called with ${config.tags.size} tag(s)" }
        writeConfig(config)
        configState.update { config }
    }

    override fun getConfig(): MessageTagSettings = configState.value

    override fun getConfigFlow(): Flow<MessageTagSettings> = configState

    private fun loadConfig(): MessageTagSettings {
        // Always load fresh values. A cached Storage instance would not contain changes made after it was created.
        val storage = storagePersister.loadValues()
        return MessageTagSettingsCodec.decode(storage.getStringOrNull(MessageTagSettingKey.MessageTags.value))
    }

    private fun writeConfig(config: MessageTagSettings) {
        val value = if (config.tags.isEmpty()) null else MessageTagSettingsCodec.encode(config)
        storageEditor.putString(MessageTagSettingKey.MessageTags.value, value)
        storageEditor.commit().also { committed ->
            logger.verbose(TAG) { "writeConfig: storageEditor.commit() resulted in: $committed" }
        }
    }

    override fun receive(scope: PreferenceScope) {
        if (scope == PreferenceScope.ALL || scope == PreferenceScope.DISPLAY_VISUAL_MESSAGE_TAGS) {
            configState.update { loadConfig() }
        }
    }
}
