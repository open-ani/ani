package me.him188.animationgarden.api.impl

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.him188.animationgarden.api.impl.model.CacheImpl
import me.him188.animationgarden.api.impl.protocol.Network
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

internal class SearchSessionImpl(
    override val query: SearchQuery,
    private val network: Network,
) : SearchSession {
    private val page = atomic(1)

    private val cache = CacheImpl()

    override val results: Flow<Topic> by lazy {
        flow {
            while (true) {
                val result = nextPage()
                if (result.isNullOrEmpty()) {
                    return@flow
                }
                emitAll(result.asFlow())
            }
        }
    }

    override suspend fun nextPage(): List<Topic>? {
        val currentPage = page.value
        val (context, result) = network.list(
            page = currentPage,
            keyword = query.keywords,
            sortId = query.category?.id,
            teamId = query.alliance?.id,
            orderId = query.ordering?.id
        )
        cache.mergeFrom(context)
        page.compareAndSet(currentPage, currentPage + 1)
        return result
    }
}