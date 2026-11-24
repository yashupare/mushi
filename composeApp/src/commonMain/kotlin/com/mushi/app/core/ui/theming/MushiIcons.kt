package com.mushi.app.core.ui.theming

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Simple line icons for the tab bar, on a 24 by 24 grid with round caps and joins. Each tab has
 * a regular version and a bolder "selected" version (the house also fills in), so the active tab
 * reads without a coloured pill. Drawn in black; the tab bar tints them.
 */
object MushiIcons {
    private const val REGULAR = 1.75f
    private const val BOLD = 2.6f

    private const val HOME = "M3 10.6 12 3.2l9 7.4V19.5A1.5 1.5 0 0 1 19.5 21H15v-6.5H9V21H4.5A1.5 1.5 0 0 1 3 19.5Z"
    private const val LIBRARY = "M5 4v16M10 4v16M15.2 6l4.6 13.6"
    private const val SEARCH = "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14ZM16.2 16.2 20.5 20.5"

    val Home: ImageVector by lazy { line("MushiHome", HOME, REGULAR) }
    // Selected tabs all get the same bolder stroke, so no tab changes style, only weight.
    val HomeSelected: ImageVector by lazy { line("MushiHomeSelected", HOME, BOLD) }
    val Library: ImageVector by lazy { line("MushiLibrary", LIBRARY, REGULAR) }
    val LibrarySelected: ImageVector by lazy { line("MushiLibrarySelected", LIBRARY, BOLD) }
    val Search: ImageVector by lazy { line("MushiSearch", SEARCH, REGULAR) }
    val SearchSelected: ImageVector by lazy { line("MushiSearchSelected", SEARCH, BOLD) }

    private const val REFRESH = "M21 12a9 9 0 0 1-15.5 6.2L3 15.7M3 12a9 9 0 0 1 15.5-6.2L21 8.3M3 20v-4.3h4.3M21 4v4.3h-4.3"
    private const val FOLDER_ADD = "M3 6.5A1.5 1.5 0 0 1 4.5 5h4l2 2.5h9A1.5 1.5 0 0 1 21 9v9.5a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 18.5ZM12 11v5.5M9.25 13.75h5.5"
    private const val CLOSE = "M6 6l12 12M18 6 6 18"
    private const val TRASH = "M4 7h16M9.5 7V4.5h5V7M6.5 7l.8 12.2A1.5 1.5 0 0 0 8.8 20.6h6.4a1.5 1.5 0 0 0 1.5-1.4L17.5 7M10 11v6M14 11v6"

    val Refresh: ImageVector by lazy { line("MushiRefresh", REFRESH, REGULAR) }
    val FolderAdd: ImageVector by lazy { line("MushiFolderAdd", FOLDER_ADD, REGULAR) }
    val Close: ImageVector by lazy { line("MushiClose", CLOSE, REGULAR) }
    val Trash: ImageVector by lazy { line("MushiTrash", TRASH, REGULAR) }

    val Download: ImageVector by lazy { line("MushiDownload", "M12 4v10m0 0-3.8-3.8M12 14l3.8-3.8M4 15v2.5A2.5 2.5 0 0 0 6.5 20h11a2.5 2.5 0 0 0 2.5-2.5V15", REGULAR) }
    val Shuffle: ImageVector by lazy { line("MushiShuffle", "M16 3h5v5M4 20 21 3M21 16v5h-5M15 15l6 6M4 4l5 5", REGULAR) }
    val MusicNote: ImageVector by lazy { line("MushiMusicNote", "M9 18V6l10-2v12M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0ZM19 16a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z", REGULAR) }
    val Album: ImageVector by lazy { line("MushiAlbum", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM12 9.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5Z", REGULAR) }
    val Cast: ImageVector by lazy { line("MushiCast", "M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-6M3 12a9 9 0 0 1 9 9M3 16a5 5 0 0 1 5 5M3 20h.01", REGULAR) }
    val Folder: ImageVector by lazy { line("MushiFolder", "M3 7a2 2 0 0 1 2-2h4l2 2.5h8a2 2 0 0 1 2 2V17a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2Z", REGULAR) }
    val UserRemove: ImageVector by lazy { line("MushiUserRemove", "M10 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM3 20a7 7 0 0 1 14 0M16.5 11h5", REGULAR) }
    val Info: ImageVector by lazy { line("MushiInfo", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM12 11v5.5M12 7.8h.01", REGULAR) }
    val AddSquare: ImageVector by lazy { line("MushiAddSquare", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2ZM12 8v8M8 12h8", REGULAR) }
    val Playlist: ImageVector by lazy { line("MushiPlaylist", "M3.5 6h13M3.5 11h13M3.5 16h6.5M19 17.5V9l3 1M19 17.5a2 2 0 1 1-4 0 2 2 0 0 1 4 0Z", REGULAR) }
    val Edit: ImageVector by lazy { line("MushiEdit", "M4 20h4L19.5 8.5a2.1 2.1 0 0 0-3-3L5 17ZM14.5 7.5l3 3", REGULAR) }
    val Repeat: ImageVector by lazy { line("MushiRepeat", "M17 2l4 4-4 4M3 11V9a3 3 0 0 1 3-3h15M7 22l-4-4 4-4M21 13v2a3 3 0 0 1-3 3H3", REGULAR) }
    val RepeatOne: ImageVector by lazy { line("MushiRepeatOne", "M17 2l4 4-4 4M3 11V9a3 3 0 0 1 3-3h15M7 22l-4-4 4-4M21 13v2a3 3 0 0 1-3 3H3M11 10l1.6-1.2v6.4", REGULAR) }
    val Forbidden: ImageVector by lazy { line("MushiForbidden", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM5.6 5.6l12.8 12.8", REGULAR) }
    val Plus: ImageVector by lazy { line("MushiPlus", "M12 5v14M5 12h14", REGULAR) }
    val Clock: ImageVector by lazy { line("MushiClock", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM12 7v5l3.2 2", REGULAR) }
    val Share: ImageVector by lazy { line("MushiShare", "M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM18 22a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM8.6 13.5l6.8 4M15.4 6.5l-6.8 4", REGULAR) }
    val Sort: ImageVector by lazy { line("MushiSort", "M4 7h16M7 12h10M10 17h4", REGULAR) }
    val SkipNext: ImageVector by lazy { solid("MushiSkipNext", "M6 5l9 7-9 7ZM18.5 5v14") }
    val SkipPrevious: ImageVector by lazy { solid("MushiSkipPrevious", "M18 5l-9 7 9 7ZM5.5 5v14") }
    val More: ImageVector by lazy { line("MushiMore", "M6 12h.01M12 12h.01M18 12h.01", 3.6f) }

    val AlignLeft: ImageVector by lazy { line("MushiAlignLeft", "M4 6h16M4 11h16M4 16h10M4 21h6", REGULAR) }
    val Globe: ImageVector by lazy { line("MushiGlobe", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM3 12h18M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9c-2.5-2.6-3.8-5.6-3.8-9S9.5 5.6 12 3Z", REGULAR) }
    val Eye: ImageVector by lazy { line("MushiEye", "M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12ZM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z", REGULAR) }
    val Moon: ImageVector by lazy { line("MushiMoon", "M20 14.5A8 8 0 0 1 9.5 4 8.5 8.5 0 1 0 20 14.5Z", REGULAR) }
    val Link: ImageVector by lazy { line("MushiLink", "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1M14 10a4 4 0 0 0-5.7 0l-3 3A4 4 0 0 0 10 18.7l1-1", REGULAR) }
    val Palette: ImageVector by lazy { line("MushiPalette", "M12 3a9 9 0 1 0 0 18c1.1 0 1.8-.9 1.5-1.9-.3-1 .4-2.1 1.5-2.1H17a4 4 0 0 0 4-4C21 6.6 17 3 12 3ZM7.5 11h.01M10 7.5h.01M14.5 7.5h.01", REGULAR) }
    val Tune: ImageVector by lazy { line("MushiTune", "M4 7h9M17 7h3M4 17h3M11 17h9M15 4.5v5M9 14.5v5", REGULAR) }
    val MusicBlock: ImageVector by lazy { line("MushiMusicBlock", "M9 18V6l8-1.6M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0ZM17 15.5a3 3 0 1 1-4-2.8M3 3l18 18", REGULAR) }
    val Waveform: ImageVector by lazy { line("MushiWaveform", "M4 10v4M8 6v12M12 3v18M16 8v8M20 11v2", REGULAR) }

    val User: ImageVector by lazy { line("MushiUser", "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM4.5 20a7.5 7.5 0 0 1 15 0", REGULAR) }
    val Wifi: ImageVector by lazy { line("MushiWifi", "M2 9a15 15 0 0 1 20 0M5.5 12.5a10 10 0 0 1 13 0M9 16a5 5 0 0 1 6 0M12 19.5h.01", REGULAR) }
    val Box: ImageVector by lazy { line("MushiBox", "M3.5 7.5 12 3l8.5 4.5v9L12 21l-8.5-4.5ZM3.5 7.5 12 12l8.5-4.5M12 12v9", REGULAR) }
    val Monitor: ImageVector by lazy { line("MushiMonitor", "M3 5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2ZM8 21h8M12 17v4", REGULAR) }
    val Upload: ImageVector by lazy { line("MushiUpload", "M12 15V4m0 0L8.2 7.8M12 4l3.8 3.8M4 15v2.5A2.5 2.5 0 0 0 6.5 20h11a2.5 2.5 0 0 0 2.5-2.5V15", REGULAR) }
    val Document: ImageVector by lazy { line("MushiDocument", "M7 3h7l5 5v11a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2ZM14 3v5h5M8.5 13h7M8.5 17h5", REGULAR) }
    val Database: ImageVector by lazy { line("MushiDatabase", "M4 6c0-1.7 3.6-3 8-3s8 1.3 8 3-3.6 3-8 3-8-1.3-8-3ZM4 6v6c0 1.7 3.6 3 8 3s8-1.3 8-3V6M4 12v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6", REGULAR) }
    val Server: ImageVector by lazy { line("MushiServer", "M4 4h16a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1ZM4 13h16a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-5a1 1 0 0 1 1-1ZM7 7.5h.01M7 16.5h.01", REGULAR) }
    val Stop: ImageVector by lazy { line("MushiStop", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18ZM9.5 9.5h5v5h-5Z", REGULAR) }
    val Swap: ImageVector by lazy { line("MushiSwap", "M17 4l4 4-4 4M21 8H8M7 20l-4-4 4-4M3 16h13", REGULAR) }
    val DragHandle: ImageVector by lazy { line("MushiDragHandle", "M5 9h14M5 15h14", REGULAR) }

    /** A single-tone play triangle with softly rounded corners (no duotone ghost). */
    val Play: ImageVector by lazy { solid("MushiPlay", "M8 5v14l11-7Z") }

    /** A single-tone pause: two rounded bars. */
    val Pause: ImageVector by lazy { solid("MushiPause", "M7 5h3v14H7ZM14 5h3v14h-3Z") }

    private fun solid(name: String, pathData: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build()

    private fun line(
        name: String,
        pathData: String,
        strokeWidth: Float,
        filled: Boolean = false
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = if (filled) SolidColor(Color.Black) else null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build()
}
