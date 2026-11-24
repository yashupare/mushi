package com.mushi.app

enum class PlatformType {
    Android, IOS, Windows, Linux, MacOS, Unknown
}

interface Platform {
    val name: String
    val type: PlatformType
}

expect fun getPlatform(): Platform

expect fun openUrlInBrowser(url: String)

fun Platform.isDesktop(): Boolean {
    return type == PlatformType.Windows ||
            type == PlatformType.Linux ||
            type == PlatformType.MacOS
}

fun Platform.isMobile(): Boolean {
    return type == PlatformType.Android ||
            type == PlatformType.IOS
}