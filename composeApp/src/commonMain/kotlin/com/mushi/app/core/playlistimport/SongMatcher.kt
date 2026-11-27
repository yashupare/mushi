package com.mushi.app.core.playlistimport

import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack

private val BRACKETS = Regex("""\([^)]*\)|\[[^]]*]""")
private val FEATURING = Regex("""\s(feat\.?|ft\.|featuring)\s.*$""", RegexOption.IGNORE_CASE)
private val DASH_SUFFIX = Regex("""\s+[-–—]\s+.*$""")
private val SPACES = Regex("""\s+""")

/** Words that make a recording a different version from the plain song. */
private val VERSION_WORDS =
    setOf("remix", "live", "karaoke", "acoustic", "instrumental", "lofi", "slowed", "reverb", "cover", "sped")

/** Words on an album that mean its songs aren't the originals, whatever their titles say. */
private val COPY_ALBUM_WORDS = setOf("karaoke", "instrumental", "tribute", "lofi", "slowed")

private val ACCENTS: Map<Char, Char> = buildMap {
    listOf(
        "àáâãäåā" to 'a', "èéêëē" to 'e', "ìíîïī" to 'i', "òóôõöøō" to 'o',
        "ùúûüū" to 'u', "çć" to 'c', "ñń" to 'n', "ýÿ" to 'y'
    ).forEach { (from, to) -> from.forEach { put(it, to) } }
}

/** Lower case words only, so punctuation and accents don't stop two spellings matching. */
private fun normalize(text: String): String {
    val out = StringBuilder()
    for (c in text.lowercase().replace("&", " and ")) {
        when {
            c == '\'' || c == '’' -> Unit
            // Vowel signs (as in Hindi) are marks, not letters, and belong to the word
            c.isLetterOrDigit() || c.category == CharCategory.NON_SPACING_MARK ||
                c.category == CharCategory.COMBINING_SPACING_MARK -> out.append(ACCENTS[c] ?: c)

            else -> out.append(' ')
        }
    }
    return out.toString().replace(SPACES, " ").trim()
}

/** The title without "(From ...)", "- Remastered" or "feat. ..." additions. */
private fun plainTitle(title: String): String = title
    .replace(BRACKETS, " ")
    .replace(DASH_SUFFIX, "")
    .replace(FEATURING, "")
    .replace(SPACES, " ")
    .trim()
    .ifEmpty { title.trim() }

private fun String.containsWords(other: String): Boolean =
    other.isNotEmpty() && " $this ".contains(" $other ")

/** How many of the track's artists are named in [wanted]. */
private fun artistsNamed(wanted: String, track: MetadataTrack): Int {
    val name = normalize(wanted)
    return track.artists.count {
        val artist = normalize(it.name)
        name.containsWords(artist) || artist.containsWords(name)
    }
}

private fun artistMatches(wanted: String, track: MetadataTrack): Boolean = artistsNamed(wanted, track) > 0

/** 0 when the titles differ; a little higher when they agree including the additions. */
private fun titleScore(wanted: String, track: MetadataTrack): Int = when {
    normalize(wanted) == normalize(track.title) -> 11
    normalize(plainTitle(wanted)) == normalize(plainTitle(track.title)) -> 10
    else -> 0
}

private fun score(song: ImportedSong, track: MetadataTrack): Int {
    val artist = song.artist
    val title = if (artist != null) {
        val asWritten = if (artistMatches(artist, track)) titleScore(song.title, track) else 0
        val swapped =
            if (!song.orderKnown && artistMatches(song.title, track)) titleScore(artist, track) else 0
        maxOf(asWritten, swapped)
    } else {
        // A line without a separator: the title alone, or the title and artist run together
        val line = normalize(song.title)
        val name = normalize(plainTitle(track.title))
        val rest = when {
            line.startsWith("$name ") -> line.removePrefix("$name ")
            line.endsWith(" $name") -> line.removeSuffix(" $name")
            else -> null
        }
        maxOf(titleScore(song.title, track), if (rest != null && artistMatches(rest, track)) 10 else 0)
    }
    if (title == 0) return 0

    // Between recordings of the same song: the one on the same album, with more of the same
    // artists, and not a karaoke, remix or other version nobody asked for
    var total = title
    val album = track.album?.title
    if (song.album != null && album != null &&
        normalize(plainTitle(song.album)) == normalize(plainTitle(album))
    ) total += 4
    val artists = maxOf(
        artist?.let { artistsNamed(it, track) } ?: 0,
        if (!song.orderKnown) artistsNamed(song.title, track) else 0)
    total += 3 * (artists - 1).coerceAtLeast(0)

    val asked = normalize("${song.title} ${artist.orEmpty()} ${song.album.orEmpty()}").split(' ').toSet()
    fun unasked(text: String, words: Set<String>) = normalize(text).split(' ').any { it in words && it !in asked }
    val copy = unasked(track.title, VERSION_WORDS) || (album != null && unasked(album, COPY_ALBUM_WORDS))
    return if (copy) (total - 8).coerceAtLeast(1) else total
}

/** The search result that is this song, or null when none of them is. Earlier results win ties. */
internal fun bestMatch(song: ImportedSong, results: List<MetadataTrack>): MetadataTrack? =
    results.map { it to score(song, it) }
        .filter { it.second > 0 }
        .maxByOrNull { it.second }
        ?.first

/** What to type into the search for this song. */
internal fun searchQuery(song: ImportedSong): String {
    val artist = song.artist ?: return song.title
    if (!song.orderKnown) return "${song.title} $artist"
    return "${plainTitle(song.title)} ${artist.split(';', ',').first().trim()}"
}
