package me.him188.ani.app.data.models

/**
 * 与数据源无关的 Bangumi 用户信息
 */
data class UserInfo(
    val id: Int,
    val username: String?,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val sign: String? = null
) {
    companion object {
        val EMPTY = UserInfo(0, "")
    }
}