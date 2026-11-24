package org.spotube.plugin_interfaces.host_apis

import app.cash.zipline.ZiplineService

const val SystemInformationAPI_SERVICE_NAME = "SystemInformationAPI"

interface SystemInformationAPI : ZiplineService {
    /**
     * ISO 8601 format, e.g. "America/New_York"
     */
    fun getTimeZone(): String

    /**
     * ISO 639-1 language code, e.g. "en"
     */
    fun getLocale(): String

    /**
     * Operating system name, e.g. "Windows", "macOS", "Linux", "iOS", "Android"
     */
    fun getOperatingSystem(): String

    /**
     * Application version, e.g. "1.0.0"
     */
    fun getAppVersion(): String
}