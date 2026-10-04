package net.thunderbird.core.preference.display.visualSettings.message.tags

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import net.thunderbird.core.logging.testing.TestLogger
import net.thunderbird.core.preference.PreferenceChangeBroker
import net.thunderbird.core.preference.PreferenceChangeSubscriber
import net.thunderbird.core.preference.PreferenceScope
import net.thunderbird.core.preference.storage.InMemoryStorage
import net.thunderbird.core.preference.storage.StorageEditor

class DefaultMessageTagSettingsPreferenceManagerTest {
    private val key = MessageTagSettingKey.MessageTags.value

    @Test
    fun `getConfig without stored value should return empty settings`() {
        val testSubject = createTestSubject()

        assertThat(testSubject.getConfig().tags).isEmpty()
    }

    @Test
    fun `getConfig should load stored value`() {
        val testSubject = createTestSubject(storedValues = mapOf(key to "\$label1\tUrgent\t"))

        assertThat(testSubject.getConfig().tags).isEqualTo(listOf(MessageTagSetting("\$label1", "Urgent", null)))
    }

    @Test
    fun `save should write encoded value and update config`() {
        val editor = FakeStorageEditor()
        val testSubject = createTestSubject(editor = editor)
        val settings = MessageTagSettings(listOf(MessageTagSetting("\$label2", "Later", 7)))

        testSubject.save(settings)

        assertThat(editor.values[key]).isEqualTo("\$label2\tLater\t7")
        assertThat(editor.commitCount).isEqualTo(1)
        assertThat(testSubject.getConfig()).isEqualTo(settings)
    }

    @Test
    fun `save of empty settings should remove stored value`() {
        val editor = FakeStorageEditor()
        val testSubject = createTestSubject(editor = editor)
        testSubject.save(MessageTagSettings(listOf(MessageTagSetting("tag", "Name", null))))

        testSubject.save(MessageTagSettings())

        assertThat(editor.values.containsKey(key)).isEqualTo(false)
    }

    @Test
    fun `receive with matching scope should reload settings`() {
        val values = mutableMapOf<String, String>()
        val broker = FakePreferenceChangeBroker()
        val testSubject = createTestSubject(values = values, broker = broker)
        values[key] = "tag\tImported\t"

        broker.publish(PreferenceScope.DISPLAY_VISUAL_MESSAGE_TAGS)

        assertThat(testSubject.getConfig().tags).isEqualTo(listOf(MessageTagSetting("tag", "Imported", null)))
    }

    @Test
    fun `receive with unrelated scope should not reload settings`() {
        val values = mutableMapOf<String, String>()
        val broker = FakePreferenceChangeBroker()
        val testSubject = createTestSubject(values = values, broker = broker)
        values[key] = "tag\tImported\t"

        broker.publish(PreferenceScope.NETWORK)

        assertThat(testSubject.getConfig().tags).isEmpty()
    }

    private fun createTestSubject(
        storedValues: Map<String, String> = emptyMap(),
        values: MutableMap<String, String> = storedValues.toMutableMap(),
        editor: FakeStorageEditor = FakeStorageEditor(),
        broker: FakePreferenceChangeBroker = FakePreferenceChangeBroker(),
    ): DefaultMessageTagSettingsPreferenceManager {
        val logger = TestLogger()
        return DefaultMessageTagSettingsPreferenceManager(
            logger = logger,
            storage = InMemoryStorage(values, logger),
            storageEditor = editor,
            preferenceChangeBroker = broker,
        )
    }

    private class FakeStorageEditor : StorageEditor {
        val values = mutableMapOf<String, String?>()
        var commitCount = 0

        override fun putBoolean(key: String, value: Boolean) = apply { values[key] = value.toString() }
        override fun putInt(key: String, value: Int) = apply { values[key] = value.toString() }
        override fun putLong(key: String, value: Long) = apply { values[key] = value.toString() }
        override fun putString(key: String, value: String?) = apply {
            if (value == null) values.remove(key) else values[key] = value
        }

        override fun remove(key: String) = apply { values.remove(key) }
        override fun commit(): Boolean {
            commitCount++
            return true
        }
    }

    private class FakePreferenceChangeBroker : PreferenceChangeBroker {
        private val subscribers = mutableSetOf<PreferenceChangeSubscriber>()

        override fun subscribe(subscriber: PreferenceChangeSubscriber) {
            subscribers.add(subscriber)
        }

        override fun unsubscribe(subscriber: PreferenceChangeSubscriber) {
            subscribers.remove(subscriber)
        }

        fun publish(scope: PreferenceScope) {
            subscribers.forEach { it.receive(scope) }
        }
    }
}
