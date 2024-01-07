package me.him188.ani.datasources.bangumi.client

import me.him188.ani.datasources.bangumi.models.BangumiToken
import me.him188.ani.datasources.bangumi.models.users.BangumiAccount

interface BangumiClientAccounts {
    sealed interface LoginResponse {
        data class Success(
            val account: BangumiAccount,
            val token: BangumiToken,
        ) : LoginResponse

        data object UsernameOrPasswordMismatch : LoginResponse
        data class UnknownError(
            val trace: String,
        ) : LoginResponse
    }

    suspend fun login(
        email: String,
        password: String,
    ): LoginResponse
}