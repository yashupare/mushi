package com.mushi.app.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.mushi.app.core.network.HttpClientFactory
import com.mushi.app.modules.update.AppInstaller
import com.mushi.app.modules.update.UpdatePackage
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** Downloads the release APK and installs it over this app with [PackageInstaller]. */
class AndroidAppInstaller(
    private val context: Context,
    private val httpClientFactory: HttpClientFactory) : AppInstaller {

    override val supported = true

    override val failures: Flow<Unit> = installFailures

    override fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    override fun openInstallPermission() {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override suspend fun download(update: UpdatePackage, onProgress: (Float) -> Unit): String =
        withContext(Dispatchers.IO) {
            require(update.url.startsWith("https://")) { "Updates are only fetched over https" }
            val folder = File(context.cacheDir, "updates")
            folder.deleteRecursively()
            folder.mkdirs()
            val file = File(folder, "update.apk")
            val sha = MessageDigest.getInstance("SHA-256")

            httpClientFactory.default.prepareGet(update.url).execute { response ->
                check(response.status.isSuccess()) { "Download refused: ${response.status.value}" }
                val total = response.contentLength() ?: update.sizeBytes
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(64 * 1024)
                var done = 0L
                var reported = 0
                file.outputStream().use { out ->
                    while (true) {
                        val count = channel.readAvailable(buffer, 0, buffer.size)
                        if (count < 0) break
                        out.write(buffer, 0, count)
                        sha.update(buffer, 0, count)
                        done += count
                        val percent = if (total > 0) (done * 100 / total).toInt() else 0
                        if (percent != reported) {
                            reported = percent
                            onProgress(percent / 100f)
                        }
                    }
                }
            }
            verify(file, update, sha.digest().joinToString("") { "%02x".format(it) })
            file.absolutePath
        }

    /** Android itself refuses a different signature; this catches a broken or wrong file early. */
    private fun verify(file: File, update: UpdatePackage, sha256: String) {
        if (update.sizeBytes > 0) check(file.length() == update.sizeBytes) { "Incomplete download" }
        update.digest?.takeIf { it.startsWith("sha256:") }?.let {
            check(it.removePrefix("sha256:").equals(sha256, ignoreCase = true)) { "Checksum differs" }
        }
        val archive: PackageInfo = checkNotNull(
            context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)) { "Not an app package" }
        check(archive.packageName == context.packageName) { "The package is for another app" }
    }

    override suspend fun install(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)
        params.setSize(file.length())
        // An app updating itself may skip the confirmation from Android 12 on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                session.openWrite("mushi.apk", 0, file.length()).use { out ->
                    file.inputStream().use { it.copyTo(out) }
                    session.fsync(out)
                }
                markUpdatingItself(context)
                val result = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    Intent(context, InstallResultReceiver::class.java),
                    // The system fills the result into the intent
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                session.commit(result.intentSender)
            }
        } catch (e: Exception) {
            runCatching { installer.abandonSession(sessionId) }
            throw e
        }
    }

    internal companion object {
        val installFailures = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }
}
