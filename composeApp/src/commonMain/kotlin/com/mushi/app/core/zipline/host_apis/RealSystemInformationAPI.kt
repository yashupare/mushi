package com.mushi.app.core.zipline.host_apis

import org.spotube.plugin_interfaces.host_apis.SystemInformationAPI
import com.mushi.app.getPlatform
import kotlinx.datetime.TimeZone

class RealSystemInformationAPI: SystemInformationAPI {
    override fun getTimeZone(): String {
        val tz = TimeZone.currentSystemDefault()
        return tz.id
    }

    override fun getLocale(): String {
        return "en-US" // TODO: Implement locale retrieval
    }

    override fun getOperatingSystem(): String {
        val platform = getPlatform()
        return platform.name
    }

    override fun getAppVersion(): String {
        return "1.0.0"
    }
}