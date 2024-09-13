package me.him188.ani.app.data.repository

import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface UserRepository {
    suspend fun getUserByUsername(username: String): UserInfo?
}

class UserRepositoryImpl : UserRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(UserRepositoryImpl::class)

    override suspend fun getUserByUsername(username: String): UserInfo? {
        return kotlin.runCatching {
            client.getApi().getUserByName(username).body().toUserInfo()
        }.onFailure {
            logger.warn("Exception in getUserByUsername", it)
        }.getOrNull()
    }
}