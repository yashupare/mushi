package com.mushi.app.core.ui.component.cards

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.User

@Composable
fun AvatarListTile(
    title: String,
    subtitle: String? = null,
    imageURL: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isBlacklisted: Boolean = false
) {
    PlayableListTile(
        title = title,
        subtitle = subtitle,
        imageURL = imageURL,
        onClick = onClick,
        modifier = modifier,
        imageShape = CircleShape,
        placeholderIcon = com.mushi.app.core.ui.theming.MushiIcons.User,
        isBlacklisted = isBlacklisted
    )
}
