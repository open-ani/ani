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

package me.him188.animationgarden.app.app

import androidx.compose.runtime.Stable
import io.ktor.client.plugins.ResponseException
import io.ktor.util.logging.error
import me.him188.animationgarden.app.i18n.ResourceBundle
import me.him188.animationgarden.datasources.api.DownloadSearchQuery
import org.slf4j.LoggerFactory
import kotlin.toString as toStringKotlin

sealed class FetchingState {
    object Idle : FetchingState()
    class Fetching(
        val query: DownloadSearchQuery
    ) : FetchingState()

    sealed class Completed : FetchingState()
    object Succeed : Completed()
    class Failed(
        val exception: Throwable
    ) : Completed()
}


private val logger = LoggerFactory.getLogger(FetchingState::class.java)!!

@Stable
fun FetchingState.Failed.render(resources: ResourceBundle): String = buildString {
    val localizedMessage =
        exception.localizedMessage.toStringKotlin() // use Kotlin's extension since `localizedMessage` is nullable.

    val exception = exception
    logger.error(exception)
    if (exception is ResponseException) {
        when (exception.response.status.value) {
            in 500 until 600 -> {
                append(resources.getString("search.failed.tips"))
                append(resources.getString("search.failed.tips.server.error"))
            }
        }
    }

    append(localizedMessage)
}