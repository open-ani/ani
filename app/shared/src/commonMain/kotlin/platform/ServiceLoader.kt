package me.him188.ani.app.platform

import kotlin.reflect.KClass

interface ServiceLoader {
    fun <T : Any> loadServices(service: KClass<T>): Sequence<T>

    companion object : ServiceLoader by createPlatformServiceLoader()
}

inline fun <reified T : Any> ServiceLoader.loadServices(): Sequence<T> = loadServices(T::class)

expect fun createPlatformServiceLoader(): ServiceLoader

