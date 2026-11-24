package com.mushi.app.modules.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import com.composeunstyled.Text
import com.mushi.app.core.ui.theming.AppTheme

/**
 * Renders a track's artists on a single, ellipsized line. Artists that have an [PlayerArtist.id]
 * are tappable links that open their artist page; local tracks (no ids) render as plain text.
 */
@Composable
internal fun PlayerArtistsText(
    artists: List<PlayerArtist>,
    onArtistClick: (String) -> Unit,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified) {
    if (artists.isEmpty()) return
    val hoverColor = AppTheme.colors.primary
    val annotatedArtists = buildAnnotatedString {
        artists.forEachIndexed { index, artist ->
            if (index > 0) append(", ")
            val artistId = artist.id
            if (artistId == null) {
                append(artist.name)
            } else {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = artistId,
                        styles = TextLinkStyles(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            hoveredStyle = SpanStyle(
                                color = hoverColor,
                                textDecoration = TextDecoration.Underline),
                        ),
                        linkInteractionListener = { onArtistClick(artistId) },
                    )
                ) {
                    append(artist.name)
                }
            }
        }
    }
    Text(
        text = annotatedArtists,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
