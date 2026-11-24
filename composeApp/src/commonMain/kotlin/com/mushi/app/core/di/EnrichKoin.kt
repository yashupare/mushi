package com.mushi.app.core.di

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

inline fun <reified T> KoinComponent.injectLogger(): Lazy<Logger> {
    return inject { parametersOf(T::class.simpleName ?: "Unknown") }
}

@Composable
inline fun <reified T : Any> rememberLogger(): Logger {
    val tag = T::class.simpleName ?: "Unknown"
    return koinInject { parametersOf(tag) }
}

@Composable
fun rememberLogger(tag: String): Logger {
    return koinInject { parametersOf(tag) }
}