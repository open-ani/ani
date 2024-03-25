package me.him188.ani.app.data

import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.User

interface ProfileRepository {
    suspend fun getSelfOrNull(): User?
}

fun ProfileRepository(): ProfileRepository {
    return ProfileRepositoryImpl()
}

internal class ProfileRepositoryImpl : ProfileRepository, KoinComponent {
    private val client: BangumiClient by inject()
    private val logger = logger(this::class)

    override suspend fun getSelfOrNull(): User? {
        return try {
            client.api.getMyself()
        } catch (e: ClientException) {
            if (e.statusCode == 403 || e.statusCode == 401) {
                return null
            } else throw e
        }
    }
}
