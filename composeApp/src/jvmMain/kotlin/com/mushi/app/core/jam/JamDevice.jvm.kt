package com.mushi.app.core.jam

import java.util.Locale
import java.util.TimeZone

internal actual fun jamDeviceTraits(): List<String> = listOf(
    "desktop",
    System.getProperty("os.name").orEmpty(),
    System.getProperty("os.version").orEmpty(),
    System.getProperty("os.arch").orEmpty(),
    Runtime.getRuntime().availableProcessors().toString(),
    Locale.getDefault().toLanguageTag(),
    TimeZone.getDefault().id)
