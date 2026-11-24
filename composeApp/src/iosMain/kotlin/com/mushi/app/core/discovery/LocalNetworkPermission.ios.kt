package com.mushi.app.core.discovery

import androidx.compose.runtime.Composable

/** No runtime local-network permission needed on iOS. */
@Composable
actual fun rememberLocalNetworkPermissionRequester(): () -> Unit = {}