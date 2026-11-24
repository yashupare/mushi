package com.mushi.app.modules.shell

import kotlin.time.Duration

internal fun formatPlayerTime(duration: Duration): String {
    return duration.toComponents { hours, minutes, seconds, _ ->
        val pMin = minutes.toString().padStart(2, '0')
        val pSec = seconds.toString().padStart(2, '0')

        if (hours > 0) {
            "${hours.toString().padStart(2, '0')}:$pMin:$pSec"
        } else {
            "$pMin:$pSec"
        }
    }
}