package me.him188.ani.app.data.repositories

import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiUser
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface UserRepository {
    suspend fun getUserByUsername(username: String): BangumiUser?
}

class UserRepositoryImpl : UserRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(UserRepositoryImpl::class)

    override suspend fun getUserByUsername(username: String): BangumiUser? {
        return kotlin.runCatching {
            client.api.getUserByName(username).body()
        }.onFailure {
            logger.warn("Exception in getUserByUsername", it)
        }.getOrNull()
    }
}