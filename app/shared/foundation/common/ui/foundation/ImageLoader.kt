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

package me.him188.ani.app.ui.foundation

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.svg.SvgDecoder

//
//fun getDefaultKamelConfig(isProduction: Boolean) = KamelConfig {
//    if (isProduction) {
//        takeFrom(KamelConfig.Default)
//
//        httpFetcher {
//            BrowserUserAgent()
//
//            httpCache(10 * 1024 * 1024)
//
//            Logging {
//                level = LogLevel.INFO
//                logger = Logger.SIMPLE
//            }
//        }
//    }
//}

fun getDefaultImageLoader(
    context: PlatformContext,
    config: ImageLoader.Builder.() -> Unit = {}
): ImageLoader {
    return ImageLoader.Builder(context).apply {
        crossfade(true)

//        diskCache(DiskCache.Builder().apply {
//            maxSizeBytes(100 * 1024 * 1024)
//        }.build())

        diskCachePolicy(CachePolicy.ENABLED)
        memoryCachePolicy(CachePolicy.ENABLED)
        memoryCache {
            MemoryCache.Builder().apply {
                maxSizeBytes(10 * 1024 * 1024)
            }.build()
        }
        networkCachePolicy(CachePolicy.ENABLED)

        components {
            add(SvgDecoder.Factory())
        }

        config()
    }.build()
}
//CompositionLocalProvider(LocalKamelConfig provides customKamelConfig) {