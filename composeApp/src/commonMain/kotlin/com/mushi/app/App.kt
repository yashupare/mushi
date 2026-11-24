package com.mushi.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.NavDisplay
import com.mushi.app.core.DebugConfig
import com.mushi.app.core.i18n.AppLocaleProvider
import com.mushi.app.core.navigation.Navigator
import com.mushi.app.core.navigation.Routes
import com.mushi.app.core.navigation.TOP_LEVEL_ROUTES
import com.mushi.app.core.navigation.navPopTransitionSpec
import com.mushi.app.core.navigation.navPredictivePopTransitionSpec
import com.mushi.app.core.navigation.navPushTransitionSpec
import com.mushi.app.core.navigation.rememberNavigationState
import com.mushi.app.core.navigation.toEntries
import com.mushi.app.core.ui.component.LocalSharedTransitionScope
import com.mushi.app.core.ui.theming.MushiTheme
import com.mushi.app.modules.library.LibraryTab
import com.mushi.app.modules.settings.SettingsRepository
import com.mushi.app.modules.settings.UserSettings
import com.mushi.app.modules.shell.AppShell
import com.mushi.app.modules.support.LocalSupportPrompt
import com.mushi.app.modules.support.SupportDialog
import com.mushi.app.modules.support.SupportReminderIntervalMillis
import com.mushi.app.modules.update.AppUpdateDialog
import com.mushi.app.modules.update.AppUpdateService
import com.mushi.app.modules.plugin.components.PluginPermissionDialog
import com.mushi.app.modules.update.PluginUpdateDialog
import com.mushi.app.modules.update.PluginUpdateService
import com.mushi.app.modules.webview.WebViewScreen
import com.mushi.app.modules.welcome.WelcomeRepository
import com.mushi.app.resources.iconsax.Iconsax
import com.mushi.app.resources.iconsax.IconsaxCd
import com.mushi.app.resources.iconsax.IconsaxDirectboxReceive
import com.mushi.app.resources.iconsax.IconsaxHome
import com.mushi.app.resources.iconsax.IconsaxMirrorScreenRegular
import com.mushi.app.resources.iconsax.IconsaxMusicDashboard
import com.mushi.app.resources.iconsax.IconsaxMusicLibrary
import com.mushi.app.resources.iconsax.IconsaxSearch
import com.mushi.app.resources.iconsax.IconsaxSetting2
import com.mushi.app.resources.iconsax.IconsaxSoundTwotone
import com.mushi.app.resources.iconsax.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Clock
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.jetbrains.compose.resources.StringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.jam_screen_title
import com.mushi.app.generated.resources.library_tab_albums
import com.mushi.app.generated.resources.library_tab_artists
import com.mushi.app.generated.resources.library_tab_downloads
import com.mushi.app.generated.resources.library_tab_local_tracks
import com.mushi.app.generated.resources.library_tab_playlists
import com.mushi.app.generated.resources.library_toolbar_compact_title
import com.mushi.app.generated.resources.nav_devices
import com.mushi.app.generated.resources.nav_group_connect
import com.mushi.app.generated.resources.nav_group_on_device
import com.mushi.app.generated.resources.nav_home
import com.mushi.app.generated.resources.nav_search
import com.mushi.app.generated.resources.settings_screen_title
import com.mushi.app.core.ui.theming.MushiIcons

interface NavigationItem {
    data class Tab<T : Any>(
        val title: StringResource, val icon: ImageVector, val route: Routes,
        val data: T? = null,
        /** Shown instead of [icon] while this tab is the active one. */
        val selectedIcon: ImageVector = icon
    ) : NavigationItem

    data class Group<T : Any>(
        val title: StringResource, val items: List<Tab<T>>
    ) : NavigationItem
}

private val tabs = listOf<NavigationItem>(
    NavigationItem.Tab<Any>(Res.string.nav_home, com.mushi.app.core.ui.theming.MushiIcons.Home, Routes.Home),
    NavigationItem.Tab<Any>(Res.string.nav_search, com.mushi.app.core.ui.theming.MushiIcons.Search, Routes.Search)
)

val sidebarTabs = tabs + listOf(
    NavigationItem.Group(
        Res.string.library_toolbar_compact_title,
        items = listOf(
            NavigationItem.Tab(
                Res.string.library_tab_playlists,
                com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                Routes.Library,
                LibraryTab.Playlists
            ),
            NavigationItem.Tab(Res.string.library_tab_albums, com.mushi.app.core.ui.theming.MushiIcons.Album, Routes.Library, LibraryTab.Albums),
            NavigationItem.Tab(Res.string.library_tab_artists, com.mushi.app.core.ui.theming.MushiIcons.User, Routes.Library, LibraryTab.Artists)
        )
    ),
    NavigationItem.Group(
        Res.string.nav_group_on_device,
        items = listOf(
            NavigationItem.Tab(
                Res.string.library_tab_local_tracks,
                com.mushi.app.core.ui.theming.MushiIcons.Playlist,
                Routes.Library,
                LibraryTab.LocalTracks
            ),
            NavigationItem.Tab(
                Res.string.library_tab_downloads,
                com.mushi.app.core.ui.theming.MushiIcons.Download,
                Routes.Library,
                LibraryTab.Downloads
            )
        )
    ),
    NavigationItem.Group(
        Res.string.nav_group_connect,
        items = listOf(
            NavigationItem.Tab(Res.string.nav_devices, com.mushi.app.core.ui.theming.MushiIcons.Cast, Routes.Devices),
            NavigationItem.Tab(Res.string.jam_screen_title, com.mushi.app.core.ui.theming.MushiIcons.Waveform, Routes.Jam)
        )
    )
)
// Home, Library, then Search last. Settings is reached from the profile button on each main
// screen instead of having a tab.
val bottombarTabs = listOf<NavigationItem>(
    NavigationItem.Tab<Any>(
        Res.string.nav_home, MushiIcons.Home, Routes.Home,
        selectedIcon = MushiIcons.HomeSelected),
    NavigationItem.Tab<Any>(
        Res.string.library_toolbar_compact_title, MushiIcons.Library, Routes.Library,
        selectedIcon = MushiIcons.LibrarySelected),
    NavigationItem.Tab<Any>(
        Res.string.nav_search, MushiIcons.Search, Routes.Search,
        selectedIcon = MushiIcons.SearchSelected)
)

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App(
    content: @Composable () -> Unit = {}
) {
    val settingsRepository: SettingsRepository = koinInject<SettingsRepository>()
    val userSettings by settingsRepository.userSettings.collectAsStateWithLifecycle(initialValue = UserSettings())
    val appUpdateService: AppUpdateService = koinInject()
    val pluginUpdateService: PluginUpdateService = koinInject()
    val availableAppUpdate by appUpdateService.availableUpdate.collectAsStateWithLifecycle()
    val availablePluginUpdate by pluginUpdateService.availableUpdate.collectAsStateWithLifecycle()
    val pluginPermissionReview by pluginUpdateService.permissionReview.collectAsStateWithLifecycle()

    val welcomeRepository: WelcomeRepository = koinInject()
    val showOnboarding by welcomeRepository.shouldShowOnboarding.collectAsStateWithLifecycle()

    var showSupportDialog by remember { mutableStateOf(false) }
    var supportDialogIsReminder by remember { mutableStateOf(false) }

    // Nudge the user to support the project at most once every seven days, unless they opted
    // out in Settings. The first eligible launch after onboarding only records the baseline, so
    // the prompt never appears right after the onboarding donation page.
    LaunchedEffect(showOnboarding) {
        if (showOnboarding) return@LaunchedEffect
        // Read the persisted value rather than the collected state, whose initial value is the
        // default (0) until DataStore has emitted once.
        val current = settingsRepository.getCurrentSettings()
        val now = Clock.System.now().toEpochMilliseconds()
        // Development override: show the reminder on every launch, regardless of timing or the
        // "Support reminders" toggle.
        if (DebugConfig.FORCE_SUPPORT_REMINDER) {
            supportDialogIsReminder = true
            showSupportDialog = true
            settingsRepository.updateSettings(
                current.copy(lastSupportReminderEpochMs = now))
            return@LaunchedEffect
        }
        if (!current.supportRemindersEnabled) return@LaunchedEffect
        val last = current.lastSupportReminderEpochMs
        if (last == 0L) {
            settingsRepository.updateSettings(
                current.copy(lastSupportReminderEpochMs = now))
        } else if (now - last >= SupportReminderIntervalMillis) {
            supportDialogIsReminder = true
            showSupportDialog = true
            settingsRepository.updateSettings(
                current.copy(lastSupportReminderEpochMs = now))
        }
    }

    // The app always renders the shell + NavDisplay; onboarding is just the start destination.
    // When it is completed the destination flips to Home and the navigation state is rebuilt.
    val startRoute = if (showOnboarding) Routes.Welcome else Routes.Home

    val navigationState = rememberNavigationState(
        startRoute = startRoute, topLevelRoutes = TOP_LEVEL_ROUTES
    )
    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }

    // Below the navigation state, so switching languages keeps the current screen.
    AppLocaleProvider(userSettings.language) {
        MushiTheme(settings = userSettings) {
            CompositionLocalProvider(LocalSupportPrompt provides {
                supportDialogIsReminder = false
                showSupportDialog = true
            }) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val currentRoute =
                        navigationState.backStacks[navigationState.topLevelRoute]?.last()

                    Box(Modifier.fillMaxSize()) {
                        AppShell(navigator, navigationState) {
                            SharedTransitionLayout {
                                CompositionLocalProvider(
                                    LocalSharedTransitionScope provides this@SharedTransitionLayout
                                ) {
                                    Column {
                                        NavDisplay(
                                            modifier = Modifier.fillMaxSize(),
                                            onBack = navigator::pop,
                                            entries = navigationState.toEntries(koinEntryProvider()),
                                            transitionSpec = navPushTransitionSpec,
                                            popTransitionSpec = navPopTransitionSpec,
                                            predictivePopTransitionSpec = navPredictivePopTransitionSpec
                                        )
                                    }
                                }
                            }
                        }
                        when (currentRoute) {
                            Routes.WebView -> WebViewScreen(koinInject())
                            else -> {}
                        }
                    }
                    // Launch animation on iOS and desktop (Android uses its system splash).
                    com.mushi.app.core.ui.component.StartupIntro()
                }
                content()
                // The update checker points at the upstream project, so it stays off here.
                availableAppUpdate?.takeIf { com.mushi.app.core.AppEdition.UPSTREAM_LINKS_ENABLED }?.let { update ->
                    AppUpdateDialog(
                        update = update,
                        onDismiss = appUpdateService::dismissUpdate,
                        onIgnore = appUpdateService::ignoreUpdate,
                        onUpdate = {
                            runCatching { openUrlInBrowser(AppBuildInfo.REPOSITORY_URL + "/releases") }
                            appUpdateService.dismissUpdate()
                        })
                }
                // The bundled plugins are versioned with the app, so users aren't offered updates.
                availablePluginUpdate?.takeIf { com.mushi.app.core.AppEdition.PLUGIN_MANAGEMENT_ENABLED }?.let { update ->
                    PluginUpdateDialog(
                        update = update,
                        onDismiss = pluginUpdateService::dismissUpdate,
                        onIgnore = pluginUpdateService::ignoreUpdate,
                        onUpdate = pluginUpdateService::installUpdate)
                }
                pluginPermissionReview?.takeIf { com.mushi.app.core.AppEdition.PLUGIN_MANAGEMENT_ENABLED }?.let { review ->
                    PluginPermissionDialog(
                        pluginInfo = review.pending.entry,
                        title = review.pending.title,
                        message = review.errorMessage ?: review.pending.message,
                        confirmLabel = review.pending.confirmLabel?.takeIf { review.canConfirm },
                        existingPlugin = review.pending.existingEntry,
                        permissions = review.pending.permissions,
                        logoPath = review.logoPath,
                        onConfirm = if (review.canConfirm) pluginUpdateService::confirmPermissionReview else null,
                        isConfirming = review.isInstalling,
                        onDismiss = pluginUpdateService::dismissPermissionReview)
                }
                SupportDialog(
                    visible = showSupportDialog && com.mushi.app.core.AppEdition.UPSTREAM_LINKS_ENABLED,
                    onDismissRequest = { showSupportDialog = false },
                    isReminder = supportDialogIsReminder)
            }
        }
    }
}
