package com.mushi.app.core.share

import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import com.mushi.app.core.share.ShareService

class AndroidShareService(private val context: Context) : ShareService {
    override fun share(url: String, title: String) {
        // context is the application context (Koin), which can only start activities in a new task
        val chooser = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setSubject(title)
            .setText(url)
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
