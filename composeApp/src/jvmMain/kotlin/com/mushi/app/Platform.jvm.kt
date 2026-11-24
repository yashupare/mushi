package com.mushi.app

import java.awt.Desktop
import java.net.URI

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val type: PlatformType = System.getProperty("os.name").let { osName ->
        when {
            osName.contains("win", ignoreCase = true) -> PlatformType.Windows
            osName.contains("mac", ignoreCase = true) -> PlatformType.MacOS
            osName.contains("nix", ignoreCase = true) || osName.contains(
                "nux",
                ignoreCase = true
            ) || osName.contains("aix", ignoreCase = true) -> PlatformType.Linux

            else -> PlatformType.Unknown
        }
    }
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun openUrlInBrowser(url: String) {
    // The app runs on a native (Tao) window, not an AWT frame, so java.awt.Desktop is not a
    // reliable way to hand a URL to the OS. Prefer the platform's own opener process and only
    // fall back to AWT when no opener is available on PATH.
    if (openWithSystemOpener(url)) return
    runCatching { Desktop.getDesktop().browse(URI(url)) }
}

private fun openWithSystemOpener(url: String): Boolean {
    val osName = System.getProperty("os.name").lowercase()
    val command = when {
        osName.contains("win") -> listOf("rundll32", "url.dll,FileProtocolHandler", url)
        osName.contains("mac") -> listOf("open", url)
        osName.contains("nux") || osName.contains("nix") || osName.contains("aix") ->
            listOf("xdg-open", url)
        else -> return false
    }
    return runCatching {
        ProcessBuilder(command).start()
        true
    }.getOrDefault(false)
}