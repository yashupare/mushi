package com.mushi.app.core.discovery

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * On Android 13+ (and especially 16+ where it became a runtime permission),
 * mDNS/NSD discovery requires `NEARBY_WIFI_DEVICES`. Requests it when the
 * returned lambda is invoked; the caller decides when (e.g. first visit to the
 * Devices screen).
 */
@Composable
actual fun rememberLocalNetworkPermissionRequester(): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { /* result is picked up by discovery/advertising retry loops */ }

    return {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }
}