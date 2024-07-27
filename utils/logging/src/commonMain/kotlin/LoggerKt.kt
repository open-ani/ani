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

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

expect interface Logger {
    fun isTraceEnabled(): Boolean
    fun trace(message: String?, throwable: Throwable?)

    fun isDebugEnabled(): Boolean
    fun debug(message: String?, throwable: Throwable?)

    fun isInfoEnabled(): Boolean
    fun info(message: String?, throwable: Throwable?)

    fun isWarnEnabled(): Boolean
    fun warn(message: String?, throwable: Throwable?)

    fun isErrorEnabled(): Boolean
    fun error(message: String?, throwable: Throwable?)
}

fun Logger.trace(message: String?) = trace(message, null)
fun Logger.trace(throwable: Throwable?) = trace(null, throwable)

fun Logger.debug(message: String?) = debug(message, null)
fun Logger.debug(throwable: Throwable?) = debug(null, throwable)

fun Logger.info(message: String?) = info(message, null)
fun Logger.info(throwable: Throwable?) = info(null, throwable)

fun Logger.warn(message: String?) = warn(message, null)
fun Logger.warn(throwable: Throwable?) = warn(null, throwable)

fun Logger.error(message: String?) = error(message, null)
fun Logger.error(throwable: Throwable?) = error(null, throwable)

expect fun logger(name: String): Logger

expect fun logger(clazz: KClass<out Any>): Logger

@JvmName("logger1")
inline fun <reified T : Any> logger(): Logger {
    return logger(T::class)
}


inline fun Logger.trace(message: () -> String) {
    if (isTraceEnabled()) {
        trace(message())
    }
}

inline fun Logger.debug(message: () -> String) {
    if (isDebugEnabled()) {
        debug(message())
    }
}

inline fun Logger.info(message: () -> String) {
    if (isInfoEnabled()) {
        info(message())
    }
}

@OverloadResolutionByLambdaReturnType
inline fun Logger.warn(message: () -> String) {
    if (isWarnEnabled()) {
        warn(message())
    }
}

@JvmName("warnThrowable")
@OverloadResolutionByLambdaReturnType
inline fun Logger.warn(e: () -> Throwable) {
    contract { callsInPlace(e, InvocationKind.AT_MOST_ONCE) }
    if (isErrorEnabled()) {
        val exception = e()
        warn(exception.message, exception)
    }
}

@OverloadResolutionByLambdaReturnType
inline fun Logger.error(message: () -> String) {
    error(message())
}

@JvmName("errorThrowable")
@OverloadResolutionByLambdaReturnType
inline fun Logger.error(e: () -> Throwable) {
    contract { callsInPlace(e, InvocationKind.AT_MOST_ONCE) }
    if (isErrorEnabled()) {
        val exception = e()
        error(exception.message, exception)
    }
}

inline fun Logger.trace(exception: Throwable? = null, message: () -> String) {
    if (isTraceEnabled()) {
        trace(message(), exception)
    }
}

inline fun Logger.debug(exception: Throwable? = null, message: () -> String) {
    if (isDebugEnabled()) {
        debug(message(), exception)
    }
}

inline fun Logger.info(exception: Throwable? = null, message: () -> String) {
    if (isInfoEnabled()) {
        info(message(), exception)
    }
}

inline fun Logger.warn(exception: Throwable? = null, message: () -> String) {
    if (isWarnEnabled()) {
        warn(message(), exception)
    }
}

inline fun Logger.error(exception: Throwable? = null, message: () -> String) {
    if (isErrorEnabled()) {
        error(message(), exception)
    }
}
