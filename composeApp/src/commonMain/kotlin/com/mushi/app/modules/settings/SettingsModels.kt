package com.mushi.app.modules.settings

import org.spotube.plugin_interfaces.plugin_apis.audio.AudioFormat
import org.spotube.plugin_interfaces.plugin_apis.audio.AudioQuality
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.spotube.millennium.theme.MillenniumPalette
import androidx.compose.ui.text.intl.Locale

@Serializable
enum class Theme {
    LIGHT, DARK, SYSTEM
}

/** The country for the device's region code, such as "IN"; the US when it names none. */
internal fun countryFor(region: String): CountryCode =
    CountryCode.entries.firstOrNull { it.code.equals(region, ignoreCase = true) } ?: CountryCode.US

@Serializable
data class UserSettings(
    // Language and Region
    // Stored under a new name: the old "language" field was persisted as EN for everyone while
    // it had no effect, so reading it would force English on existing users.
    @SerialName("appLanguage")
    val language: SupportedLanguages = SupportedLanguages.SYSTEM,
    val country: CountryCode = countryFor(Locale.current.region),

    // Appearance
    val theme: Theme = Theme.DARK,
    val palette: MillenniumPalette = MillenniumPalette.Blue,
    /** The Mushi colour palette, chosen in Settings → Appearance. */
    val colorPalette: com.mushi.app.core.ui.theming.MushiPalette = com.mushi.app.core.ui.theming.MushiPalette.RoseNight,

    // Playback
    val streamingMusicFormat: AudioFormat = defaultStreamingMusicFormat(),
    val streamingMusicQuality: AudioQuality = AudioQuality.Lossy(bitrate = 256_000),
    val enableMusicCaching: Boolean = true,
    val cacheFolder: String? = null,
    val cacheSizeLimitMB: Long = -1L,
    val enableEndlessPlayback: Boolean = true,
    val playbackProxyServerPort: Int = 14769,

    // Remote Control (LAN)
    val allowRemoteControl: Boolean = false,
    val allowedRemoteDevices: List<String> = emptyList(),
    val remoteControlDeviceName: String = "",
    val remoteControlDeviceId: String = "",

    // Group Jam
    val jamParticipantName: String = "",
    /** Group Jam room server (mushi-jam-server); users can point this at their own deployment. */
    val jamServerUrl: String = DEFAULT_JAM_SERVER_URL,
    val lastJamCode: String = "",
    /** Random per-install secret behind this device's jam identities (see `jamParticipantKey`). */
    val jamDeviceKey: String = "",

    // Downloads
    val overloadedDownloadFolder: String? = null, // When null, uses default music folder
    val localMediaFolders: List<String> = emptyList(),
    val downloadMusicFormat: AudioFormat = AudioFormat(
        codec = "aac",
        container = "mp4",
        qualities = listOf(
            AudioQuality.Lossy(bitrate = 44_000),
            AudioQuality.Lossy(bitrate = 96_000),
            AudioQuality.Lossy(bitrate = 128_000),
            AudioQuality.Lossy(bitrate = 256_000))
    ),
    val downloadMusicQuality: AudioQuality = AudioQuality.Lossy(bitrate = 256_000),

    // Desktop
    val minimizeToTray: Boolean = false,
    val discordRichPresence: Boolean = true,

    // Updates
    val autoCheckForUpdates: Boolean = true,
    val ignoredAppUpdateVersion: String? = null,
    val ignoredPluginUpdateVersions: Map<String, String> = emptyMap(),

    // Support
    val supportRemindersEnabled: Boolean = true,
    val lastSupportReminderEpochMs: Long = 0L)

/** The public Group Jam room server. */
/** Mushi's own jam server: the mushi-jam Cloudflare Worker. */
const val DEFAULT_JAM_SERVER_URL = "https://mushi-jam.uddash.workers.dev"

/** Whether [url] can be a room server address (http(s), or ws(s) for self-hosters). */
internal fun isJamServerUrl(url: String): Boolean =
    Regex("^(https?|wss?)://[^\\s/?#]+(/\\S*)?$", RegexOption.IGNORE_CASE).matches(url.trim())

/** opus/webm on every platform: all of their players (ExoPlayer, VLC) can play it. */
internal fun defaultStreamingMusicFormat(): AudioFormat = AudioFormat(
    codec = "opus",
    container = "webm",
    qualities = listOf(
        AudioQuality.Lossy(bitrate = 44_000),
        AudioQuality.Lossy(bitrate = 96_000),
        AudioQuality.Lossy(bitrate = 128_000),
        AudioQuality.Lossy(bitrate = 256_000)))
