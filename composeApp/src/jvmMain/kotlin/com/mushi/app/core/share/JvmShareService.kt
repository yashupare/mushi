package com.mushi.app.core.share

import com.mushi.app.core.share.ShareService
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class JvmShareService : ShareService {
    override fun share(url: String, title: String) {
        val selection = StringSelection(url)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }
}
