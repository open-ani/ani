package me.him188.ani.datasources.api.source

/**
 * @see MediaSource.checkConnection
 */
enum class ConnectionStatus {
    SUCCESS,
    FAILED,
}

fun Boolean.toConnectionStatus() = if (this) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
