package me.him188.ani.utils.logging

import org.slf4j.ILoggerFactory
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

actual typealias Logger = org.slf4j.Logger

fun ILoggerFactory.getLogger(clazz: KClass<out Any>): Logger =
    getLogger(
        clazz.qualifiedName?.let {
            when {
                it.startsWith("me.him188.ani.") -> {
                    it.removePrefix("me.him188.ani.")
                }

                else -> {
                    it
                }
            }
        },
    )


inline fun <reified T : Any> ILoggerFactory.getLogger(): Logger = getLogger(T::class)

actual fun logger(name: String): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(name)
}

actual fun logger(clazz: KClass<out Any>): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(clazz)
}
