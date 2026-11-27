package com.mushi.app.core.backup

import com.mushi.app.core.history.HistoryItem
import com.mushi.app.core.history.MAX_HISTORY_ITEMS
import com.mushi.app.core.moments.MomentLibrary
import com.mushi.app.modules.settings.UserSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** What a backup file holds. Sign-ins and this device's secrets are never part of it. */
@Serializable
data class BackupFile(
    val app: String,
    val version: Int,
    val createdAtMs: Long,
    val settings: UserSettings,
    val history: List<HistoryItem> = emptyList(),
    val moments: MomentLibrary = MomentLibrary())

private const val BACKUP_APP = "mushi"
private const val BACKUP_VERSION = 1

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun encodeBackup(
    settings: UserSettings,
    history: List<HistoryItem>,
    moments: MomentLibrary,
    nowMs: Long): String = json.encodeToString(
    BackupFile(BACKUP_APP, BACKUP_VERSION, nowMs, settings.portable(UserSettings()), history, moments))

/** The backup in [text], or null if it isn't a Mushi backup this version can read. */
internal fun decodeBackup(text: String): BackupFile? =
    runCatching { json.decodeFromString<BackupFile>(text) }.getOrNull()
        ?.takeIf { it.app == BACKUP_APP && it.version <= BACKUP_VERSION }

/**
 * These settings with everything that belongs to one device taken from [device]: secrets and
 * identities, paired devices, folders and ports.
 */
internal fun UserSettings.portable(device: UserSettings): UserSettings = copy(
    jamDeviceKey = device.jamDeviceKey,
    lastJamCode = device.lastJamCode,
    allowRemoteControl = device.allowRemoteControl,
    allowedRemoteDevices = device.allowedRemoteDevices,
    remoteControlDeviceName = device.remoteControlDeviceName,
    remoteControlDeviceId = device.remoteControlDeviceId,
    jamServerUrl = device.jamServerUrl,
    cacheFolder = device.cacheFolder,
    overloadedDownloadFolder = device.overloadedDownloadFolder,
    localMediaFolders = device.localMediaFolders,
    playbackProxyServerPort = device.playbackProxyServerPort)

/** Restoring adds to what's here and never removes it. */
internal fun mergeHistory(current: List<HistoryItem>, restored: List<HistoryItem>): List<HistoryItem> =
    (current + restored)
        .groupBy { it.key }
        .map { (_, same) ->
            val latest = same.maxBy { it.lastPlayedAtMs }
            latest.copy(playCount = same.maxOf { it.playCount })
        }
        .sortedByDescending { it.lastPlayedAtMs }
        .take(MAX_HISTORY_ITEMS)

internal fun mergeMoments(current: MomentLibrary, restored: MomentLibrary): MomentLibrary {
    val moments = (current.moments + restored.moments).distinctBy { it.id }
    val ids = moments.mapTo(mutableSetOf()) { it.id }
    val constellations = (current.constellations + restored.constellations)
        .distinctBy { it.id }
        .map { saved -> saved.copy(momentIds = saved.momentIds.filter { it in ids }) }
        .filter { it.momentIds.size > 1 }
    return MomentLibrary(moments, constellations)
}
