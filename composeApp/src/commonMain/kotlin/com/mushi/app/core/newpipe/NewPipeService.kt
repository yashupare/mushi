package com.mushi.app.core.newpipe

enum class SearchKind {
    /** YouTube Music "Songs" catalogue. */
    Songs,

    /** Regular YouTube videos; covers tracks that are not in the Music songs catalogue. */
    Videos
}

expect class NewPipeService() {
    suspend fun searchVideos(query: String, kind: SearchKind = SearchKind.Songs): List<VideoSearchResult>
    suspend fun getVideoInfo(id: String): VideoInfo
}