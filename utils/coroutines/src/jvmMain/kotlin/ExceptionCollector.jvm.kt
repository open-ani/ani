package me.him188.ani.utils.coroutines

internal actual fun hashException(e: Throwable): Long {
    return e.stackTrace.fold(0L) { acc, stackTraceElement ->
        acc * 31 + hash(stackTraceElement).toUInt().toLong()
    }
}

private fun hash(element: StackTraceElement): Int {
    return element.lineNumber.hashCode() xor element.className.hashCode() xor element.methodName.hashCode()
}
