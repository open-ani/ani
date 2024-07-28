package me.him188.ani.app.platform

import kotlin.reflect.KClass

actual fun createPlatformServiceLoader(): ServiceLoader {
    return JdkServiceLoader()
}

private class JdkServiceLoader : ServiceLoader {
    override fun <T : Any> loadServices(service: KClass<T>): Sequence<T> {
        java.util.ServiceLoader.load(service.java).iterator().let {
            return sequence {
                while (it.hasNext()) {
                    yield(it.next())
                }
            }
        }
    }
}