package com.mushi.app.core.ui.theming

import coil3.request.ImageRequest

/**
 * Asks Coil for a bitmap that can be read from a software canvas. Only Android has hardware
 * bitmaps; everywhere else this does nothing.
 */
expect fun ImageRequest.Builder.softwareBitmapsOnly(): ImageRequest.Builder
