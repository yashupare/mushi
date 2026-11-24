package com.mushi.app.core.discovery

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that requests the OS permission needed for local network
 * discovery (mDNS/NSD). No-op on platforms where such a permission doesn't
 * exist or is granted implicitly.
 */
@Composable
expect fun rememberLocalNetworkPermissionRequester(): () -> Unit