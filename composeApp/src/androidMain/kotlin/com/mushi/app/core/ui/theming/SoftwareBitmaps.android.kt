package com.mushi.app.core.ui.theming

import coil3.request.ImageRequest
import coil3.request.allowHardware

actual fun ImageRequest.Builder.softwareBitmapsOnly(): ImageRequest.Builder = allowHardware(false)
