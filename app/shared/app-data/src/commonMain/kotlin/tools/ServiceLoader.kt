package me.him188.ani.app.tools

import kotlin.reflect.KClass

expect object ServiceLoader {
    fun <T : Any> loadServices(kClass: KClass<T>): List<T>
}

