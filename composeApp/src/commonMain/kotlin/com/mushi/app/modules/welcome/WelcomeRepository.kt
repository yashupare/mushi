package com.mushi.app.modules.welcome

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mushi.app.core.AppEdition
import com.mushi.app.core.DebugConfig
import com.mushi.app.core.db.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns whether the Welcome/Onboarding flow should be the app's start destination.
 *
 * This lives in its own preference rather than [com.mushi.app.modules.settings.UserSettings]
 * so the very first launch can be distinguished from "settings not read yet". It is a Koin
 * singleton so the app-level start route and the routed [WelcomeScreen] observe the same state.
 */
class WelcomeRepository(private val database: Database) {
    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Set when onboarding is finished for this process only; does not persist (debug forcing). */
    private val sessionDismissed = MutableStateFlow(false)

    /** Synchronous signal, available before the persisted flag loads; true on upgrades. */
    val isExistingInstall: Boolean get() = database.isExistingInstall

    private val onboardingCompleted: StateFlow<Boolean?> = database.settingsDataStore.data
        .map { prefs -> prefs[ONBOARDING_COMPLETED_KEY] ?: database.isExistingInstall }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Whether the app should start on the Welcome/Onboarding route instead of the main shell.
     *
     * - `true` on a fresh install, or whenever [DebugConfig.FORCE_SHOW_ONBOARDING] is enabled.
     * - `false` on an existing install (upgrade) or once onboarding is completed or skipped,
     *   including within a forced session.
     *
     * The initial value is derived from [isExistingInstall] so the start route can be chosen on
     * the very first frame without flashing the wrong screen.
     */
    val shouldShowOnboarding: StateFlow<Boolean> = combine(
        onboardingCompleted,
        sessionDismissed) { completed, dismissed ->
        when {
            // The onboarding walks through plugin setup, which this build does for the user.
            !AppEdition.PLUGIN_MANAGEMENT_ENABLED -> false
            dismissed -> false
            DebugConfig.FORCE_SHOW_ONBOARDING -> true
            else -> !(completed ?: database.isExistingInstall)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = if (DebugConfig.FORCE_SHOW_ONBOARDING) true else !database.isExistingInstall)

    init {
        // One-time migration. When the flag was never written this is either a brand-new install
        // (show onboarding) or an upgrade of an existing install (skip it). Persisting the
        // decision also stops the fresh install's own data writes from being mistaken for an
        // upgrade on the next launch.
        scope.launch {
            database.settingsDataStore.edit { prefs ->
                if (ONBOARDING_COMPLETED_KEY !in prefs) {
                    prefs[ONBOARDING_COMPLETED_KEY] = database.isExistingInstall
                }
            }
        }
    }

    suspend fun completeOnboarding() {
        sessionDismissed.value = true
        database.settingsDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    /** Development helper to surface the welcome screen again on the next launch. */
    suspend fun resetOnboarding() {
        database.settingsDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = false
        }
    }
}
