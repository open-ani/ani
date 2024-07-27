package me.him188.ani.utils.coroutines

internal actual fun hashException(e: Throwable): Long = e.hashCode().toLong()
