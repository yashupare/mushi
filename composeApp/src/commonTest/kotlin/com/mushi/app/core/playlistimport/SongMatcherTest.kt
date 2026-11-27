package com.mushi.app.core.playlistimport

import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbum
import org.spotube.plugin_interfaces.plugin_apis.metadata.album.MetadataAlbumType
import org.spotube.plugin_interfaces.plugin_apis.metadata.artist.MetadataArtist
import org.spotube.plugin_interfaces.plugin_apis.metadata.track.MetadataTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal fun track(
    title: String,
    vararg artists: String,
    id: String = title,
    albumName: String? = null
) = MetadataTrack(
    id = id,
    title = title,
    durationMs = 200_000,
    trackNumber = null,
    discNumber = null,
    artists = artists.map { MetadataArtist.Basic(id = it, name = it, thumbnails = emptyList(), externalUri = null) },
    album = albumName?.let {
        MetadataAlbum.Detailed(
            releaseDate = null,
            genres = emptyList(),
            trackCount = 1,
            id = it,
            title = it,
            description = null,
            thumbnails = emptyList(),
            albumType = MetadataAlbumType.Album,
            artists = emptyList(),
            externalUri = null)
    },
    thumbnails = null,
    explicit = null,
    popularity = null,
    isrcCode = null,
    externalUri = null)

class SongMatcherTest {

    @Test
    fun `finds a song whichever side the artist is on`() {
        val results = listOf(track("Kesariya (From \"Brahmastra\")", "Pritam", "Arijit Singh"))

        assertEquals(results[0], bestMatch(ImportedSong("Kesariya", "Arijit Singh", orderKnown = false), results))
        assertEquals(results[0], bestMatch(ImportedSong("Arijit Singh", "Kesariya", orderKnown = false), results))
        assertEquals(results[0], bestMatch(ImportedSong("kesariya arijit singh"), results))
    }

    @Test
    fun `does not take another artist's song of the same name`() {
        assertNull(bestMatch(ImportedSong("Hello", "Adele"), listOf(track("Hello", "Lionel Richie"))))
    }

    @Test
    fun `does not take a different song by the artist`() {
        assertNull(bestMatch(ImportedSong("Hello", "Adele"), listOf(track("Skyfall", "Adele"))))
    }

    @Test
    fun `prefers the plain song to a remix, unless the remix was asked for`() {
        val remix = track("Pasoori - Remix", "Ali Sethi", id = "remix")
        val plain = track("Pasoori", "Ali Sethi", id = "plain")

        assertEquals(plain, bestMatch(ImportedSong("Pasoori", "Ali Sethi"), listOf(remix, plain)))
        assertEquals(remix, bestMatch(ImportedSong("Pasoori - Remix", "Ali Sethi"), listOf(plain, remix)))
    }

    @Test
    fun `leaves a karaoke copy for the original`() {
        val karaoke = track("Tum Hi Ho", "Mithoon", id = "copy", albumName = "Karaoke Sing Along")
        val original = track("Tum Hi Ho (From \"Aashiqui 2\")", "Mithoon", "Arijit Singh", id = "original")

        val song = ImportedSong("Tum Hi Ho", "Mithoon;Arijit Singh", album = "Aashiqui 2")

        assertEquals(original, bestMatch(song, listOf(karaoke, original)))
    }

    @Test
    fun `prefers the recording on the same album`() {
        val other = track("Excuses", "AP Dhillon", id = "other", albumName = "Hits")
        val same = track("Excuses", "AP Dhillon", id = "same", albumName = "Excuses")

        assertEquals(same, bestMatch(ImportedSong("Excuses", "AP Dhillon", album = "Excuses"), listOf(other, same)))
    }

    @Test
    fun `ignores accents, punctuation and extra additions`() {
        val song = track("Don't Stop - Remastered 2011", "Beyoncé")

        assertEquals(song, bestMatch(ImportedSong("Dont Stop", "Beyonce feat. Someone"), listOf(song)))
    }

    @Test
    fun `keeps Hindi vowel signs`() {
        val song = track("केसरिया", "अरिजीत सिंह")

        assertEquals(song, bestMatch(ImportedSong("केसरिया", "अरिजीत सिंह"), listOf(song)))
        assertNull(bestMatch(ImportedSong("कसरिया", "अरिजीत सिंह"), listOf(song)))
    }

    @Test
    fun `searches with the first artist and without the additions`() {
        assertEquals("Kesariya Pritam", searchQuery(ImportedSong("Kesariya (From \"X\")", "Pritam;Arijit Singh")))
        assertEquals("A B", searchQuery(ImportedSong("A", "B", orderKnown = false)))
    }
}
