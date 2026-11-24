package org.spotube.plugin_interfaces.extras.logger

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Logger(tag: String) {
    fun d(message: () -> String)
    fun i(message: () -> String)
    fun w(message: () -> String)
    fun e(message: () -> String, throwable: Throwable? = null)
}