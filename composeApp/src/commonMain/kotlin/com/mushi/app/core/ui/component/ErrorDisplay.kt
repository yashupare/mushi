package com.mushi.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.mushi.app.core.network.isNetworkError
import com.mushi.app.core.ui.rememberStaggerState
import com.mushi.app.core.ui.staggeredEntrance
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.generated.resources.*
import com.mushi.app.modules.connection.ConnectionViewModel
import com.mushi.app.modules.plugin.MetadataAuthViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.WifiOff
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIcon

/**
 * What a screen shows when loading failed. [errorMessage] is only used to tell a network failure
 * from other ones; it is never shown, since it is internal text. Loads again by itself when the
 * network comes back.
 */
@Composable
fun ErrorDisplay(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: MetadataAuthViewModel = koinViewModel(),
    connection: ConnectionViewModel = koinViewModel()) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val online by connection.online.collectAsStateWithLifecycle()
    val reconnects by connection.reconnects.collectAsStateWithLifecycle()

    // Saved, so a screen that was left while offline still reloads when it is returned to
    var seenReconnects by rememberSaveable { mutableStateOf(connection.reconnects.value) }
    LaunchedEffect(reconnects) {
        if (reconnects > seenReconnects) {
            // The network is announced a moment before it can be used
            delay(2_000)
            seenReconnects = reconnects
            onRetry()
        }
    }

    // Offline first: signing in can't be the fix while there's no network
    if (!online || isNetworkError(errorMessage)) {
        OfflineDisplay(onRetry = onRetry, modifier = modifier, connection = connection)
        return
    }
    if (authState.loginRequired) {
        LoginRequiredDisplay(
            pluginName = authState.pluginName,
            onLogin = authViewModel::login,
            isLoggingIn = authState.isLoggingIn,
            modifier = modifier)
        return
    }

    val stagger = rememberStaggerState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        MillenniumIcon(
            imageVector = FeatherIcons.AlertCircle,
            contentDescription = null,
            modifier = Modifier
                .staggeredEntrance(stagger, 0)
                .pulseRings(AppTheme.colors.error, rings = 2, periodMillis = 3000)
                .size(64.dp),
            tint = AppTheme.colors.error.copy(alpha = 0.7f))

        Text(
            text = stringResource(Res.string.error_title),
            modifier = Modifier.staggeredEntrance(stagger, 1),
            style = AppTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center)

        Text(
            text = stringResource(Res.string.error_try_again),
            modifier = Modifier.staggeredEntrance(stagger, 2),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center)

        RetryButton(onRetry)
    }
}

/** Shown instead of an error or a sign-in prompt while the network can't be reached. */
@Composable
fun OfflineDisplay(
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    connection: ConnectionViewModel = koinViewModel()) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MillenniumIcon(
            imageVector = FeatherIcons.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = AppTheme.colors.onSurfaceVariant)

        Text(
            text = stringResource(Res.string.offline_title),
            style = AppTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center)

        Text(
            text = stringResource(Res.string.offline_description),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center)

        if (onRetry != null) RetryButton(onRetry)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MillenniumButton(onClick = connection::openDownloads, style = MillenniumButtonStyle.Secondary) {
                Text(stringResource(Res.string.offline_downloads))
            }
            MillenniumButton(onClick = connection::openLocalTracks, style = MillenniumButtonStyle.Secondary) {
                Text(stringResource(Res.string.offline_local))
            }
        }
    }
}

@Composable
private fun RetryButton(onRetry: () -> Unit) {
    MushiButton(onClick = onRetry, modifier = Modifier.height(40.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MillenniumIcon(
                imageVector = FeatherIcons.RefreshCw,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp).size(18.dp))
            Text(stringResource(Res.string.common_retry))
        }
    }
}
