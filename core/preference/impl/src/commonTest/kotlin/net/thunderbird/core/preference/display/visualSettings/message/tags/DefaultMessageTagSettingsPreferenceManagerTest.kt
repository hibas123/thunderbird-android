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
import net.thunderbird.core.preference.storage.Storage
import net.thunderbird.core.preference.storage.StorageEditor
import net.thunderbird.core.preference.storage.StoragePersister
import net.thunderbird.core.preference.storage.StorageUpdater

class DefaultMessageTagSettingsPreferenceManagerTest {
    private val key = MessageTagSettingKey.MessageTags.value
    private val persister = FakeStoragePersister()
    private val broker = FakePreferenceChangeBroker()

    @Test
    fun `getConfig without stored value should return empty settings`() {
        val testSubject = createTestSubject()

        assertThat(testSubject.getConfig().tags).isEmpty()
    }

    @Test
    fun `getConfig should load stored value`() {
        persister.values[key] = "\$label1\tUrgent\t"

        val testSubject = createTestSubject()

        assertThat(testSubject.getConfig().tags).isEqualTo(listOf(MessageTagSetting("\$label1", "Urgent", null)))
    }

    @Test
    fun `save should write encoded value and update config`() {
        val testSubject = createTestSubject()
        val settings = MessageTagSettings(listOf(MessageTagSetting("\$label2", "Later", 7)))

        testSubject.save(settings)

        assertThat(persister.values[key]).isEqualTo("\$label2\tLater\t7")
        assertThat(testSubject.getConfig()).isEqualTo(settings)
    }

    @Test
    fun `save of empty settings should remove stored value`() {
        val testSubject = createTestSubject()
        testSubject.save(MessageTagSettings(listOf(MessageTagSetting("tag", "Name", null))))

        testSubject.save(MessageTagSettings())

        assertThat(persister.values.containsKey(key)).isEqualTo(false)
    }

    @Test
    fun `saved settings should survive a change notification for all preferences`() {
        val testSubject = createTestSubject()
        val settings = MessageTagSettings(listOf(MessageTagSetting("\$label1", "Urgent", 5)))
        testSubject.save(settings)

        broker.publish(PreferenceScope.ALL)

        assertThat(testSubject.getConfig()).isEqualTo(settings)
    }

    @Test
    fun `receive with matching scope should reload settings`() {
        val testSubject = createTestSubject()
        persister.values[key] = "tag\tImported\t"

        broker.publish(PreferenceScope.DISPLAY_VISUAL_MESSAGE_TAGS)

        assertThat(testSubject.getConfig().tags).isEqualTo(listOf(MessageTagSetting("tag", "Imported", null)))
    }

    @Test
    fun `receive with unrelated scope should not reload settings`() {
        val testSubject = createTestSubject()
        persister.values[key] = "tag\tImported\t"

        broker.publish(PreferenceScope.NETWORK)

        assertThat(testSubject.getConfig().tags).isEmpty()
    }

    private fun createTestSubject() = DefaultMessageTagSettingsPreferenceManager(
        logger = TestLogger(),
        storagePersister = persister,
        storageEditor = persister.createStorageEditor { },
        preferenceChangeBroker = broker,
    )

    /**
     * Like the real persister, [loadValues] returns a snapshot that doesn't reflect later changes.
     */
    private class FakeStoragePersister : StoragePersister {
        val values = mutableMapOf<String, String>()

        override fun loadValues(): Storage = InMemoryStorage(values.toMap(), TestLogger())

        override fun createStorageEditor(storageUpdater: StorageUpdater): StorageEditor {
            return object : StorageEditor {
                private val changes = mutableMapOf<String, String?>()

                override fun putBoolean(key: String, value: Boolean) = apply { changes[key] = value.toString() }
                override fun putInt(key: String, value: Int) = apply { changes[key] = value.toString() }
                override fun putLong(key: String, value: Long) = apply { changes[key] = value.toString() }
                override fun putString(key: String, value: String?) = apply { changes[key] = value }
                override fun remove(key: String) = apply { changes[key] = null }

                override fun commit(): Boolean {
                    for ((key, value) in changes) {
                        if (value == null) values.remove(key) else values[key] = value
                    }
                    changes.clear()
                    return true
                }
            }
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
