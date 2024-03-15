package me.him188.ani.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.persistent.TokenStoreKeys
import me.him188.ani.app.session.Session

interface TokenRepository : Repository {
    val refreshToken: Flow<String?>
    suspend fun setRefreshToken(value: String)

    val session: Flow<Session?>
    suspend fun setSession(session: Session)
}


internal class TokenRepositoryImpl(
    store: DataStore<Preferences>,
) : TokenRepository {
    private val tokenStore = store

    override val refreshToken: Flow<String?> = tokenStore.data.map { it[TokenStoreKeys.REFRESH_TOKEN] }

    override suspend fun setRefreshToken(value: String) {
        tokenStore.edit { it[TokenStoreKeys.REFRESH_TOKEN] = value }
    }

    override val session: Flow<Session?> = tokenStore.data.map { preferences ->
        val userId = preferences[TokenStoreKeys.USER_ID]
        val accessToken = preferences[TokenStoreKeys.ACCESS_TOKEN]
        val expireAt = preferences[TokenStoreKeys.ACCESS_TOKEN_EXPIRE_AT]
        if (userId == null || accessToken == null || expireAt == null) {
            return@map null
        }
        Session(
            userId = userId,
            accessToken = accessToken,
            expiresIn = expireAt,
        )
    }

    override suspend fun setSession(session: Session) {
        tokenStore.edit {
            it[TokenStoreKeys.USER_ID] = session.userId
            it[TokenStoreKeys.ACCESS_TOKEN] = session.accessToken
            it[TokenStoreKeys.ACCESS_TOKEN_EXPIRE_AT] = session.expiresIn
        }
    }
}