package me.him188.ani.app.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.session.Session

interface TokenRepository : Repository {
    val refreshToken: Flow<String?>
    suspend fun setRefreshToken(value: String)

    val session: Flow<Session?>
    suspend fun setSession(session: Session)

    suspend fun clear()
}

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
        val userId = preferences[USER_ID]
        val accessToken = preferences[ACCESS_TOKEN]
        val expireAt = preferences[ACCESS_TOKEN_EXPIRE_AT]
        if (userId == null || accessToken == null || expireAt == null) {
            return@map null
        }
        Session(
            userId = userId,
            accessToken = accessToken,
            expiresAt = expireAt,
        )
    }

    override suspend fun setSession(session: Session) {
        tokenStore.edit {
            it[USER_ID] = session.userId
            it[ACCESS_TOKEN] = session.accessToken
            it[ACCESS_TOKEN_EXPIRE_AT] = session.expiresAt
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