package com.mushi.app.core.audioeffects

/** The centre of each equalizer band, in Hz, and the upper edge of the range it covers. */
internal val EQ_BAND_HZ = listOf(60, 230, 910, 3_600, 14_000)
internal val EQ_BAND_EDGES_HZ = listOf(120f, 460f, 1_800f, 7_200f, 20_000f)

/** How far a band can be raised or lowered, in dB. */
internal const val EQ_MAX_GAIN = 8

internal val EQ_FLAT = listOf(0, 0, 0, 0, 0)

enum class EqualizerPreset(val gains: List<Int>) {
    Flat(EQ_FLAT),
    BassBoost(listOf(6, 4, 0, 0, 0)),
    Vocal(listOf(-2, 0, 3, 4, 1)),
    Treble(listOf(0, 0, 0, 4, 6)),
    Lively(listOf(4, 1, -1, 2, 4))
}

/** The preset these gains are, or null for the listener's own mix. */
internal fun presetFor(gains: List<Int>): EqualizerPreset? =
    EqualizerPreset.entries.firstOrNull { it.gains == gains.normalizedGains() }

/** Exactly one gain per band, each within range, whatever was stored. */
internal fun List<Int>.normalizedGains(): List<Int> =
    EQ_BAND_HZ.indices.map { getOrElse(it) { 0 }.coerceIn(-EQ_MAX_GAIN, EQ_MAX_GAIN) }

/** Turns the whole signal down by the largest boost, so raised bands don't distort. */
internal fun preampDb(gains: List<Int>): Float = (0 - (gains.maxOrNull() ?: 0).coerceAtLeast(0)).toFloat()
