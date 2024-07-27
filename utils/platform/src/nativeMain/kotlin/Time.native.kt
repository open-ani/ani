package me.him188.ani.utils.platform

import kotlinx.datetime.Clock

actual fun currentTimeMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}
