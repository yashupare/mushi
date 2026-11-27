package com.mushi.app.modules.update

/** One line of release notes, ready to show. */
internal data class NoteLine(val text: String, val heading: Boolean = false)

private val LINK = Regex("""\[([^]]*)]\([^)]*\)""")

/**
 * Reduces the Markdown of a release to headings and plain lines. The note on where to download
 * the file is left out: whoever reads this in the app is already updating.
 */
internal fun releaseNoteLines(markdown: String): List<NoteLine> =
    markdown.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("**Install on Android") }
        .map { line ->
            val heading = line.startsWith('#')
            var text = if (heading) line.trimStart('#').trim() else line
            if (text.startsWith("- ") || text.startsWith("* ")) text = "• " + text.drop(2)
            NoteLine(text.replace(LINK, "$1").replace("**", "").replace("`", ""), heading)
        }
