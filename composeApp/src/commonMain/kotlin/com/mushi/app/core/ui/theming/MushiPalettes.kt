package com.mushi.app.core.ui.theming

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import org.spotube.millennium.theme.MillenniumColors
import org.spotube.millennium.theme.MillenniumDarkColors
import org.spotube.millennium.theme.MillenniumLightColors

/**
 * The colour palettes people can choose in Settings → Appearance.
 *
 * Every palette has the same structure: a near-black base, three more surface levels for depth,
 * text and muted text that meet 4.5:1 contrast on every surface, one accent for actions and
 * active states, and a signature gradient reserved for brand moments (logo, launch, hero art).
 */
@Serializable
enum class MushiPalette {
    RoseNight,
    Aurora,
    Ember,
    Tide,
}

@Immutable
data class MushiPaletteSpec(
    val dark: MillenniumColors,
    val light: MillenniumColors,
    /** Signature gradient, start to end. Brand moments only, never behind running text. */
    val gradient: List<Color>,
)

/** The palette's colours plus the brand gradient, for components outside Millennium. */
@Immutable
data class MushiBrand(
    val palette: MushiPalette,
    val gradient: List<Color>,
    val accent: Color,
)

val LocalMushiBrand = staticCompositionLocalOf {
    val spec = MushiPalette.RoseNight.spec()
    MushiBrand(MushiPalette.RoseNight, spec.gradient, spec.dark.accent)
}

private val DarkScrim = Color(0x99000000)
private val LightScrim = Color(0x66000000)
private val DarkDanger = Color(0xFFFF5A52)
private val LightDanger = Color(0xFFD92D20)

private fun darkColors(
    base: Long, surface: Long, raised: Long, inset: Long, border: Long,
    text: Long, muted: Long, accent: Long, onAccent: Long,
): MillenniumColors = MillenniumDarkColors.copy(
    background = Color(base),
    surface = Color(surface),
    raised = Color(raised),
    inset = Color(inset),
    text = Color(text),
    muted = Color(muted),
    border = Color(border),
    // Flat look: no top highlight, and the accent is a solid fill rather than a gradient.
    highlight = Color.Transparent,
    accent = Color(accent),
    accentTop = Color(accent),
    accentBottom = Color(accent),
    onAccent = Color(onAccent),
    danger = DarkDanger,
    scrim = DarkScrim)

private fun lightColors(
    base: Long, surface: Long, raised: Long, inset: Long, border: Long,
    text: Long, muted: Long, accent: Long, onAccent: Long,
): MillenniumColors = MillenniumLightColors.copy(
    background = Color(base),
    surface = Color(surface),
    raised = Color(raised),
    inset = Color(inset),
    text = Color(text),
    muted = Color(muted),
    border = Color(border),
    highlight = Color.Transparent,
    accent = Color(accent),
    accentTop = Color(accent),
    accentBottom = Color(accent),
    onAccent = Color(onAccent),
    danger = LightDanger,
    scrim = LightScrim)

private const val White = 0xFFFFFFFF

private val RoseNightSpec = MushiPaletteSpec(
    dark = darkColors(
        base = 0xFF0A0A0C, surface = 0xFF141417, raised = 0xFF1E1E22, inset = 0xFF070708, border = 0xFF2A2A30,
        // White on this rose is only 3.2:1, so button labels are a deep rose-black (6:1).
        text = 0xFFF5F5F7, muted = 0xFF9A9AA2, accent = 0xFFFF4D6D, onAccent = 0xFF1F0610),
    light = lightColors(
        base = 0xFFFFFFFF, surface = 0xFFF4F4F6, raised = 0xFFECECF0, inset = 0xFFF4F4F6, border = 0xFFE2E2E8,
        text = 0xFF111114, muted = 0xFF67676F, accent = 0xFFE11D48, onAccent = White),
    gradient = listOf(Color(0xFFFF8A5B), Color(0xFFFF4D6D), Color(0xFFD6336C)))

private val AuroraSpec = MushiPaletteSpec(
    dark = darkColors(
        base = 0xFF07070D, surface = 0xFF11111A, raised = 0xFF1A1A26, inset = 0xFF05050A, border = 0xFF252534,
        // Violet is light enough that white labels on it would fall below 4.5:1, so they're dark.
        text = 0xFFF4F3FF, muted = 0xFF9C9AB8, accent = 0xFFA78BFA, onAccent = 0xFF120A2A),
    light = lightColors(
        base = 0xFFFCFCFF, surface = 0xFFF3F2FA, raised = 0xFFEAE8F5, inset = 0xFFF3F2FA, border = 0xFFE1DFEE,
        text = 0xFF14131F, muted = 0xFF67657E, accent = 0xFF7C3AED, onAccent = White),
    gradient = listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899)))

private val EmberSpec = MushiPaletteSpec(
    dark = darkColors(
        base = 0xFF0D0A09, surface = 0xFF171210, raised = 0xFF221A17, inset = 0xFF090706, border = 0xFF2E2420,
        // White on coral is 2.8:1; a warm near-black label reads at 6.7:1.
        text = 0xFFFBF5F2, muted = 0xFFA8988F, accent = 0xFFFF6B3D, onAccent = 0xFF1F0A03),
    light = lightColors(
        base = 0xFFFFFCFA, surface = 0xFFF7F1EE, raised = 0xFFF0E7E2, inset = 0xFFF7F1EE, border = 0xFFEADFD9,
        text = 0xFF1C1512, muted = 0xFF74655E, accent = 0xFFC2410C, onAccent = White),
    gradient = listOf(Color(0xFFFFB547), Color(0xFFFF6B3D), Color(0xFFE8384F)))

private val TideSpec = MushiPaletteSpec(
    dark = darkColors(
        base = 0xFF050B0F, surface = 0xFF0D161C, raised = 0xFF142129, inset = 0xFF03080B, border = 0xFF1C2D37,
        // Teal is bright, so labels on it are dark for contrast.
        text = 0xFFEEF8FA, muted = 0xFF8FA6B0, accent = 0xFF2DD4BF, onAccent = 0xFF03201C),
    light = lightColors(
        base = 0xFFFAFDFD, surface = 0xFFEFF6F7, raised = 0xFFE4EFF1, inset = 0xFFEFF6F7, border = 0xFFD9E7EA,
        text = 0xFF0D1A1F, muted = 0xFF566A72, accent = 0xFF0F766E, onAccent = White),
    gradient = listOf(Color(0xFF22D3EE), Color(0xFF2DD4BF), Color(0xFFA3E635)))

fun MushiPalette.spec(): MushiPaletteSpec = when (this) {
    MushiPalette.RoseNight -> RoseNightSpec
    MushiPalette.Aurora -> AuroraSpec
    MushiPalette.Ember -> EmberSpec
    MushiPalette.Tide -> TideSpec
}
