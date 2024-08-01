package me.him188.ani.utils.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

actual fun logger(name: String): Logger {
    return IosLogger(KotlinLogging.logger(name))
}

actual fun logger(clazz: KClass<out Any>): Logger {
    return IosLogger(KotlinLogging.logger(clazz.qualifiedName ?: clazz.simpleName ?: clazz.toString()))
}

actual interface Logger {
    actual fun isTraceEnabled(): Boolean
    actual fun trace(message: String?, throwable: Throwable?)
    actual fun isDebugEnabled(): Boolean
    actual fun debug(message: String?, throwable: Throwable?)
    actual fun isInfoEnabled(): Boolean
    actual fun info(message: String?, throwable: Throwable?)
    actual fun isWarnEnabled(): Boolean
    actual fun warn(message: String?, throwable: Throwable?)
    actual fun isErrorEnabled(): Boolean
    actual fun error(message: String?, throwable: Throwable?)
}

private class IosLogger(
    private val delegate: KLogger,
) : Logger {
    override fun isTraceEnabled(): Boolean = delegate.isTraceEnabled()

    override fun trace(message: String?, throwable: Throwable?) {
        return delegate.trace(throwable) { message }
    }

    override fun isDebugEnabled(): Boolean {
        return delegate.isDebugEnabled()
    }

    override fun debug(message: String?, throwable: Throwable?) {
        return delegate.debug(throwable) { message }
    }

    override fun isInfoEnabled(): Boolean {
        return delegate.isInfoEnabled()
    }

    override fun info(message: String?, throwable: Throwable?) {
        return delegate.info(throwable) { message }
    }

    override fun isWarnEnabled(): Boolean {
        return delegate.isWarnEnabled()
    }

    override fun warn(message: String?, throwable: Throwable?) {
        return delegate.warn(throwable) { message }
    }

    override fun isErrorEnabled(): Boolean {
        return delegate.isErrorEnabled()
    }

    override fun error(message: String?, throwable: Throwable?) {
        return delegate.error(throwable) { message }
    }
}