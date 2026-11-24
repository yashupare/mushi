package com.mushi.app.core.jam

import android.content.res.Resources
import android.os.Build
import java.util.Locale
import java.util.TimeZone

internal actual fun jamDeviceTraits(): List<String> {
    val display = Resources.getSystem().displayMetrics
    return listOf(
        "android",
        Build.MANUFACTURER,
        Build.MODEL,
        Build.DEVICE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT.toString(),
        "${minOf(display.widthPixels, display.heightPixels)}x${maxOf(display.widthPixels, display.heightPixels)}@${display.densityDpi}",
        Locale.getDefault().toLanguageTag(),
        TimeZone.getDefault().id)
}
