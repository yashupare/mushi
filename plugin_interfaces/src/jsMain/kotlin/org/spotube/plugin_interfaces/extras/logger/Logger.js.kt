package org.spotube.plugin_interfaces.extras.logger

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class Logger actual constructor(tag: String) {
    internal val tag = tag

    actual fun d(message: () -> String) {
        console.log("DEBUG [${this.tag}] ${message.invoke()}")
    }

    actual fun i(message: () -> String) {
        console.log("INFO [${this.tag}] ${message.invoke()}")
    }

    actual fun w(message: () -> String) {
        console.log("WARN [${this.tag}] ${message.invoke()}")
    }

    actual fun e(message: () -> String, throwable: Throwable?) {
        console.log("ERROR [${this.tag}] ${message.invoke()}", throwable)
    }
}