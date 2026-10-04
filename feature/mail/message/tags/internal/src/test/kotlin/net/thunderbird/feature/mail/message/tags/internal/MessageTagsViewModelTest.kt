package net.thunderbird.feature.mail.message.tags.internal

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.thunderbird.core.preference.display.visualSettings.message.tags.DefaultMessageTags
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSetting
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettings
import net.thunderbird.core.preference.display.visualSettings.message.tags.MessageTagSettingsPreferenceManager
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.Event
import net.thunderbird.feature.mail.message.tags.internal.MessageTagsContract.KeywordError
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageTagsViewModelTest {
    private val settingsManager = FakeMessageTagSettingsPreferenceManager()
    private val defaultNames = listOf("Important", "Work", "Personal", "To Do", "Later")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `without customizations should list the five default tags`() {
        val testSubject = createTestSubject()

        val tags = testSubject.state.value.tags

        assertThat(tags.map { it.name }).containsExactly("Important", "Work", "Personal", "To Do", "Later")
        assertThat(tags.map { it.color }).isEqualTo(DefaultMessageTags.tags.map { it.color })
        assertThat(tags.all { it.isDefaultTag && !it.isCustomized }).isTrue()
    }

    @Test
    fun `customized default tag should show custom name and color`() {
        settingsManager.save(MessageTagSettings(listOf(MessageTagSetting("\$label1", "Urgent", 5))))

        val tag = createTestSubject().state.value.tags.first()

        assertThat(tag.name).isEqualTo("Urgent")
        assertThat(tag.color).isEqualTo(5)
        assertThat(tag.isCustomized).isTrue()
    }

    @Test
    fun `custom tags should be listed after default tags`() {
        settingsManager.save(MessageTagSettings(listOf(MessageTagSetting("project-x", "Project X", 7))))

        val tags = createTestSubject().state.value.tags

        assertThat(tags.last().keyword).isEqualTo("project-x")
        assertThat(tags.last().isDefaultTag).isFalse()
    }

    @Test
    fun `clicking a tag should open editor with its current values`() {
        val testSubject = createTestSubject()

        testSubject.event(Event.OnTagClick("\$label2"))

        val editor = testSubject.state.value.editor
        assertThat(editor).isNotNull()
        assertThat(editor!!.name).isEqualTo("Work")
        assertThat(editor.isNewTag).isFalse()
        assertThat(editor.canReset).isFalse()
    }

    @Test
    fun `saving a renamed default tag should store the custom name only`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnTagClick("\$label1"))

        testSubject.event(Event.OnEditorNameChange("Urgent"))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(settingsManager.getConfig().tags).containsExactly(MessageTagSetting("\$label1", "Urgent", null))
        assertThat(testSubject.state.value.editor).isNull()
    }

    @Test
    fun `saving a default tag with unchanged values should not store anything`() {
        settingsManager.save(MessageTagSettings(listOf(MessageTagSetting("\$label1", "Urgent", 5))))
        val testSubject = createTestSubject()
        testSubject.event(Event.OnTagClick("\$label1"))

        testSubject.event(Event.OnEditorNameChange("Important"))
        testSubject.event(Event.OnEditorColorChange(DefaultMessageTags.tags[0].color))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(settingsManager.getConfig().tags).isEqualTo(emptyList())
    }

    @Test
    fun `resetting a customized default tag should remove its customization`() {
        settingsManager.save(MessageTagSettings(listOf(MessageTagSetting("\$label3", "Home", 9))))
        val testSubject = createTestSubject()
        testSubject.event(Event.OnTagClick("\$label3"))

        testSubject.event(Event.OnEditorResetClick)

        assertThat(settingsManager.getConfig().tags).isEqualTo(emptyList())
        assertThat(testSubject.state.value.tags[2].name).isEqualTo("Personal")
    }

    @Test
    fun `adding a new tag should store it`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)

        testSubject.event(Event.OnEditorKeywordChange(" Project-X "))
        testSubject.event(Event.OnEditorNameChange("Project X"))
        testSubject.event(Event.OnEditorColorChange(0xFF112233.toInt()))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(settingsManager.getConfig().tags)
            .containsExactly(MessageTagSetting("Project-X", "Project X", 0xFF112233.toInt()))
        assertThat(testSubject.state.value.tags.last().keyword).isEqualTo("Project-X")
    }

    @Test
    fun `adding a tag with empty keyword should show error and keep editor open`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)

        testSubject.event(Event.OnEditorSaveClick)

        assertThat(testSubject.state.value.editor!!.keywordError).isEqualTo(KeywordError.Empty)
        assertThat(settingsManager.getConfig().tags).isEqualTo(emptyList())
    }

    @Test
    fun `adding a tag with invalid keyword should show error`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)

        testSubject.event(Event.OnEditorKeywordChange("with space"))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(testSubject.state.value.editor!!.keywordError).isEqualTo(KeywordError.Invalid)
    }

    @Test
    fun `adding a tag with special characters should show error`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)

        testSubject.event(Event.OnEditorKeywordChange("a*b"))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(testSubject.state.value.editor!!.keywordError).isEqualTo(KeywordError.Invalid)
    }

    @Test
    fun `adding a tag with keyword of a default tag should show error`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)

        testSubject.event(Event.OnEditorKeywordChange("\$LABEL1"))
        testSubject.event(Event.OnEditorSaveClick)

        assertThat(testSubject.state.value.editor!!.keywordError).isEqualTo(KeywordError.AlreadyExists)
    }

    @Test
    fun `changing the keyword should clear the error`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnAddTagClick)
        testSubject.event(Event.OnEditorSaveClick)

        testSubject.event(Event.OnEditorKeywordChange("a"))

        assertThat(testSubject.state.value.editor!!.keywordError).isNull()
    }

    @Test
    fun `deleting a custom tag should remove it`() {
        settingsManager.save(MessageTagSettings(listOf(MessageTagSetting("project-x", "Project X", 7))))
        val testSubject = createTestSubject()
        testSubject.event(Event.OnTagClick("project-x"))

        testSubject.event(Event.OnEditorResetClick)

        assertThat(settingsManager.getConfig().tags).isEqualTo(emptyList())
        assertThat(testSubject.state.value.tags.size).isEqualTo(5)
    }

    @Test
    fun `dismissing the editor should not change settings`() {
        val testSubject = createTestSubject()
        testSubject.event(Event.OnTagClick("\$label1"))
        testSubject.event(Event.OnEditorNameChange("Changed"))

        testSubject.event(Event.OnEditorDismiss)

        assertThat(testSubject.state.value.editor).isNull()
        assertThat(settingsManager.getConfig().tags).isEqualTo(emptyList())
    }

    private fun createTestSubject() = MessageTagsViewModel(
        settingsManager = settingsManager,
        defaultTagNameProvider = { index -> defaultNames[index] },
    )

    private class FakeMessageTagSettingsPreferenceManager : MessageTagSettingsPreferenceManager {
        private val state = MutableStateFlow(MessageTagSettings())

        override fun save(config: MessageTagSettings) {
            state.value = config
        }

        override fun getConfig(): MessageTagSettings = state.value

        override fun getConfigFlow(): Flow<MessageTagSettings> = state
    }
}
