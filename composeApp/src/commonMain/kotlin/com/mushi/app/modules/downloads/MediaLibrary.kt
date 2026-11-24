package com.mushi.app.modules.downloads

/** Tells the OS media index about a finished download so other apps can see it right away. */
internal expect fun addToMediaLibrary(path: String)
