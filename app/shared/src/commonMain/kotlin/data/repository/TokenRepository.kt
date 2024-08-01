package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.time.Duration.Companion.hours

/**
 * Do not access directly. Use [SessionManager] instead.
 */
interface TokenRepository : Repository {
    val refreshToken: Flow<String?>
    suspend fun setRefreshToken(value: String)

    /**
     * 当前的登录会话, 为 `null` 表示未登录.
     */
    val session: Flow<Session?>
    suspend fun setSession(session: Session)

    suspend fun clear()
}

/**
 * Bangumi 账号信息.
 */
data class Session(
    val accessToken: String,
    val expiresAtMillis: Long,
)

fun Session.isValid() = !isExpired()
fun Session.isExpired() = expiresAtMillis <= currentTimeMillis() + 1.hours.inWholeMilliseconds

internal class TokenRepositoryImpl(
    store: DataStore<Preferences>,
) : TokenRepository {
    private companion object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val ACCESS_TOKEN_EXPIRE_AT = longPreferencesKey("access_token_expire_at")
    }

    private val tokenStore = store

    override val refreshToken: Flow<String?> = tokenStore.data.map { it[REFRESH_TOKEN] }

    override suspend fun setRefreshToken(value: String) {
        tokenStore.edit { it[REFRESH_TOKEN] = value }
    }

    override val session: Flow<Session?> = tokenStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN]
        val expireAt = preferences[ACCESS_TOKEN_EXPIRE_AT]
        if (accessToken == null || expireAt == null) {
            return@map null
        }
        Session(
            accessToken = accessToken,
            expiresAtMillis = expireAt,
        )
    }

    override suspend fun setSession(session: Session) {
        tokenStore.edit {
            it[ACCESS_TOKEN] = session.accessToken
            it[ACCESS_TOKEN_EXPIRE_AT] = session.expiresAtMillis
        }
    }

    override suspend fun clear() {
        tokenStore.edit {
            it.remove(USER_ID)
            it.remove(ACCESS_TOKEN)
            it.remove(ACCESS_TOKEN_EXPIRE_AT)
            it.remove(REFRESH_TOKEN)
        }
    }
}