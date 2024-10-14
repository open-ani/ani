/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress("unused")

package me.him188.ani.app.tools.ldc

import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.test.Sample

class LazyDataCacheSamples {
    private data class Subject(
        val id: Int = 1,
        val data: String = "",
    )

    private val cache = LazyDataCache(
        {
            ApiResponse.success(
                SinglePagePagedSource {
                    flowOf(
                        Subject(1), Subject(2), Subject(3),
                    )
                },
            )
        },
    )

    @Sample
    suspend fun mutate() {
        cache.mutate {
            setEach(where = { it.id == 1 }) {
                copy(data = "New data")
            }
        }
    }

}