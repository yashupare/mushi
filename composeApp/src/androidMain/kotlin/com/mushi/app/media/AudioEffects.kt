package com.mushi.app.media

import android.media.audiofx.DynamicsProcessing
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.mushi.app.core.audioeffects.EQ_BAND_EDGES_HZ
import com.mushi.app.core.audioeffects.EQ_FLAT
import com.mushi.app.core.audioeffects.normalizedGains
import com.mushi.app.core.audioeffects.preampDb
import com.mushi.app.core.audioplayer.AudioPlayer
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.settings.UserSettingsSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "AudioEffects"

/** What the effects need from the settings. */
private data class Wanted(val equalizer: Boolean, val gains: List<Int>, val evening: Boolean)

private fun UserSettings.wanted() = Wanted(equalizerEnabled, equalizerGains.normalizedGains(), volumeEvening)

/**
 * The equalizer and volume evening, applied to the player's audio with Android's own
 * DynamicsProcessing effect. With both off there is no effect at all, so playback is untouched.
 */
@OptIn(UnstableApi::class)
class AudioEffects(
    private val player: AudioPlayer,
    private val settings: UserSettingsSource) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val session = MutableStateFlow(0)
    private var effect: DynamicsProcessing? = null
    private var effectSession = 0

    /** Must run on the main thread, which the player belongs to. */
    fun start() {
        session.value = player.player.audioSessionId
        player.player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                session.value = audioSessionId
            }
        })
        scope.launch {
            combine(session, settings.userSettings.map { it.wanted() }.distinctUntilChanged()) { id, wanted ->
                id to wanted
            }.collect { (id, wanted) -> apply(id, wanted) }
        }
    }

    private fun apply(sessionId: Int, wanted: Wanted) {
        try {
            if (sessionId == 0 || (!wanted.equalizer && !wanted.evening)) {
                release()
                return
            }
            val current = effect?.takeIf { effectSession == sessionId } ?: run {
                release()
                create(sessionId).also {
                    effect = it
                    effectSession = sessionId
                }
            }
            configure(current, wanted)
            current.enabled = true
        } catch (e: Exception) {
            // Some devices don't offer the effect; music keeps playing without it
            Log.w(TAG, "Audio effects aren't available", e)
            release()
        }
    }

    private fun create(sessionId: Int): DynamicsProcessing {
        val config = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            2,
            true, EQ_BAND_EDGES_HZ.size,
            true, 1,
            false, 0,
            true).build()
        return DynamicsProcessing(0, sessionId, config)
    }

    private fun configure(effect: DynamicsProcessing, wanted: Wanted) {
        val gains = if (wanted.equalizer) wanted.gains else EQ_FLAT
        effect.setInputGainAllChannelsTo(preampDb(gains))

        val equalizer = DynamicsProcessing.Eq(true, true, EQ_BAND_EDGES_HZ.size)
        EQ_BAND_EDGES_HZ.forEachIndexed { i, edge ->
            equalizer.setBand(i, DynamicsProcessing.EqBand(true, edge, gains[i].toFloat()))
        }
        effect.setPreEqAllChannelsTo(equalizer)

        // Volume evening: quiet passages are lifted and loud ones held back
        val compressor = DynamicsProcessing.Mbc(true, true, 1)
        compressor.setBand(
            0,
            DynamicsProcessing.MbcBand(
                wanted.evening,
                20_000f,
                /* attack ms */ 15f,
                /* release ms */ 300f,
                /* ratio */ 3f,
                /* threshold dB */ -28f,
                /* knee dB */ 6f,
                /* noise gate dB */ -90f,
                /* expander ratio */ 1f,
                /* pre gain */ 0f,
                /* make-up gain */ if (wanted.evening) 8f else 0f))
        effect.setMbcAllChannelsTo(compressor)

        // Catches any peak the gains above would push into distortion
        effect.setLimiterAllChannelsTo(
            DynamicsProcessing.Limiter(true, true, 0, 1f, 60f, 10f, -1f, 0f))
    }

    private fun release() {
        effect?.runCatching { release() }
        effect = null
        effectSession = 0
    }
}
