package me.him188.ani.app.data

import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.User

interface UserRepository {
    fun getUserByUsername(username: String): User?
}

class UserRepositoryImpl : UserRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(UserRepositoryImpl::class)

    override fun getUserByUsername(username: String): User? {
        return kotlin.runCatching {
            client.api.getUserByName(username)
        }.onFailure {
            logger.warn("Exception in getUserByUsername", it)
        }.getOrNull()
    }
}