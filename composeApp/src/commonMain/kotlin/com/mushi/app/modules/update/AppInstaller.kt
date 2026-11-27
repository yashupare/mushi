package com.mushi.app.modules.update

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** The installable file attached to a release. */
data class UpdatePackage(
    val url: String,
    val sizeBytes: Long,
    /** "sha256:…" as GitHub reports it, when it does. */
    val digest: String?)

/** Installs an update from inside the app, where the platform allows it. */
interface AppInstaller {
    /** False where updates are installed by hand from the releases page. */
    val supported: Boolean

    /** Installs that were refused or cancelled after [install] returned. */
    val failures: Flow<Unit>

    /** Whether the listener has allowed this app to install apps. */
    fun canInstall(): Boolean

    fun openInstallPermission()

    /** Downloads and checks the package, returning where it was saved. */
    suspend fun download(update: UpdatePackage, onProgress: (Float) -> Unit): String

    /** Hands the package to the system. A successful install closes the app. */
    suspend fun install(path: String)
}

class NoAppInstaller : AppInstaller {
    override val supported = false
    override val failures: Flow<Unit> = emptyFlow()
    override fun canInstall() = false
    override fun openInstallPermission() = Unit
    override suspend fun download(update: UpdatePackage, onProgress: (Float) -> Unit): String =
        error("Not supported")

    override suspend fun install(path: String) = error("Not supported")
}
