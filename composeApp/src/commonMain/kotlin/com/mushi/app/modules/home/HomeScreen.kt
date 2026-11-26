package com.mushi.app.modules.home


import com.mushi.app.core.ui.listItemMotion
import com.mushi.app.core.ui.rememberStaggerState
import com.mushi.app.core.ui.staggeredEntrance
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumIconButton
import org.spotube.millennium.composables.MillenniumIconButtonStyle
import org.spotube.millennium.composables.MillenniumButton
import org.spotube.millennium.composables.MillenniumButtonStyle
import org.spotube.millennium.theme.MillenniumTheme
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import com.mushi.app.core.ui.theming.AppTheme
import com.composeunstyled.Text
import org.spotube.millennium.composables.MillenniumPullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.currentWindowWidthBreakpoint
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseGenre
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseItem
import org.spotube.plugin_interfaces.plugin_apis.metadata.browse.MetadataBrowseSection
import com.mushi.app.PlatformType
import com.mushi.app.core.ui.component.AlbumCard
import com.mushi.app.core.ui.component.ApplicationMainBar
import com.mushi.app.core.ui.component.ArtistCard
import com.mushi.app.core.ui.component.ErrorDisplay
import com.mushi.app.core.ui.component.PlaylistCard
import com.mushi.app.core.ui.component.TrackCard
import com.mushi.app.core.ui.component.UserCard
import com.mushi.app.core.ui.component.FeaturedCarousel
import com.mushi.app.core.ui.component.VerticalScrollbar
import com.mushi.app.core.ui.component.cards.PlayableCard
import com.mushi.app.core.ui.component.dragScrollable
import com.mushi.app.core.ui.misc.SkeletonTree
import com.mushi.app.core.navigation.NavigationCommands
import com.mushi.app.core.navigation.Routes
import com.mushi.app.modules.shell.ConnectStatusBanner
import com.mushi.app.resources.iconsax.IconsaxSoundTwotone
import com.mushi.app.getPlatform
import com.mushi.app.modules.shell.LocalAppShellBottomInset
import com.mushi.app.modules.support.LocalSupportPrompt
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxHeart2
import com.mushi.app.resources.iconsax.IconsaxMirroringScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.support_button_label
import com.mushi.app.generated.resources.devices_title
import com.mushi.app.generated.resources.home_title_browse
import com.mushi.app.generated.resources.jam_screen_title
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.Font
import org.spotube.millennium.adaptive.WindowBreakpoints
import com.mushi.app.resources.iconsax.IconsaxMirrorScreenRegular
import com.mushi.app.generated.resources.cookie_regular
import com.mushi.app.isDesktop

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel) {
    val navigationCommands = koinInject<NavigationCommands>()
    val openSupport = LocalSupportPrompt.current
    val isSmallScreen = currentWindowWidthBreakpoint() isBelow WindowBreakpoints.Medium
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val greeting = stringResource(rememberGreeting())

    val selectedGenreId = when (val s = state) {
        is HomeScreenState.Data -> s.selectedGenreId
        else -> null
    }

    LaunchedEffect(selectedGenreId) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItems = layoutInfo.totalItemsCount
                Pair(lastVisibleIndex, totalItems)
            }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                if (
                    lastVisibleIndex != null &&
                    totalItems > 0 &&
                    lastVisibleIndex >= totalItems - 2
                ) {
                    viewModel.loadMoreData()
                }
            }
    }

    // A wash of the first cover's colour behind the greeting, fading out as the page scrolls.
    val washSource = (state as? HomeScreenState.Data)?.featuredItems
        ?.firstNotNullOfOrNull { it.asQuickPick()?.imageUrl }
    val artTint = com.mushi.app.core.ui.theming.rememberArtworkTint(washSource)
    val pageBackground = MillenniumTheme.colors.background
    val darkPage = pageBackground.luminance() < 0.5f
    val washColor by androidx.compose.animation.animateColorAsState(
        targetValue = artTint
            ?.let { androidx.compose.ui.graphics.lerp(pageBackground, it, if (darkPage) 0.55f else 0.2f) }
            ?: pageBackground,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 700),
        label = "home_wash")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MillenniumTheme.colors.background)
            .drawBehind {
                val scrolled = if (listState.firstVisibleItemIndex == 0) {
                    listState.firstVisibleItemScrollOffset
                } else {
                    Int.MAX_VALUE
                }
                val visible = (1f - scrolled / 900f).coerceIn(0f, 1f)
                if (visible > 0f) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(washColor, androidx.compose.ui.graphics.Color.Transparent),
                            endY = size.height * 0.55f),
                        alpha = visible)
                }
            }
    ) {
        val headerActions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
            if (isSmallScreen) {
                // Devices and Group Jam now live at the top of Settings.
                val supportLabel = stringResource(Res.string.support_button_label)
                if (com.mushi.app.core.AppEdition.UPSTREAM_LINKS_ENABLED) {
                    MillenniumIconButton(
                        onClick = openSupport,
                        style = MillenniumIconButtonStyle.Quiet
                    ) {
                        MillenniumIcon(
                            imageVector = Iconsax.IconsaxHeart2,
                            contentDescription = supportLabel
                        )
                    }
                }
                com.mushi.app.core.ui.component.ProfileButton()
            }
        }
        if (isSmallScreen && !getPlatform().isDesktop()) {
            com.mushi.app.core.ui.component.PageHeader(title = greeting, actions = headerActions)
        } else {
            ApplicationMainBar(
                backButton = false,
                title = { Text(greeting) },
                actions = headerActions)
        }
        if (isSmallScreen) {
            ConnectStatusBanner(
                onOpenRemote = { navigationCommands.navigateTo(Routes.RemoteControl) },
                onOpenJam = { navigationCommands.navigateTo(Routes.Jam) }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (!isSmallScreen) {
                HomeContent(
                    listState = listState,
                    state = state,
                    onRetry = { viewModel.refresh() },
                    onGenreSelected = { viewModel.selectGenre(it) })
            } else {
                com.mushi.app.core.ui.component.MushiPullToRefreshBox(
                    // The first load shows the skeleton, so the logo only shows while pulling.
                    isRefreshing = false,
                    onRefresh = {
                        viewModel.refresh()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeContent(
                        listState = listState,
                        state = state,
                        onRetry = { viewModel.refresh() },
                        onGenreSelected = { viewModel.selectGenre(it) })
                }
            }

            VerticalScrollbar(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun HomeContent(
    listState: androidx.compose.foundation.lazy.LazyListState,
    state: HomeScreenState,
    onRetry: () -> Unit,
    onGenreSelected: (String) -> Unit
) {
    val shellBottomInset = LocalAppShellBottomInset.current
    val contentPadding = remember(shellBottomInset) {
        PaddingValues(top = 16.dp, bottom = 16.dp + shellBottomInset)
    }

    // Content rises in block by block when it first arrives and again for each genre.
    val dataState = state as? HomeScreenState.Data
    val stagger = rememberStaggerState(key = (state is HomeScreenState.Loading) to dataState?.selectedGenreId)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        when (state) {
            is HomeScreenState.Loading -> {
                item { HomeSkeleton() }
            }

            is HomeScreenState.Error -> {
                item {
                    ErrorDisplay(
                        errorMessage = state.message,
                        onRetry = onRetry
                    )
                }
            }

            is HomeScreenState.Data -> {
                val quickPicks = state.quickPicks
                val shelves = state.shelves

                if (state.genres.isNotEmpty()) {
                    item {
                        Box(Modifier.staggeredEntrance(stagger, 0)) {
                        GenreTabs(
                            genres = state.genres,
                            selectedGenreId = state.selectedGenreId,
                            onGenreSelected = onGenreSelected
                        )
                        }
                    }
                }

                if (quickPicks.isNotEmpty()) {
                    item {
                        val nav = koinInject<NavigationCommands>()
                        Box(Modifier.staggeredEntrance(stagger, 1)) {
                        QuickPicksGrid(
                            picks = quickPicks,
                            onClick = { picked ->
                                when (picked) {
                                    is MetadataBrowseItem.Album -> nav.navigateTo(Routes.Album(picked.data.id))
                                    is MetadataBrowseItem.Playlist -> nav.navigateTo(Routes.Playlist(picked.data.id))
                                    is MetadataBrowseItem.Artist -> nav.navigateTo(Routes.Artist(picked.data.id))
                                    else -> Unit
                                }
                            })
                        }
                    }
                }

                if (state.featuredItems.isNotEmpty()) {
                    item {
                        Box(Modifier.staggeredEntrance(stagger, 2)) {
                            FeaturedCarousel(items = state.featuredItems)
                        }
                    }
                }

                if (shelves == null) {
                    item { HomeBodySkeleton() }
                } else {
                    // Keyed per genre, so a new genre's shelves are new items that rise in.
                    itemsIndexed(shelves, key = { index, _ -> "shelf-${state.selectedGenreId}-$index" }) { index, section ->
                        HomeSection(
                            title = section.title,
                            subtitle = section.description,
                            items = section.items,
                            modifier = Modifier
                                .then(listItemMotion())
                                .staggeredEntrance(stagger, 3 + index)
                        )
                    }
                }

                if (state is HomeScreenState.Data.LoadingMore) {
                    item { ShelfSkeleton() }
                }
            }
        }

    }
}

@Composable
private fun HomeSection(
    title: String,
    subtitle: String? = null,
    items: List<MetadataBrowseItem>,
    modifier: Modifier = Modifier
) {
    val rowState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!subtitle.isNullOrEmpty()) Text(
                text = subtitle,
                style = AppTheme.typography.labelMedium.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = title,
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyRow(
            state = rowState,
            modifier = Modifier.dragScrollable(rowState),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            items(items) { browseItem ->
                when (browseItem) {
                    is MetadataBrowseItem.Album -> AlbumCard(album = browseItem.data)
                    is MetadataBrowseItem.Artist -> ArtistCard(artist = browseItem.data)
                    is MetadataBrowseItem.Playlist -> PlaylistCard(playlist = browseItem.data)
                    is MetadataBrowseItem.Track -> TrackCard(track = browseItem.data)
                    is MetadataBrowseItem.User -> UserCard(user = browseItem.data)
                }
            }
        }
    }
}

@Composable
private fun GenreTabs(
    genres: List<MetadataBrowseGenre>,
    selectedGenreId: String?,
    onGenreSelected: (String) -> Unit
) {
    val rowState = rememberLazyListState()

    LazyRow(
        state = rowState,
        modifier = Modifier.dragScrollable(rowState),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(genres) { genre ->
            com.mushi.app.core.ui.component.MushiFilterChip(
                label = genre.name,
                selected = selectedGenreId == genre.id,
                onClick = { onGenreSelected(genre.id) })
        }
    }
}
