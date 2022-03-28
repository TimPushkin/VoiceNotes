package me.timpushkin.voicenotes.utils

import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class CountingTimer {
    private var timer: Timer? = null
    private var time = 0L
    private var period = 500L
    private var callback: (Long) -> Unit = {}

    fun start(updatePeriodMs: Long = 500L, onUpdate: (Long) -> Unit = {}) {
        time = 0
        period = updatePeriodMs
        callback = onUpdate
        `continue`()
    }

    fun pause() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

    fun `continue`() {
        timer = Timer().apply {
            scheduleAtFixedRate(0, period) {
                callback(time)
                time += period
            }
        }
    }
}
