package me.him188.ani.app.session

import me.him188.ani.app.data.models.UserInfo

sealed interface SessionState {
    sealed interface HasAccessToken : SessionState {
        val accessToken: String
    }

    data class Verified(
        override val accessToken: String,
        val userInfo: UserInfo,
    ) : SessionState, HasAccessToken

    /**
     * 正在验证会话有效性 (同时也能验证是否有网络)
     */
//    data class Verifying(
//        override val accessToken: String,
//    ) : SessionState, HasAccessToken

    /**
     * 有 token, 但是验证失败
     */
    sealed interface VerificationFailed : SessionState

    /**
     * 有 token, 但是验证失败, 因为 token 过期了
     */
    data object Expired : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为网络问题
     */
    data object NetworkError : VerificationFailed

    /**
     * 有 token, 但是验证失败, 因为 bug
     */
    data class Exception(
        val cause: Throwable,
    ) : VerificationFailed

    /**
     * 没有 token
     */
    data object NoToken : SessionState
}

fun SessionState.isValid(): Boolean = this is SessionState.Verified

val SessionState.unverifiedAccessToken: String? get() = (this as? SessionState.HasAccessToken)?.accessToken
val SessionState.userInfo: UserInfo? get() = (this as? SessionState.Verified)?.userInfo
val SessionState.username: String? get() = userInfo?.username
