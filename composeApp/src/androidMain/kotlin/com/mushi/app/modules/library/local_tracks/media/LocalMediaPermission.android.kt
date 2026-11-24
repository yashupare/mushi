package com.mushi.app.modules.library.local_tracks.media

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberLocalMediaPermissionState(): LocalMediaPermissionState {
    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    var isGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()) { granted ->
        isGranted = granted ||
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    return LocalMediaPermissionState(
        isGranted = isGranted,
        requestPermission = { launcher.launch(permission) })
}
