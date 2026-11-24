package com.mushi.app.modules.library.local_tracks.media

import androidx.compose.runtime.Composable

data class LocalMediaPermissionState(
    val isGranted: Boolean,
    val requestPermission: () -> Unit)

@Composable
expect fun rememberLocalMediaPermissionState(): LocalMediaPermissionState
