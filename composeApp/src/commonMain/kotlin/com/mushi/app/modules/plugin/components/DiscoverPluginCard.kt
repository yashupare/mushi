package com.mushi.app.modules.plugin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
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
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.millenniumPanelBrush
import org.spotube.millennium.theme.millenniumSurface
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.core.ui.theming.flatPanel
import com.mushi.app.modules.plugin.GitHubRepo
import com.mushi.app.modules.plugin.isOfficialPlugin
import com.mushi.app.modules.plugin.isVerifiedPlugin
import com.mushi.app.modules.plugin.pluginDisplayName
import com.mushi.app.openUrlInBrowser
import com.mushi.app.resources.iconsax.CarbonGithubLogo
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxAdd
import com.mushi.app.resources.iconsax.IconsaxCheckCircle
import com.mushi.app.resources.iconsax.IconsaxHeart
import com.mushi.app.resources.iconsax.User
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.plugin_discover_badge_official
import com.mushi.app.generated.resources.plugin_discover_badge_verified
import com.mushi.app.generated.resources.plugin_discover_repository_content_description
import com.mushi.app.generated.resources.plugin_discover_repository_host
import com.mushi.app.generated.resources.plugin_discover_stars_content_description
import com.mushi.app.generated.resources.plugin_section_install

/**
 * A community plugin from GitHub. A self-contained card (header, description, footer with the
 * install action) so it reads the same in a one-column phone list and a two-column grid.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DiscoverPluginCard(
    repo: GitHubRepo,
    isInstalling: Boolean,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.onSurface.copy(alpha = 0.07f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PluginIconWell(size = 44.dp) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(repo.owner.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = repo.owner.login,
                    modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically) {
                    Text(
                        repo.pluginDisplayName,
                        style = AppTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    when {
                        repo.isOfficialPlugin -> GelBadge(
                            stringResource(Res.string.plugin_discover_badge_official),
                            icon = Iconsax.IconsaxCheckCircle)
                        repo.isVerifiedPlugin -> OutlineTag(
                            stringResource(Res.string.plugin_discover_badge_verified),
                            icon = Iconsax.IconsaxCheckCircle)
                    }
                }
                MetaLabel(repo.owner.login, icon = com.mushi.app.core.ui.theming.MushiIcons.User)
            }
        }

        if (!repo.description.isNullOrBlank()) {
            Text(
                repo.description,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
        }

        // Pushes the footer down so cards sharing a grid row keep their buttons aligned.
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetaLabel(
                repo.stargazersCount.toString(),
                icon = Iconsax.IconsaxHeart,
                iconDescription = stringResource(Res.string.plugin_discover_stars_content_description))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { openUrlInBrowser(repo.htmlUrl) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MillenniumIcon(
                    Iconsax.CarbonGithubLogo,
                    contentDescription = stringResource(Res.string.plugin_discover_repository_content_description),
                    modifier = Modifier.size(12.dp),
                    tint = AppTheme.colors.onSurfaceVariant)
                Text(
                    stringResource(Res.string.plugin_discover_repository_host),
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.primary,
                    maxLines = 1)
            }
            Spacer(modifier = Modifier.weight(1f))
            MillenniumButton(
                onClick = onInstall,
                loading = isInstalling,
                style = MillenniumButtonStyle.Secondary) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MillenniumIcon(com.mushi.app.core.ui.theming.MushiIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(stringResource(Res.string.plugin_section_install))
                }
            }
        }
    }
}
