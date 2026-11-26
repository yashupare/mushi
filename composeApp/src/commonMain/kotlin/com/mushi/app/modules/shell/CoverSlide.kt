package com.mushi.app.modules.shell

/**
 * Which way the cover should travel on a track change: 1 for a later track in the queue (comes in
 * from the right), -1 for an earlier one. Wrapping from the last track to the first counts as
 * going forward.
 */
internal class CoverSlideDirection {
    private var lastIndex = -1
    var direction = 1
        private set

    fun onIndex(index: Int, queueSize: Int): Int {
        if (index >= 0 && index != lastIndex) {
            val wrapped = index == 0 && lastIndex == queueSize - 1 && queueSize > 2
            direction = if (!wrapped && lastIndex >= 0 && index < lastIndex) -1 else 1
            lastIndex = index
        }
        return direction
    }
}
