package com.mushi.app.core.jam

import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.localTimeZone
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice

internal actual fun jamDeviceTraits(): List<String> {
    val device = UIDevice.currentDevice
    return listOf(
        "ios",
        device.model,
        device.systemName(),
        device.systemVersion,
        NSLocale.currentLocale.localeIdentifier,
        NSTimeZone.localTimeZone.name)
}
