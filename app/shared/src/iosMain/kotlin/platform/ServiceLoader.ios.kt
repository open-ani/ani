package me.him188.ani.app.platform

import me.him188.ani.app.torrent.anitorrent.AnitorrentDownloaderFactory
import me.him188.ani.app.torrent.api.TorrentDownloaderFactory
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import kotlin.reflect.KClass

actual fun createPlatformServiceLoader(): ServiceLoader {
    return RegistryServiceLoader().apply {
        register<MediaSourceFactory> { MikanMediaSource.Factory() }
        register<MediaSourceFactory> { MikanCNMediaSource.Factory() }
        register<TorrentDownloaderFactory> { AnitorrentDownloaderFactory() }
    }
}

open class RegistryServiceLoader : ServiceLoader {
    class Registry<T : Any>(
        val kClass: KClass<T>,
        val factory: () -> T,
    )

    val registries = mutableListOf<Registry<*>>()

    value class RegisterBuilder<T : Any> @PublishedApi internal constructor(
        private val register: (() -> T) -> Unit,
    ) {
        fun factory(factory: () -> T) {
            register(factory)
        }
    }

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER") // 必须提供 kClass
    inline fun <T : Any> register(
        kClass: KClass<T>,
        builderAction: RegisterBuilder<@kotlin.internal.NoInfer T>.() -> Unit,
    ) {
        RegisterBuilder {
            registries.add(Registry(kClass, it))
        }.run(builderAction)
    }

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER") // 必须提供泛型
    inline fun <reified T : Any> register(
        builderAction: RegisterBuilder<@kotlin.internal.NoInfer T>.() -> Unit,
    ) = register(T::class, builderAction)

    override fun <T : Any> loadServices(service: KClass<T>): Sequence<T> {
        return registries.asSequence()
            .filter { it.kClass == service }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.factory() as T
            }
    }
}

internal fun RegistryServiceLoader.registerCommonServices() {
    register<ServiceLoader> {
        RegistryServiceLoader()
    }
}
