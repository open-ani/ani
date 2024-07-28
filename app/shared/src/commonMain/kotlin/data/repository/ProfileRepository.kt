package me.him188.ani.app.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiUser
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ProfileRepository {
    suspend fun getSelfUserInfo(accessToken: String?): ApiResponse<UserInfo>
}

fun ProfileRepository(): ProfileRepository {
    return ProfileRepositoryImpl()
}

internal class ProfileRepositoryImpl : ProfileRepository, KoinComponent {
    private val client: BangumiClient by inject()

    override suspend fun getSelfUserInfo(accessToken: String?): ApiResponse<UserInfo> {
        return runApiRequest {
            withContext(Dispatchers.IO) {
                client.getSelfInfoByToken(accessToken).toUserInfo()
            }
        }
    }
}

fun BangumiUser.toUserInfo() = UserInfo(
    id = id,
    username = username,
    nickname = nickname,
    avatarUrl = avatar.medium,
    sign = sign,
)