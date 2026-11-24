package com.mushi.app.core.audioplayer

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerQueue {
	val queueFlow: StateFlow<List<QueueEntry>>
	val currentQueueEntryFlow: StateFlow<QueueEntry?>
	val currentCollectionEntryFlow: StateFlow<QueueCollectionEntry?>
	val collectionHistoryFlow: StateFlow<List<QueueCollectionEntry>>

	suspend fun load(
		entries: List<QueueEntry>,
		autoPlay: Boolean = true,
		startPosition: Int = 0,
		collectionEntry: QueueCollectionEntry? = null)
	suspend fun addToQueue(entry: QueueEntry)
	suspend fun addAllToQueue(entries: List<QueueEntry>, collectionEntry: QueueCollectionEntry? = null)
	suspend fun addAllAfterCurrent(entries: List<QueueEntry>)
	suspend fun removeFromQueue(entry: QueueEntry)
	suspend fun removeFromQueueByMediaUrl(mediaUrl: String)
	suspend fun move(fromIndex: Int, toIndex: Int)
	suspend fun jumpTo(index: Int, autoPlay: Boolean = true)
	suspend fun reloadCurrent()
	suspend fun clear()
	suspend fun getQueue(): List<QueueEntry>
	suspend fun getCurrentQueueEntry(): QueueEntry?
	suspend fun getCurrentCollectionEntry(): QueueCollectionEntry?
	suspend fun getCollectionHistory(): List<QueueCollectionEntry>

	/**
	 * Stops endless playback from adding recommendations when the queue runs out. A jam
	 * guest's queue belongs to the host; recommendations of its own would split them.
	 */
	fun setEndlessPlaybackSuppressed(suppressed: Boolean) = Unit

	fun isPlaylistPlaying(playlistId: String? = null): Boolean {
		val entry = currentCollectionEntryFlow.value as? QueueCollectionEntry.Playlist ?: return false
		return playlistId == null || entry.id == playlistId
	}

	fun isAlbumPlaying(albumId: String? = null): Boolean {
		val entry = currentCollectionEntryFlow.value as? QueueCollectionEntry.Album ?: return false
		return albumId == null || entry.id == albumId
	}

	fun isSavedTracksPlaying(): Boolean {
		return currentCollectionEntryFlow.value is QueueCollectionEntry.SavedTracks
	}
}
