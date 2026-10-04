package net.thunderbird.feature.mail.message.tags.internal

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.thunderbird.core.preference.display.visualSettings.message.tags.DefaultMessageTags
import net.thunderbird.core.preference.display.visualSettings.message.tags.MESSAGE_TAG_NAME_MAX_LENGTH
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSetting
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettings
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettingsPreferenceManager
import net.thunderbird.core.ui.contract.mvi.BaseViewModel
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Editor
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Effect
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Event
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.KeywordError
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.State
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.TagItem

/**
 * Characters that are not allowed in IMAP keywords (atom-specials, see RFC 3501), in addition to whitespace and
 * control characters.
 */
private const val KEYWORD_FORBIDDEN_CHARACTERS = "(){%*\"\\]"

internal class MessageTagsViewModel(
    private val settingsManager: MessageTagSettingsPreferenceManager,
    private val defaultTagNameProvider: DefaultTagNameProvider,
) : BaseViewModel<State, Event, Effect>(initialState = State()), MessageTagsContract.ViewModel {

    init {
        settingsManager.getConfigFlow()
            .onEach { settings -> updateState { it.copy(tags = createTagItems(settings).toImmutableList()) } }
            .launchIn(viewModelScope)
    }

    override fun event(event: Event) {
        when (event) {
            is Event.OnTagClick -> openEditor(event.keyword)
            Event.OnAddTagClick -> openEditorForNewTag()
            is Event.OnEditorKeywordChange -> updateEditor { it.copy(keyword = event.keyword, keywordError = null) }
            is Event.OnEditorNameChange -> updateEditor { it.copy(name = event.name.take(MESSAGE_TAG_NAME_MAX_LENGTH)) }
            is Event.OnEditorColorChange -> updateEditor { it.copy(color = event.color) }
            Event.OnEditorSaveClick -> saveEditor()
            Event.OnEditorResetClick -> resetEditorTag()
            Event.OnEditorDismiss -> updateState { it.copy(editor = null) }
            Event.OnBackClick -> emitEffect(Effect.NavigateBack)
        }
    }

    private fun createTagItems(settings: MessageTagSettings): List<TagItem> {
        val defaultItems = DefaultMessageTags.tags.mapIndexed { index, defaultTag ->
            val customization = settings.find(defaultTag.keyword)
            TagItem(
                keyword = defaultTag.keyword,
                name = customization?.name ?: defaultTagNameProvider.getName(index),
                color = customization?.color ?: defaultTag.color,
                isDefaultTag = true,
                isCustomized = customization != null,
            )
        }

        val customItems = settings.tags
            .filter { DefaultMessageTags.indexOf(it.keyword) == null }
            .map { setting ->
                TagItem(
                    keyword = setting.keyword,
                    name = setting.name ?: setting.keyword,
                    color = setting.color ?: DefaultMessageTags.FALLBACK_COLOR,
                    isDefaultTag = false,
                    isCustomized = true,
                )
            }
            .sortedBy { it.name.lowercase() }

        return defaultItems + customItems
    }

    private fun openEditor(keyword: String) {
        val item = state.value.tags.firstOrNull { it.keyword == keyword } ?: return

        updateState {
            it.copy(
                editor = Editor(
                    isNewTag = false,
                    keyword = item.keyword,
                    name = item.name,
                    color = item.color,
                    isDefaultTag = item.isDefaultTag,
                    canReset = item.isCustomized,
                ),
            )
        }
    }

    private fun openEditorForNewTag() {
        updateState {
            it.copy(
                editor = Editor(
                    isNewTag = true,
                    keyword = "",
                    name = "",
                    color = DefaultMessageTags.FALLBACK_COLOR,
                    isDefaultTag = false,
                    canReset = false,
                ),
            )
        }
    }

    private fun updateEditor(update: (Editor) -> Editor) {
        updateState { state -> state.copy(editor = state.editor?.let(update)) }
    }

    private fun saveEditor() {
        val editor = state.value.editor ?: return

        if (editor.isNewTag) {
            val error = validateKeyword(editor.keyword.trim())
            if (error != null) {
                updateEditor { it.copy(keywordError = error) }
                return
            }
        }

        val keyword = editor.keyword.trim()
        val setting = createSetting(keyword, editor)
        val settings = settingsManager.getConfig()
        val newSettings = if (setting == null) settings.withoutTag(keyword) else settings.withTag(setting)

        settingsManager.save(newSettings)
        updateState { it.copy(editor = null) }
    }

    /**
     * Creates the setting to store for the tag. Values that match the defaults are not stored. Returns `null` if
     * nothing about a default tag is customized.
     */
    private fun createSetting(keyword: String, editor: Editor): MessageTagSetting? {
        val defaultIndex = DefaultMessageTags.indexOf(keyword)
        val name = editor.name.trim()

        return if (defaultIndex != null) {
            val defaultName = defaultTagNameProvider.getName(defaultIndex)
            val customName = name.takeIf { it.isNotEmpty() && it != defaultName }
            val customColor = editor.color.takeIf { it != DefaultMessageTags.tags[defaultIndex].color }

            if (customName == null && customColor == null) {
                null
            } else {
                MessageTagSetting(keyword = keyword, name = customName, color = customColor)
            }
        } else {
            MessageTagSetting(
                keyword = keyword,
                name = name.takeIf { it.isNotEmpty() && it != keyword },
                color = editor.color.takeIf { it != DefaultMessageTags.FALLBACK_COLOR },
            )
        }
    }

    private fun validateKeyword(keyword: String): KeywordError? {
        return when {
            keyword.isEmpty() -> KeywordError.Empty

            keyword.length > MESSAGE_TAG_NAME_MAX_LENGTH || !keyword.all(::isValidKeywordCharacter) ->
                KeywordError.Invalid

            DefaultMessageTags.indexOf(keyword) != null || settingsManager.getConfig().find(keyword) != null ->
                KeywordError.AlreadyExists

            else -> null
        }
    }

    private fun isValidKeywordCharacter(char: Char): Boolean {
        return char.code in PRINTABLE_ASCII_RANGE && char !in KEYWORD_FORBIDDEN_CHARACTERS
    }

    private fun resetEditorTag() {
        val editor = state.value.editor ?: return

        settingsManager.save(settingsManager.getConfig().withoutTag(editor.keyword))
        updateState { it.copy(editor = null) }
    }

    private companion object {
        val PRINTABLE_ASCII_RANGE = 0x21..0x7E
    }
}
