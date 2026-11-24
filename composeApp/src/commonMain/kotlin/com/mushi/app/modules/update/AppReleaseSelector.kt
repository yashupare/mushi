package com.mushi.app.modules.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.swiftzer.semver.SemVer

@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val body: String? = null,
    val draft: Boolean = false)

/**
 * Picks the release to offer from GitHub's release list using full SemVer 2.0.0 precedence
 * (pre-release identifiers compared field by field, build metadata ignored).
 *
 * Stable installs are only offered stable releases. Pre-release installs (e.g. `6.0.0-beta.1`)
 * are also offered newer pre-releases, which `releases/latest` would never return.
 */
internal object AppReleaseSelector {
    /** Parses a release tag such as `v6.0.0`, `V6.0.0-beta.2` or `6.0.0+build.7`; null if not SemVer. */
    fun parseTag(tag: String): SemVer? {
        val trimmed = tag.trim()
        val withoutPrefix = if (trimmed.startsWith('v') || trimmed.startsWith('V')) trimmed.drop(1) else trimmed
        return SemVer.parseOrNull(withoutPrefix)
    }

    fun select(currentVersion: SemVer, releases: List<GitHubRelease>, ignoredTag: String?): GitHubRelease? {
        val ignoredVersion = ignoredTag?.let(::parseTag)
        val includePreReleases = currentVersion.preRelease != null

        return releases.asSequence()
            .filterNot { it.draft }
            .mapNotNull { release -> parseTag(release.tagName)?.let { release to it } }
            .filter { (_, version) -> includePreReleases || version.preRelease == null }
            .maxWithOrNull(compareBy { it.second })
            ?.takeIf { (_, version) -> version > currentVersion }
            ?.takeIf { (_, version) -> ignoredVersion == null || version.compareTo(ignoredVersion) != 0 }
            ?.first
    }
}
