package com.mushi.app.modules.library.local_tracks.media

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocalMediaPermissionState(): LocalMediaPermissionState {
    return LocalMediaPermissionState(
        isGranted = true,
        requestPermission = {})
}
