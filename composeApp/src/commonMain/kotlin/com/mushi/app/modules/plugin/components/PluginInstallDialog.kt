package com.mushi.app.modules.plugin.components

import com.mushi.app.core.ui.misc.AppSpinner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.composeunstyled.Text
import com.mushi.app.core.ui.component.AdaptiveDialogBottomSheet
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.plugin.GitHubRelease
import com.mushi.app.modules.plugin.GitHubRepo
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxArrowDown4
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumDropdownMenu
import org.spotube.millennium.composables.MillenniumDropdownMenuContent
import org.spotube.millennium.composables.MillenniumDropdownMenuItem
import org.spotube.millennium.composables.MillenniumIcon
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.common_cancel
import com.mushi.app.generated.resources.plugin_action_install
import com.mushi.app.generated.resources.plugin_install_no_releases
import com.mushi.app.generated.resources.plugin_install_prerelease
import com.mushi.app.generated.resources.plugin_install_release_notes
import com.mushi.app.generated.resources.plugin_install_select_release_label
import com.mushi.app.generated.resources.plugin_install_select_release_placeholder

@Composable
fun PluginInstallDialog(
    repo: GitHubRepo,
    releases: List<GitHubRelease>,
    isLoadingReleases: Boolean,
    onDismiss: () -> Unit,
    onInstall: (GitHubRelease) -> Unit) {
    var selectedRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(releases) {
        if (selectedRelease == null && releases.isNotEmpty()) {
            selectedRelease = releases.first()
        }
    }

    AdaptiveDialogBottomSheet(
        onDismiss = onDismiss,
        title = { PluginDialogTitle(stringResource(Res.string.plugin_action_install)) },
        dialogTitle = stringResource(Res.string.plugin_action_install),
        actions = {
            PluginDialogActions(
                secondaryLabel = stringResource(Res.string.common_cancel),
                onSecondary = onDismiss,
                primaryLabel = stringResource(Res.string.plugin_action_install),
                onPrimary = { selectedRelease?.let(onInstall) },
                primaryEnabled = selectedRelease != null && !isLoadingReleases)
        }) {
        PluginDialogBody {
            PluginPanel {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    val platformContext = LocalPlatformContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(platformContext)
                            .data(repo.owner.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = repo.owner.login,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            repo.fullName,
                            style = AppTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        if (!repo.description.isNullOrBlank()) {
                            Text(
                                repo.description,
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            if (isLoadingReleases) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    contentAlignment = Alignment.Center) {
                    AppSpinner(size = 28.dp)
                }
            } else if (releases.isEmpty()) {
                PluginCallout(stringResource(Res.string.plugin_install_no_releases), isWarning = true)
            } else {
                PluginSectionLabel(stringResource(Res.string.plugin_install_select_release_label))
                MillenniumDropdownMenu(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth(),
                    anchor = {
                        MillenniumButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            style = MillenniumButtonStyle.Secondary) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    selectedRelease?.tagName
                                        ?: stringResource(Res.string.plugin_install_select_release_placeholder),
                                    style = AppTheme.typography.bodyMedium)
                                MillenniumIcon(
                                    imageVector = Iconsax.IconsaxArrowDown4,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }) {
                    MillenniumDropdownMenuContent {
                        releases.forEach { release ->
                            MillenniumDropdownMenuItem(
                                label = release.tagName,
                                selected = release.tagName == selectedRelease?.tagName,
                                description = if (release.prerelease) {
                                    stringResource(Res.string.plugin_install_prerelease)
                                } else null,
                                onClick = {
                                    selectedRelease = release
                                    expanded = false
                                })
                        }
                    }
                }

                selectedRelease?.let { release ->
                    if (release.prerelease) {
                        PluginCallout(stringResource(Res.string.plugin_install_prerelease), isWarning = true)
                    }
                    if (!release.body.isNullOrBlank()) {
                        PluginSectionLabel(stringResource(Res.string.plugin_install_release_notes))
                        PluginPanel {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp)) {
                                Text(
                                    release.body,
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
