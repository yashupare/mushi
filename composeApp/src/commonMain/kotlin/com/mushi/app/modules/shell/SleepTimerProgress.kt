package com.mushi.app.modules.shell

import kotlinx.coroutines.flow.MutableStateFlow

/** How far the sleep timer has run (0 to 1), or null while none runs. The Home sky sets with it. */
class SleepTimerProgress {
    val fraction = MutableStateFlow<Float?>(null)
}
