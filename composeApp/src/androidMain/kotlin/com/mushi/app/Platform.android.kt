package com.mushi.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import org.koin.core.context.GlobalContext

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val type: PlatformType = PlatformType.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun openUrlInBrowser(url: String) {
    val context = GlobalContext.get().get<Context>()
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}