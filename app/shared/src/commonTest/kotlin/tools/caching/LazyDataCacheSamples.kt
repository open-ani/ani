@file:Suppress("unused")

package me.him188.ani.app.tools.caching

import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.datasources.api.paging.SinglePagePagedSource

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

    suspend fun mutate() {
        cache.mutate {
            setEach(where = { it.id == 1 }) {
                copy(data = "New data")
            }
        }
    }

}