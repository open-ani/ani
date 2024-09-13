package me.him188.ani.app.tools

import kotlin.reflect.KClass

actual object ServiceLoader {
    actual fun <T : Any> loadServices(kClass: KClass<T>): List<T> {
        return java.util.ServiceLoader.load(kClass.java).toList()
    }
}
