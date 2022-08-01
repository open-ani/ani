package me.him188.animationgarden.api.impl

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.him188.animationgarden.api.impl.model.CacheImpl
import me.him188.animationgarden.api.impl.protocol.Network
import me.him188.animationgarden.api.model.SearchFilter
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

internal class SearchSessionImpl(
    override val filter: SearchFilter,
    private val network: Network,
) : SearchSession {
    private val page = atomic(1)

    private val cache = CacheImpl()

    override val results: Flow<Topic> by lazy {
        flow {
            val (context, result) = network.list(
                page = page.value,
                keyword = filter.keywords,
                sortId = filter.category.id,
                teamId = filter.alliance.id,
                order = filter.ordering.id
            )
            cache.mergeFrom(context)
            emitAll(result.asFlow())
        }
    }
}