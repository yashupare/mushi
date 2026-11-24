package com.mushi.app.core.share

import com.mushi.app.core.share.ShareService
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class IosShareService : ShareService {
    override fun share(url: String, title: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val activityViewController = UIActivityViewController(listOf(title, url, nsUrl), null)
        rootViewController.presentViewController(activityViewController, true, null)
    }
}
