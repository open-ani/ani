package me.him188.ani.app.data.repositories

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiUser
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ProfileRepository {
    suspend fun getSelfOrNull(): BangumiUser?
}

fun ProfileRepository(): ProfileRepository {
    return ProfileRepositoryImpl()
}

internal class ProfileRepositoryImpl : ProfileRepository, KoinComponent {
    private val client: BangumiClient by inject()
    private val logger = logger(this::class)

    override suspend fun getSelfOrNull(): BangumiUser? {
        return try {
            withContext(Dispatchers.IO) {
                client.api.getMyself().body()
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Forbidden || e.response.status == HttpStatusCode.Unauthorized) {
                return null
            } else throw e
        }
    }
}
