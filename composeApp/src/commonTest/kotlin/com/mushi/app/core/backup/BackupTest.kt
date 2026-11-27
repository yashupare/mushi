package com.mushi.app.core.backup

import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.HistoryItem
import com.mushi.app.core.moments.Constellation
import com.mushi.app.core.moments.Moment
import com.mushi.app.core.moments.MomentLibrary
import com.mushi.app.modules.settings.UserSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BackupTest {

    private fun song(url: String) = QueueEntry.LocalTrack(
        name = url, artists = emptyList(), duration = 200_000, album = null, url = url)

    private fun played(url: String, atMs: Long, count: Int = 1) =
        HistoryItem(song(url), listOf(atMs), count)

    private fun moment(id: String) = Moment(id, song(id), 1_000, 0, 0.5f, 0.5f)

    @Test
    fun `a backup reads back, without this device's secrets`() {
        val settings = UserSettings(jamDeviceKey = "secret-key", jamParticipantName = "Sam")
        val library = MomentLibrary(listOf(moment("a")))

        val text = encodeBackup(settings, listOf(played("a", 1)), library, nowMs = 5)

        assertFalse("secret-key" in text)
        val backup = assertNotNull(decodeBackup(text))
        assertEquals("Sam", backup.settings.jamParticipantName)
        assertEquals(library, backup.moments)
        assertEquals(1, backup.history.size)
    }

    @Test
    fun `other files are not backups`() {
        assertNull(decodeBackup("not json"))
        assertNull(decodeBackup("""{"app":"other","version":1,"createdAtMs":0,"settings":{}}"""))
    }

    @Test
    fun `restoring keeps this device's own settings`() {
        val restored = UserSettings(jamDeviceKey = "", jamParticipantName = "Sam")
        val device = UserSettings(jamDeviceKey = "mine", cacheFolder = "/here")

        val result = restored.portable(device)

        assertEquals("mine", result.jamDeviceKey)
        assertEquals("/here", result.cacheFolder)
        assertEquals("Sam", result.jamParticipantName)
    }

    @Test
    fun `restoring adds without removing`() {
        val history = mergeHistory(
            current = listOf(played("a", 10, count = 1), played("b", 5)),
            restored = listOf(played("a", 2, count = 4), played("c", 20)))

        assertEquals(listOf("c", "a", "b"), history.map { it.key })
        assertEquals(4, history[1].playCount)

        val moments = mergeMoments(
            MomentLibrary(listOf(moment("a"))),
            MomentLibrary(
                listOf(moment("a"), moment("b")),
                listOf(Constellation("1", "mix", listOf("a", "b")), Constellation("2", "gone", listOf("a", "x")))))

        assertEquals(listOf("a", "b"), moments.moments.map { it.id })
        assertEquals(listOf("mix"), moments.constellations.map { it.name })
    }
}
