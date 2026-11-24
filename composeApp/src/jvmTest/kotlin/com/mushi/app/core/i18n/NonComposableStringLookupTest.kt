package com.mushi.app.core.i18n

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The library's non-composable getters query the system theme over D-Bus on Linux on every
 * call, which crashed the desktop app (#3183). App code must use the ones in Strings.kt.
 */
class NonComposableStringLookupTest {
    @Test
    fun appCodeUsesTheAppStringLookups() {
        val forbidden = Regex("""^import org\.jetbrains\.compose\.resources\.get(String|PluralString|StringArray)\s*$""")
        val offenders = File("src").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> forbidden.matches(line) }
                    .map { (index, _) -> "${file.path}:${index + 1}" }
            }
            .toList()

        assertTrue(
            offenders.isEmpty(),
            "Import getString/getPluralString/getStringArray from com.mushi.app.core.i18n instead:\n" +
                offenders.joinToString("\n"))
    }
}
