package com.mushi.app.core.playlistimport

/**
 * One song read from a pasted list or a file.
 *
 * @param orderKnown false when [title] and [artist] came from a line like "A - B", where either
 *   side may be the artist.
 */
data class ImportedSong(
    val title: String,
    val artist: String? = null,
    val orderKnown: Boolean = true,
    val album: String? = null)

private val TITLE_HEADERS = setOf("track name", "title", "name", "song", "song name", "track", "track title")
private val ARTIST_HEADERS = setOf("artist name(s)", "artist name", "artist names", "artist", "artists")
private val ALBUM_HEADERS = setOf("album name", "album", "album title")

private val LINE_NUMBER = Regex("""^\d{1,4}[.)]\s+""")
private val SEPARATOR = Regex("""\s+[-–—]\s+|\t+""")

/**
 * Reads the songs out of a CSV export (with a header naming the title and artist columns), an
 * M3U playlist, or plain lines of "Title - Artist".
 */
internal fun parseSongList(text: String): List<ImportedSong> {
    val content = text.removePrefix("﻿")
    val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return emptyList()
    if (lines.first().startsWith("#EXTM3U") || lines.any { it.startsWith("#EXTINF") }) {
        return parseM3u(lines)
    }
    return parseCsv(content) ?: lines.mapNotNull(::parseLine)
}

private fun parseLine(line: String): ImportedSong? {
    val song = line.replaceFirst(LINE_NUMBER, "").trim()
    if (song.isEmpty()) return null
    val parts = song.split(SEPARATOR, limit = 2)
    return if (parts.size == 2) ImportedSong(parts[0].trim(), parts[1].trim(), orderKnown = false)
    else ImportedSong(song)
}

/** "#EXTINF:seconds,Artist - Title" followed by the file; bare file names are used otherwise. */
private fun parseM3u(lines: List<String>): List<ImportedSong> {
    val songs = mutableListOf<ImportedSong>()
    var described = false
    for (line in lines) {
        if (line.startsWith("#EXTINF")) {
            val name = line.substringAfter(',', "").trim()
            if (name.isNotEmpty()) {
                val parts = name.split(SEPARATOR, limit = 2)
                songs += if (parts.size == 2) ImportedSong(parts[1].trim(), parts[0].trim())
                else ImportedSong(name)
                described = true
            }
        } else if (!line.startsWith("#")) {
            if (!described) {
                val name = line.substringAfterLast('/').substringAfterLast('\\').substringBeforeLast('.')
                parseLine(name)?.let { songs += it }
            }
            described = false
        }
    }
    return songs
}

/** Null when the first line isn't a header naming both a title and an artist column. */
private fun parseCsv(content: String): List<ImportedSong>? {
    val header = content.lineSequence().firstOrNull { it.isNotBlank() } ?: return null
    for (delimiter in listOf(',', ';', '\t')) {
        val columns = splitRows(header, delimiter).firstOrNull()?.map { it.trim().lowercase() } ?: continue
        val title = columns.indexOfFirst { it in TITLE_HEADERS }
        val artist = columns.indexOfFirst { it in ARTIST_HEADERS }
        if (title < 0 || artist < 0) continue
        val album = columns.indexOfFirst { it in ALBUM_HEADERS }

        return splitRows(content, delimiter)
            .filter { row -> row.any { it.isNotBlank() } }
            .drop(1)
            .mapNotNull { row ->
                val name = row.getOrNull(title)?.trim().orEmpty()
                if (name.isEmpty()) null
                else ImportedSong(
                    name,
                    row.getOrNull(artist)?.trim()?.ifEmpty { null },
                    album = row.getOrNull(album)?.trim()?.ifEmpty { null })
            }
    }
    return null
}

/** Splits delimited text into rows of fields, honouring quotes (which may hold line breaks). */
private fun splitRows(text: String, delimiter: Char): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var i = 0

    fun endField() {
        row.add(field.toString())
        field.clear()
    }

    fun endRow() {
        endField()
        rows.add(row)
        row = mutableListOf()
    }

    while (i < text.length) {
        val c = text[i]
        when {
            quoted && c == '"' && text.getOrNull(i + 1) == '"' -> {
                field.append('"')
                i++
            }

            c == '"' && (quoted || field.isBlank()) -> quoted = !quoted
            !quoted && c == delimiter -> endField()
            !quoted && c == '\n' -> endRow()
            c != '\r' -> field.append(c)
        }
        i++
    }
    if (field.isNotEmpty() || row.isNotEmpty()) endRow()
    return rows
}
