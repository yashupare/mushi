package com.mushi.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.mushi.app.MainActivity
import com.mushi.app.R
import com.mushi.app.core.audioplayer.AudioPlayerInterface
import com.mushi.app.core.audioplayer.AudioPlayerQueue
import com.mushi.app.core.audioplayer.PlayerState
import com.mushi.app.core.audioplayer.QueueEntry
import com.mushi.app.core.history.ListeningHistoryRepository
import com.mushi.app.core.history.durationMs
import com.mushi.app.core.history.historyKey
import com.mushi.app.core.i18n.getPluralString
import com.mushi.app.core.i18n.getString
import com.mushi.app.core.moments.ConstellationPlayer
import com.mushi.app.core.moments.MomentLibrary
import com.mushi.app.core.moments.MomentsRepository
import com.mushi.app.core.moments.monthKey
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.home_continue_time_left
import com.mushi.app.generated.resources.home_continue_title
import com.mushi.app.generated.resources.sky_moment_count
import com.mushi.app.generated.resources.sky_star_count
import com.mushi.app.generated.resources.widget_empty_continue
import com.mushi.app.generated.resources.widget_empty_paths
import com.mushi.app.generated.resources.widget_empty_player
import com.mushi.app.generated.resources.widget_empty_sky_first
import com.mushi.app.generated.resources.widget_empty_sky_second
import com.mushi.app.generated.resources.widget_open_app
import com.mushi.app.media.PlaybackService
import com.mushi.app.modules.home.ContinueCard
import com.mushi.app.modules.home.resumeFrom
import com.mushi.app.modules.home.toContinueCards
import com.mushi.app.modules.saved_tracks.SavedTracksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

private const val TAG = "MushiWidgets"

private const val ACTION_PLAY_PAUSE = "com.mushi.app.widget.PLAY_PAUSE"
private const val ACTION_NEXT = "com.mushi.app.widget.NEXT"
private const val ACTION_LIKE = "com.mushi.app.widget.LIKE"
private const val ACTION_LATEST_MOMENT = "com.mushi.app.widget.LATEST_MOMENT"
private const val ACTION_RESUME = "com.mushi.app.widget.RESUME"
private const val ACTION_PATH = "com.mushi.app.widget.PATH"
private const val EXTRA_INDEX = "index"

/** What every widget shows. They're redrawn only when this changes, never on a timer. */
private data class WidgetData(
    val entry: QueueEntry?,
    val playing: Boolean,
    val liked: Boolean,
    val resume: ContinueCard?,
    val library: MomentLibrary)

/** Keeps the home screen widgets showing what the app knows, and carries out their buttons. */
object WidgetUpdater : KoinComponent {
    private val audioPlayer: AudioPlayerInterface by inject()
    private val audioPlayerQueue: AudioPlayerQueue by inject()
    private val savedTracks: SavedTracksRepository by inject()
    private val history: ListeningHistoryRepository by inject()
    private val moments: MomentsRepository by inject()
    private val constellationPlayer: ConstellationPlayer by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val drawing = Mutex()
    private lateinit var context: Context

    private val providers = listOf(
        NowPlayingWidget::class.java,
        SlimPlayerWidget::class.java,
        MiniSkyWidget::class.java,
        ContinueWidget::class.java,
        ConstellationsWidget::class.java)

    @OptIn(FlowPreview::class)
    private fun data() = combine(
        audioPlayerQueue.currentQueueEntryFlow,
        audioPlayer.playerStateFlow.map { it == PlayerState.PLAYING }.distinctUntilChanged(),
        savedTracks.savedTracksIdsFlow,
        history.history,
        moments.library) { entry, playing, liked, history, library ->
        val underWay = playing || audioPlayer.positionFlow.value.inWholeMilliseconds > 1_000
        WidgetData(
            entry = entry,
            playing = playing,
            liked = (entry as? QueueEntry.StreamingTrack)?.track?.id in liked,
            resume = history.toContinueCards(entry?.historyKey()?.takeIf { underWay }).firstOrNull(),
            library = library)
    }

    @OptIn(FlowPreview::class)
    fun start(appContext: Context) {
        context = appContext.applicationContext
        scope.launch {
            data().debounce(400).distinctUntilChanged().collect { draw() }
        }
    }

    /** Redraws now, for a widget that was just added or resized, or after the phone restarts. */
    suspend fun refresh() {
        // The saved queue takes a moment to come back after the app starts
        withTimeoutOrNull(4_000) { audioPlayerQueue.queueFlow.first { it.isNotEmpty() } }
        draw()
    }

    private suspend fun draw() = drawing.withLock {
        // Read inside the lock: a redraw that waited its turn must not paint older state
        val data = data().first()
        val manager = AppWidgetManager.getInstance(context)
        val renderer by lazy { WidgetRenderer(context) }
        providers.forEach { provider ->
            manager.getAppWidgetIds(ComponentName(context, provider)).forEach { id ->
                try {
                    val options = manager.getAppWidgetOptions(id)
                    val views = views(provider, data, renderer, options)
                    manager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    // A widget must never take the app down with it
                    Log.w(TAG, "Couldn't draw ${provider.simpleName}", e)
                }
            }
        }
    }

    private suspend fun views(
        provider: Class<*>,
        data: WidgetData,
        renderer: WidgetRenderer,
        options: Bundle): RemoteViews {
        fun size(key: String, fallback: Int) =
            options.getInt(key, 0).takeIf { it > 0 }?.toFloat() ?: fallback.toFloat()
        val width = size(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val height = size(
            AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
            if (provider == SlimPlayerWidget::class.java) 60 else 120)

        return when (provider) {
            NowPlayingWidget::class.java -> player(R.layout.widget_now_playing, data, renderer, width, height)
            SlimPlayerWidget::class.java -> player(R.layout.widget_slim, data, renderer, width, height)
            MiniSkyWidget::class.java -> miniSky(data, renderer, width, height)
            ContinueWidget::class.java -> continueViews(data, renderer, width, height)
            else -> paths(data, renderer, width, height)
        }
    }

    private fun picture(layout: Int, bitmap: Bitmap, description: String) =
        RemoteViews(context.packageName, layout).apply {
            setImageViewBitmap(R.id.widget_picture, bitmap)
            setContentDescription(R.id.widget_picture, description)
        }

    private suspend fun player(
        layout: Int, data: WidgetData, renderer: WidgetRenderer, width: Float, height: Float): RemoteViews {
        val slim = layout == R.layout.widget_slim
        val entry = data.entry
        val views = if (entry == null) {
            picture(layout, renderer.message(width, height, getString(Res.string.widget_empty_player)),
                getString(Res.string.widget_open_app))
        } else {
            val art = art(entry.artworkModel)
            val color = WidgetRenderer.tintOf(art)
            val (title, artist) = entry.names()
            val duration = entry.durationMs()
            val progress = if (duration > 0) {
                audioPlayer.positionFlow.value.inWholeMilliseconds.toFloat() / duration
            } else 0f
            picture(
                layout,
                if (slim) renderer.slim(width, height, art, color, title, artist, data.playing)
                else renderer.nowPlaying(
                    width, height, art, color, title, artist, data.playing, data.liked, progress),
                "$title, $artist")
        }
        views.setOnClickPendingIntent(R.id.widget_picture, openApp())
        views.setOnClickPendingIntent(R.id.widget_play, playback(ACTION_PLAY_PAUSE))
        views.setOnClickPendingIntent(R.id.widget_next, playback(ACTION_NEXT))
        if (!slim) views.setOnClickPendingIntent(R.id.widget_like, action(ACTION_LIKE))
        return views
    }

    private suspend fun miniSky(
        data: WidgetData, renderer: WidgetRenderer, width: Float, height: Float): RemoteViews {
        val zone = TimeZone.currentSystemDefault()
        val month = monthKey(Clock.System.now().toEpochMilliseconds(), zone)
        val all = data.library.moments
        val shownMonth = all.map { monthKey(it.keptAtMs, zone) }.maxOrNull() ?: month
        val stars = all.filter { monthKey(it.keptAtMs, zone) == shownMonth }
            .takeLast(12)
            .mapIndexed { i, moment -> WidgetStar(moment.x, moment.y, STAR_COLORS[i % STAR_COLORS.size]) }
        val kept = stars.isNotEmpty()
        val count = getPluralString(Res.plurals.sky_moment_count, all.size, all.size)
        val bitmap = renderer.miniSky(
            width, height,
            stars = if (kept) stars else FAINT.mapIndexed { i, (x, y) ->
                WidgetStar(x, y, STAR_COLORS[i % STAR_COLORS.size])
            },
            kept = kept,
            headline = if (kept) all.size.toString() else getString(Res.string.widget_empty_sky_first),
            caption = if (kept) count.substringAfter(' ') else getString(Res.string.widget_empty_sky_second))
        return picture(R.layout.widget_mini_sky, bitmap, count).apply {
            setOnClickPendingIntent(R.id.widget_picture, openApp())
            setOnClickPendingIntent(R.id.widget_play, if (kept) playback(ACTION_LATEST_MOMENT) else openApp())
        }
    }

    private suspend fun continueViews(
        data: WidgetData, renderer: WidgetRenderer, width: Float, height: Float): RemoteViews {
        val card = data.resume
        if (card == null) {
            val text = getString(Res.string.widget_empty_continue)
            return picture(R.layout.widget_continue, renderer.message(width, height, text), text)
                .apply { setOnClickPendingIntent(R.id.widget_picture, openApp()) }
        }
        val art = art(card.entry.artworkModel)
        val left = getString(Res.string.home_continue_time_left).replace("%1\$s", card.timeLeft)
        val label = getString(Res.string.home_continue_title)
        val bitmap = renderer.resume(
            width, height, art, WidgetRenderer.tintOf(art), label, card.title, left, card.progress)
        return picture(R.layout.widget_continue, bitmap, "$label, ${card.title}, $left")
            .apply { setOnClickPendingIntent(R.id.widget_picture, playback(ACTION_RESUME)) }
    }

    private suspend fun paths(
        data: WidgetData, renderer: WidgetRenderer, width: Float, height: Float): RemoteViews {
        val byId = data.library.moments.associateBy { it.id }
        val saved = data.library.constellations.take(3).map { constellation ->
            val stars = constellation.momentIds.mapNotNull(byId::get)
            // Stretch the shape to fill its little box
            val minX = stars.minOfOrNull { it.x } ?: 0f
            val minY = stars.minOfOrNull { it.y } ?: 0f
            val spanX = ((stars.maxOfOrNull { it.x } ?: 1f) - minX).coerceAtLeast(0.01f)
            val spanY = ((stars.maxOfOrNull { it.y } ?: 1f) - minY).coerceAtLeast(0.01f)
            WidgetPath(
                name = constellation.name,
                count = getPluralString(Res.plurals.sky_star_count, stars.size, stars.size),
                stars = stars.mapIndexed { i, m ->
                    WidgetStar((m.x - minX) / spanX, (m.y - minY) / spanY, STAR_COLORS[i % STAR_COLORS.size])
                })
        }
        val empty = getString(Res.string.widget_empty_paths)
        val bitmap = if (saved.isEmpty()) renderer.message(width, height, empty)
        else renderer.constellations(width, height, saved)
        val views = picture(
            R.layout.widget_constellations, bitmap,
            if (saved.isEmpty()) empty else saved.joinToString { it.name })
        views.setOnClickPendingIntent(R.id.widget_picture, openApp())
        listOf(R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3).forEachIndexed { index, row ->
            views.setOnClickPendingIntent(
                row, if (index < saved.size) playback(ACTION_PATH, index) else openApp())
        }
        return views
    }

    private suspend fun art(model: Any?): Bitmap? {
        if (model == null) return null
        val request = ImageRequest.Builder(context).data(model).size(256).allowHardware(false).build()
        val result = withTimeoutOrNull(8_000) { SingletonImageLoader.get(context).execute(request) }
        return (result as? SuccessResult)?.image?.toBitmap()
    }

    private fun QueueEntry.names(): Pair<String, String> = when (this) {
        is QueueEntry.StreamingTrack -> track.title to track.artists.joinToString(", ") { it.name }
        is QueueEntry.LocalTrack -> name to artists.joinToString(", ")
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    /**
     * For buttons that start music. Android only lets the playback service start from a widget
     * when the button starts it directly; from a broadcast it would be refused with the app
     * closed, and the song would play with no notification until Android stopped it.
     */
    private fun playback(name: String, index: Int = 0): PendingIntent = PendingIntent.getForegroundService(
        context, name.hashCode() + index,
        Intent(context, PlaybackService::class.java).setAction(name).putExtra(EXTRA_INDEX, index),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    /** For buttons that don't. */
    private fun action(name: String, index: Int = 0): PendingIntent = PendingIntent.getBroadcast(
        context, name.hashCode() + index,
        Intent(context, NowPlayingWidget::class.java).setAction(name).putExtra(EXTRA_INDEX, index),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    /** Carries out a widget button. The app may have only just been started for it. */
    suspend fun perform(action: String, index: Int) {
        // Let the saved queue come back first, or it would replace what we start
        withTimeoutOrNull(3_000) { audioPlayerQueue.queueFlow.first { it.isNotEmpty() } }
        var startsPlaying = true
        when (action) {
            ACTION_PLAY_PAUSE -> if (audioPlayer.playerStateFlow.value == PlayerState.PLAYING) {
                audioPlayer.pause()
                startsPlaying = false
            } else audioPlayer.play()

            ACTION_NEXT -> {
                audioPlayer.skipToNext()
                audioPlayer.play()
            }

            ACTION_LIKE -> {
                startsPlaying = false
                val id = (audioPlayerQueue.getCurrentQueueEntry() as? QueueEntry.StreamingTrack)?.track?.id
                    ?: return
                if (id in savedTracks.savedTracksIdsFlow.value) savedTracks.removeSavedTracks(listOf(id))
                else savedTracks.saveTracks(listOf(id))
            }

            ACTION_LATEST_MOMENT -> moments.library.first().moments.maxByOrNull { it.keptAtMs }
                ?.let { constellationPlayer.play(listOf(it)) }

            ACTION_RESUME -> history.history.first().toContinueCards(null).firstOrNull()
                ?.let { resumeFrom(audioPlayerQueue, audioPlayer, it) }

            ACTION_PATH -> {
                val library = moments.library.first()
                val byId = library.moments.associateBy { it.id }
                library.constellations.getOrNull(index)
                    ?.let { saved -> constellationPlayer.play(saved.momentIds.mapNotNull(byId::get)) }
            }
        }
        // Stay alive until the playback service has taken over
        if (startsPlaying) withTimeoutOrNull(5_000) {
            audioPlayer.playerStateFlow.first { it == PlayerState.PLAYING }
        }
    }

    /** Whether [intent] is a widget button sent to the playback service; carries it out if so. */
    internal fun handleServiceStart(intent: Intent?, scope: CoroutineScope, done: () -> Unit): Boolean {
        val action = intent?.action?.takeIf { it.startsWith("com.mushi.app.widget.") } ?: return false
        val index = intent.getIntExtra(EXTRA_INDEX, 0)
        scope.launch {
            try {
                withTimeoutOrNull(12_000) { perform(action, index) }
            } catch (e: Exception) {
                Log.w(TAG, "Widget request failed", e)
            } finally {
                done()
            }
        }
        return true
    }

    internal fun run(pending: android.content.BroadcastReceiver.PendingResult, block: suspend () -> Unit) {
        scope.launch {
            try {
                withTimeoutOrNull(9_000) { block() }
            } catch (e: Exception) {
                Log.w(TAG, "Widget request failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private val FAINT = listOf(0.2f to 0.28f, 0.48f to 0.18f, 0.76f to 0.34f, 0.32f to 0.56f, 0.64f to 0.5f)
}

/** The five widgets share their behaviour; each only differs in what [WidgetUpdater] draws. */
abstract class MushiWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetUpdater.run(goAsync()) { WidgetUpdater.refresh() }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        WidgetUpdater.run(goAsync()) { WidgetUpdater.refresh() }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action.startsWith("com.mushi.app.widget.")) {
            val index = intent.getIntExtra(EXTRA_INDEX, 0)
            WidgetUpdater.run(goAsync()) { WidgetUpdater.perform(action, index) }
        } else {
            super.onReceive(context, intent)
        }
    }
}

class NowPlayingWidget : MushiWidget()
class SlimPlayerWidget : MushiWidget()
class MiniSkyWidget : MushiWidget()
class ContinueWidget : MushiWidget()
class ConstellationsWidget : MushiWidget()
