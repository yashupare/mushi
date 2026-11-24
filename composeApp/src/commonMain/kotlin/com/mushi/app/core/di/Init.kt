package com.mushi.app.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        logger(
            KermitKoinLogger(Logger.withTag("koin"))
        )
        modules(
            sharedModules,
            platformModules
        )
    }

}