/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:JvmName("LoggerKt")

package me.him188.ani.utils.logging

import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import kotlin.reflect.KClass

fun ILoggerFactory.getLogger(clazz: KClass<out Any>): Logger =
    getLogger(clazz.qualifiedName?.let {
        when {
            it.startsWith("me.him188.ani.") -> {
                it.removePrefix("me.him188.ani.")
            }
            else -> {
                it
            }
        }
    })

inline fun <reified T : Any> ILoggerFactory.getLogger(): Logger = getLogger(T::class)

fun logger(name: String): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(name)
}

fun logger(clazz: KClass<out Any>): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(clazz)
}

@JvmName("logger1")
inline fun <reified T : Any> logger(): Logger {
    return logger(T::class)
}

inline fun Logger.trace(marker: Marker? = null, message: () -> String) {
    if (isTraceEnabled(marker)) {
        trace(marker, message())
    }
}

inline fun Logger.debug(marker: Marker? = null, message: () -> String) {
    if (isDebugEnabled(marker)) {
        debug(marker, message())
    }
}

inline fun Logger.info(marker: Marker? = null, message: () -> String) {
    if (isInfoEnabled(marker)) {
        info(marker, message())
    }
}

inline fun Logger.warn(marker: Marker? = null, message: () -> String) {
    if (isWarnEnabled(marker)) {
        warn(marker, message())
    }
}

inline fun Logger.error(marker: Marker? = null, message: () -> String) {
    if (isErrorEnabled(marker)) {
        error(marker, message())
    }
}


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun Logger.trace(exception: Throwable? = null, message: () -> String) {
    if (isTraceEnabled) {
        trace(message(), exception)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun Logger.debug(exception: Throwable? = null, message: () -> String) {
    if (isDebugEnabled) {
        debug(message(), exception)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun Logger.info(exception: Throwable? = null, message: () -> String) {
    if (isInfoEnabled) {
        info(message(), exception)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun Logger.warn(exception: Throwable? = null, message: () -> String) {
    if (isWarnEnabled) {
        warn(message(), exception)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun Logger.error(exception: Throwable? = null, message: () -> String) {
    if (isErrorEnabled) {
        error(message(), exception)
    }
}


inline fun Logger.trace(marker: Marker? = null, exception: Throwable? = null, message: () -> String) {
    if (isTraceEnabled(marker)) {
        trace(marker, message(), exception)
    }
}

inline fun Logger.debug(marker: Marker? = null, exception: Throwable? = null, message: () -> String) {
    if (isDebugEnabled(marker)) {
        debug(marker, message(), exception)
    }
}

inline fun Logger.info(marker: Marker? = null, exception: Throwable? = null, message: () -> String) {
    if (isInfoEnabled(marker)) {
        info(marker, message(), exception)
    }
}

inline fun Logger.warn(marker: Marker? = null, exception: Throwable? = null, message: () -> String) {
    if (isWarnEnabled(marker)) {
        warn(marker, message(), exception)
    }
}

inline fun Logger.error(marker: Marker? = null, exception: Throwable? = null, message: () -> String) {
    if (isErrorEnabled(marker)) {
        error(marker, message(), exception)
    }
}
