package com.mushi.app.core.window

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.mushi.app.core.db.Database
import com.mushi.app.core.db.DatabaseKeys

/**
 * What survives a restart: the last *normal* (floating) size, and whether the window was
 * maximized. The size is kept while maximized so un-maximizing after a restart restores it.
 */
@Serializable
data class PersistedWindowState(
    val widthDp: Float,
    val heightDp: Float,
    val maximized: Boolean
) {
    val size: DpSize get() = DpSize(widthDp.dp, heightDp.dp)
    val placement: WindowPlacement
        get() = if (maximized) WindowPlacement.Maximized else WindowPlacement.Floating

    companion object {
        fun of(size: DpSize, placement: WindowPlacement) = PersistedWindowState(
            widthDp = size.width.value,
            heightDp = size.height.value,
            maximized = placement == WindowPlacement.Maximized)
    }
}

class WindowStateRepository internal constructor(private val dataStore: DataStore<Preferences>) {
    constructor(database: Database) : this(database.windowStateDataStore)

    private val json = Json { ignoreUnknownKeys = true }

    /** Returns null when nothing was saved yet or the stored value is unreadable. */
    suspend fun load(): PersistedWindowState? = runCatching {
        val raw = dataStore.data.first()[DatabaseKeys.WINDOW_STATE_KEY] ?: return null
        json.decodeFromString<PersistedWindowState>(raw)
    }.getOrNull()

    suspend fun save(state: PersistedWindowState) {
        runCatching {
            dataStore.edit { prefs ->
                prefs[DatabaseKeys.WINDOW_STATE_KEY] = json.encodeToString(PersistedWindowState.serializer(), state)
            }
        }
    }
}

/**
 * Keeps a restored [size] usable: never smaller than [min], and never bigger than the
 * available screen ([maxWidth] x [maxHeight]) so a size saved on a larger monitor can't open
 * off-screen. A null max means the screen size is unknown, so only [min] is enforced.
 */
fun DpSize.coerceForScreen(
    min: DpSize,
    maxWidth: Dp?,
    maxHeight: Dp?
): DpSize {
    val width = width.coerceAtLeast(min.width).let { if (maxWidth != null) it.coerceAtMost(maxOf(maxWidth, min.width)) else it }
    val height = height.coerceAtLeast(min.height).let { if (maxHeight != null) it.coerceAtMost(maxOf(maxHeight, min.height)) else it }
    return DpSize(width, height)
}
