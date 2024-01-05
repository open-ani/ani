/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui.foundation

import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpFetcher
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.isSuccess

val DefaultKamelConfig = KamelConfig {
    httpFetcher {
        takeFrom(KamelConfig.Default)

        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, httpResponse ->
                !httpResponse.status.isSuccess()
            }
        }

        httpCache(10 * 1024 * 1024)

        Logging {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }
    }
}
//CompositionLocalProvider(LocalKamelConfig provides customKamelConfig) {