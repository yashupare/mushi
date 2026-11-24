package com.mushi.app.core.extras

fun String.kebabToTitleCase(): String {
    return this.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}