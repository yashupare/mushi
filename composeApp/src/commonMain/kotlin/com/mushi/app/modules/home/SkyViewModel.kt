package com.mushi.app.modules.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.durationMs
import com.mushi.app.core.history.historyKey
import com.mushi.app.core.jam.JamRole
import com.mushi.app.core.jam.JamRoomService
import com.mushi.app.core.moments.ConstellationPlayer
import com.mushi.app.core.moments.Moment
import com.mushi.app.core.moments.MomentLibrary
import com.mushi.app.core.moments.MomentsRepository
import com.mushi.app.core.moments.PathPlayback
import com.mushi.app.core.moments.monthKey
import com.mushi.app.modules.shell.SleepTimerProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration

data class StarUi(
    val id: String,
    val x: Float,
    val y: Float,
    val title: String,
    val artists: String,
    val artworkUrl: String?,
    val artworkModel: Any?,
    /** The kept spot, or empty for a faint star from the queue. */
    val time: String,
    val kept: Boolean,
    val positionMs: Long = 0,
    val durationMs: Long = 0)

data class ConstellationUi(val id: String, val name: String, val stars: Int)

data class SkyUiState(
    val visible: Boolean = false,
    val stars: List<StarUi> = emptyList(),
    /** "2026-11", or null for this month. */
    val month: String? = null,
    val hasEarlier: Boolean = false,
    val hasLater: Boolean = false,
    val momentCount: Int = 0,
    val selected: StarUi? = null,
    /** Linked star ids, in order. */
    val path: List<String> = emptyList(),
    /** The stars being played, in playing order, and which one is on. */
    val playing: List<String> = emptyList(),
    val playingIndex: Int = 0,
    val saved: List<ConstellationUi> = emptyList(),
    val naming: Boolean = false,
    val sleepFraction: Float? = null)

/** What the listener is doing with the sky; not saved. */
private data class SkySelection(
    val month: String? = null,
    val selectedId: String? = null,
    val path: List<String> = emptyList(),
    val naming: Boolean = false)

private const val MAX_FAINT_STARS = 6

/** Where the faint stars of a new sky sit. */
private val FAINT_SPOTS = listOf(
    0.22f to 0.42f, 0.54f to 0.31f, 0.80f to 0.50f, 0.34f to 0.70f, 0.66f to 0.76f, 0.14f to 0.82f)

internal fun formatSpot(ms: Long): String {
    val seconds = ms / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

private fun QueueEntry.title(): String = when (this) {
    is QueueEntry.StreamingTrack -> track.title
    is QueueEntry.LocalTrack -> name
}

private fun QueueEntry.artistNames(): String = when (this) {
    is QueueEntry.StreamingTrack -> track.artists.joinToString(", ") { it.name }
    is QueueEntry.LocalTrack -> artists.joinToString(", ")
}

private fun Moment.toStar() = StarUi(
    id = id,
    x = x,
    y = y,
    title = entry.title(),
    artists = entry.artistNames(),
    artworkUrl = entry.artworkUrl,
    artworkModel = entry.artworkModel,
    time = formatSpot(positionMs),
    kept = true,
    positionMs = positionMs,
    durationMs = entry.durationMs())

/** Faint stars for a listener who hasn't kept anything yet. */
private fun faintStars(queue: List<QueueEntry>): List<StarUi> =
    queue.distinctBy { it.historyKey() }.take(MAX_FAINT_STARS).mapIndexed { index, entry ->
        StarUi(
            id = entry.historyKey(),
            x = FAINT_SPOTS[index].first,
            y = FAINT_SPOTS[index].second,
            title = entry.title(),
            artists = entry.artistNames(),
            artworkUrl = entry.artworkUrl,
            artworkModel = entry.artworkModel,
            time = "",
            kept = false)
    }

private fun skyState(
    library: MomentLibrary,
    selection: SkySelection,
    queue: List<QueueEntry>,
    playback: PathPlayback?,
    zone: TimeZone,
    thisMonth: String): SkyUiState {
    val months = library.moments.map { monthKey(it.keptAtMs, zone) }.distinct().sorted()
    val month = selection.month?.takeIf { it in months } ?: months.lastOrNull()
    val stars = if (month == null) {
        faintStars(queue)
    } else {
        library.moments.filter { monthKey(it.keptAtMs, zone) == month }.map { it.toStar() }
    }
    val ids = stars.mapTo(mutableSetOf()) { it.id }
    val index = months.indexOf(month)
    return SkyUiState(
        visible = true,
        stars = stars,
        month = month?.takeIf { it != thisMonth },
        hasEarlier = index > 0,
        hasLater = index in 0 until months.lastIndex,
        momentCount = if (month == null) 0 else stars.size,
        selected = stars.firstOrNull { it.id == selection.selectedId },
        path = selection.path.filter { it in ids },
        playing = playback?.moments?.map { it.id }.orEmpty(),
        playingIndex = playback?.index ?: 0,
        saved = library.constellations.map { ConstellationUi(it.id, it.name, it.momentIds.size) },
        naming = selection.naming)
}

class SkyViewModel(
    private val repository: MomentsRepository,
    private val player: ConstellationPlayer,
    private val audioPlayer: AudioPlayerInterface,
    private val audioPlayerQueue: AudioPlayerQueue,
    jamRoomService: JamRoomService,
    sleepTimer: SleepTimerProgress) : ViewModel() {

    private val selection = MutableStateFlow(SkySelection())
    private var library = MomentLibrary()

    /** For the light travelling along a path; read only while drawing. */
    val position: StateFlow<Duration> = audioPlayer.positionFlow

    private val sky = combine(
        repository.library,
        selection,
        audioPlayerQueue.queueFlow,
        player.playback) { library, selection, queue, playback ->
        this.library = library
        val zone = TimeZone.currentSystemDefault()
        skyState(library, selection, queue, playback, zone, monthKey(Clock.System.now().toEpochMilliseconds(), zone))
    }

    val uiState: StateFlow<SkyUiState> = combine(sky, jamRoomService.role, sleepTimer.fraction) { sky, role, sleep ->
        // A jam guest's queue belongs to the host
        if (role == JamRole.Guest) SkyUiState() else sky.copy(sleepFraction = sleep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SkyUiState())

    fun select(starId: String?) {
        selection.update { it.copy(selectedId = starId, path = emptyList(), naming = false) }
    }

    /** A drag that began on [starId] reached another star. Only kept stars can be linked. */
    fun link(starId: String) {
        if (library.moments.none { it.id == starId }) return
        selection.update {
            if (starId in it.path) it else it.copy(selectedId = null, path = it.path + starId, naming = false)
        }
    }

    fun startLink(starId: String) {
        select(null)
        link(starId)
    }

    /** A path needs two stars; a shorter drag just selects its star. */
    fun endLink() {
        selection.update {
            if (it.path.size > 1) it else it.copy(selectedId = it.path.firstOrNull(), path = emptyList())
        }
    }

    fun playSelected() {
        val star = uiState.value.selected ?: return
        if (star.kept) {
            library.moments.firstOrNull { it.id == star.id }?.let { player.play(listOf(it)) }
        } else viewModelScope.launch {
            val index = audioPlayerQueue.getQueue().indexOfFirst { it.historyKey() == star.id }
            if (index >= 0) audioPlayerQueue.jumpTo(index)
        }
    }

    fun removeSelected() {
        val star = uiState.value.selected ?: return
        select(null)
        viewModelScope.launch { repository.remove(star.id) }
    }

    fun playPath() = play(uiState.value.path)

    fun askName() {
        selection.update { it.copy(naming = true) }
    }

    fun cancelName() {
        selection.update { it.copy(naming = false) }
    }

    fun save(name: String) {
        val path = uiState.value.path
        selection.update { it.copy(naming = false) }
        if (path.size > 1 && name.isNotBlank()) {
            viewModelScope.launch { repository.saveConstellation(name.trim(), path) }
        }
    }

    /** Shows a saved constellation in its month's sky and plays it. */
    fun playSaved(constellationId: String) {
        val saved = library.constellations.firstOrNull { it.id == constellationId } ?: return
        val first = library.moments.firstOrNull { it.id == saved.momentIds.first() } ?: return
        selection.value = SkySelection(
            month = monthKey(first.keptAtMs, TimeZone.currentSystemDefault()),
            path = saved.momentIds)
        play(saved.momentIds)
    }

    fun delete(constellationId: String) {
        viewModelScope.launch { repository.deleteConstellation(constellationId) }
    }

    fun showMonth(step: Int) {
        val zone = TimeZone.currentSystemDefault()
        val months = library.moments.map { monthKey(it.keptAtMs, zone) }.distinct().sorted()
        if (months.isEmpty()) return
        val current = selection.value.month?.takeIf { it in months } ?: months.last()
        val next = months[(months.indexOf(current) + step).coerceIn(0, months.lastIndex)]
        selection.value = SkySelection(month = next)
    }

    private fun play(momentIds: List<String>) {
        val byId = library.moments.associateBy { it.id }
        player.play(momentIds.mapNotNull(byId::get))
    }
}
