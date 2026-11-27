package com.mushi.app.core.playlistimport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SongListTest {

    @Test
    fun `reads plain lines, with or without numbers and artists`() {
        val songs = parseSongList("1. Kesariya - Arijit Singh\n\n  Pasoori \n2) Excuses – AP Dhillon")

        assertEquals(
            listOf(
                ImportedSong("Kesariya", "Arijit Singh", orderKnown = false),
                ImportedSong("Pasoori"),
                ImportedSong("Excuses", "AP Dhillon", orderKnown = false)),
            songs)
    }

    @Test
    fun `reads a CSV export by its column names`() {
        val csv = "﻿\"Track URI\",\"Track Name\",\"Artist Name(s)\",\"Album Name\"\r\n" +
            "\"x\",\"Hello, \"\"World\"\"\",\"A;B\",\"One\"\r\n" +
            "\"y\",\"Second\",\"C\",\"Two\nlines\"\r\n"

        assertEquals(
            listOf(
                ImportedSong("Hello, \"World\"", "A;B", album = "One"),
                ImportedSong("Second", "C", album = "Two\nlines")),
            parseSongList(csv))
    }

    @Test
    fun `reads a semicolon separated file`() {
        assertEquals(listOf(ImportedSong("Song", "Singer")), parseSongList("title;artist\nSong;Singer"))
    }

    @Test
    fun `reads an M3U playlist`() {
        val m3u = "#EXTM3U\n#EXTINF:210,Arijit Singh - Kesariya\n/music/01.mp3\n/music/Atif Aslam - Tera Hone Laga Hoon.mp3"

        assertEquals(
            listOf(
                ImportedSong("Kesariya", "Arijit Singh"),
                ImportedSong("Atif Aslam", "Tera Hone Laga Hoon", orderKnown = false)),
            parseSongList(m3u))
    }

    @Test
    fun `nothing in, nothing out`() {
        assertTrue(parseSongList("  \n\n").isEmpty())
    }
}
