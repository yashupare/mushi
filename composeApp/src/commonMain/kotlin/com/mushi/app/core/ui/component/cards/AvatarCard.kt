package com.mushi.app.core.ui.component.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

import com.mushi.app.core.ui.theming.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.misc.TextWithShimmer
import com.mushi.app.core.ui.misc.shimmerApply
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.User

@Composable
fun AvatarCard(
    title: String,
    subtitle: String? = null,
    imageURL: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isBlacklisted: Boolean = false,
    listMode: Boolean = false
) {
    if (listMode) {
        AvatarListTile(title, subtitle, imageURL, onClick, modifier, isBlacklisted)
        return
    }

    MediaCardSurface(modifier.width(160.dp), onClick, isBlacklisted = isBlacklisted) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                MediaArtwork(
                    model = imageURL,
                    title = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .shimmerApply(),
                    shape = CircleShape,
                    placeholderIcon = com.mushi.app.core.ui.theming.MushiIcons.User
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 10.dp, end = 2.dp, bottom = 4.dp)
            ) {
                TextWithShimmer(
                    text = title,
                    style = AppTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    TextWithShimmer(
                        text = subtitle,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
