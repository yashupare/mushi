package com.mushi.app.modules.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.composeunstyled.Text
import androidx.compose.runtime.Composable
import com.mushi.app.generated.resources.search_category_telugu
import com.mushi.app.generated.resources.search_category_tamil
import com.mushi.app.generated.resources.search_category_retro
import com.mushi.app.generated.resources.search_category_indie
import com.mushi.app.generated.resources.search_category_devotional
import com.mushi.app.generated.resources.search_category_focus
import com.mushi.app.generated.resources.search_category_party
import com.mushi.app.generated.resources.search_category_chill
import com.mushi.app.generated.resources.search_category_workout
import com.mushi.app.generated.resources.search_category_romance
import com.mushi.app.generated.resources.search_category_punjabi
import com.mushi.app.generated.resources.search_category_bollywood
import com.mushi.app.generated.resources.search_category_hiphop
import com.mushi.app.generated.resources.search_category_pop
import com.mushi.app.generated.resources.search_browse_all
import org.jetbrains.compose.resources.StringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumText
import com.mushi.app.core.ui.pressScale
import com.mushi.app.core.ui.theming.AppTheme
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCloseSquare
import com.mushi.app.resources.iconsax.InconsaxClock
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.search_recent_clear
import com.mushi.app.generated.resources.search_recent_remove
import com.mushi.app.generated.resources.search_recent_title

/**
 * What Search shows before anything is typed: the recent searches as a plain list under a
 * "Recent Searches" header with a Clear action, like Apple Music. Tap a row to run it again.
 */
@Composable
internal fun RecentSearchesList(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            bottom = LocalAppShellBottomInset.current)
    ) {
        if (searches.isNotEmpty()) item(key = "recent_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.search_recent_title),
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
                )
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clickable(onClick = onClearAll)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MillenniumText(
                        text = stringResource(Res.string.search_recent_clear),
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
        // Only the latest few, so the browse tiles below stay in reach.
        if (searches.isNotEmpty()) items(searches.take(5), key = { it }) { search ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .pressScale(0.985f)
                    .clickable { onSearchClick(search) }
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MillenniumIcon(
                    imageVector = com.mushi.app.core.ui.theming.MushiIcons.Clock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppTheme.colors.onSurfaceVariant
                )
                Text(
                    text = search,
                    color = AppTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                MillenniumIconButton(
                    onClick = { onRemove(search) },
                    style = MillenniumIconButtonStyle.Quiet
                ) {
                    MillenniumIcon(
                        imageVector = com.mushi.app.core.ui.theming.MushiIcons.Close,
                        contentDescription = stringResource(Res.string.search_recent_remove),
                        modifier = Modifier.size(18.dp),
                        tint = AppTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
        item(key = "browse_header") {
            Text(
                text = stringResource(Res.string.search_browse_all),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onSurface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
            )
        }
        items(BrowseCategories.chunked(2), key = { row -> row.first().first.key }) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (resource, color) ->
                    val name = stringResource(resource)
                    BrowseCategoryTile(
                        name = name,
                        color = color,
                        onClick = { onCategoryClick(name) },
                        modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

private val BrowseCategories: List<Pair<StringResource, Color>> = listOf(
    Res.string.search_category_pop to Color(0xFFE8115B),
    Res.string.search_category_hiphop to Color(0xFFBA5D07),
    Res.string.search_category_bollywood to Color(0xFF8D67AB),
    Res.string.search_category_punjabi to Color(0xFF1E3264),
    Res.string.search_category_romance to Color(0xFFDC148C),
    Res.string.search_category_workout to Color(0xFF477D95),
    Res.string.search_category_chill to Color(0xFF148A08),
    Res.string.search_category_party to Color(0xFF8C1932),
    Res.string.search_category_focus to Color(0xFF503750),
    Res.string.search_category_devotional to Color(0xFFE1118C),
    Res.string.search_category_indie to Color(0xFF509BF5),
    Res.string.search_category_retro to Color(0xFFB49BC8),
    Res.string.search_category_tamil to Color(0xFF006450),
    Res.string.search_category_telugu to Color(0xFFAF2896)
)

/** A bold, colourful category tile; tapping it searches for that category. */
@Composable
private fun BrowseCategoryTile(
    name: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(92.dp)
            .pressScale(0.96f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.72f))))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(
            text = name,
            style = AppTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart))
    }
}
