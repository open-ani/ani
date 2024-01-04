/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.database.impl.xodus.impl

import com.jetbrains.teamsys.dnq.database.TransientEntityStoreImpl
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.*
import kotlinx.dnq.creator.findOrNew
import kotlinx.dnq.query.any
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.util.findById
import me.him188.animationgarden.server.database.UserId
import me.him188.animationgarden.server.database.UserToken
import me.him188.animationgarden.server.database.Users
import me.him188.animationgarden.server.database.login.PasswordHash
import me.him188.animationgarden.shared.AccountInputChecker
import me.him188.animationgarden.shared.dto.UserInfo
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

internal class XdUser(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdUser>()

    var username by xdRequiredStringProp(unique = true, trimmed = true)
    var password by xdRequiredStringProp() // hashed
    var timeRegistered by xdRequiredDateTimeProp()

    val refreshTokens by xdChildren0_N(XdRefreshToken::owner)
    val accessTokens by xdChildren0_N(XdAccessToken::owner)
}

internal sealed class XdToken(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdToken>()

    var value by xdRequiredStringProp(unique = true)
    var expireAt by xdRequiredDateTimeProp()
}

internal fun XdToken.toDto(): UserToken {
    return UserToken(
        token = this.value,
        expireTimestamp = this.expireAt.millis,
    )
}

internal class XdRefreshToken(entity: Entity) : XdToken(entity) {
    companion object : XdNaturalEntityType<XdRefreshToken>()

    var owner: XdUser by xdParent(XdUser::refreshTokens)
}

internal class XdAccessToken(entity: Entity) : XdToken(entity) {
    companion object : XdNaturalEntityType<XdAccessToken>()

    var owner: XdUser by xdParent(XdUser::accessTokens)
}


internal class UsersImpl(
    private val xd: TransientEntityStoreImpl,
) : Users, KoinComponent {
    private val passwordHash: PasswordHash by inject()

    init {
        XdModel.registerNodes(XdUser, XdToken)
    }

    private fun hashPassword(password: String): String {
        return passwordHash.hash(password)
    }

    override suspend fun createUser(username: String, password: String): Users.CreateUserResult {
        if (!AccountInputChecker.isUsernameValid(username)) {
            return Users.CreateUserResult.InvalidUsername
        }
        if (!AccountInputChecker.isPasswordValid(password)) {
            return Users.CreateUserResult.InvalidPassword
        }

        return xd.transactional {
            if (XdUser.filter { it.username eq username }.any()) {
                return@transactional Users.CreateUserResult.UsernameAlreadyExists
            }
            XdUser.findOrNew(XdUser.all().filter { it.username eq username }) {
                this.username = username
                this.password = hashPassword(password)
                this.timeRegistered = DateTime.now()
            }
            return@transactional Users.CreateUserResult.Success
        }
    }


    private fun generateTokenValue(
        userId: String,
    ): String = userId + "-" + UUID.randomUUID().toString()

    override suspend fun loginByPassword(username: String, password: String): Users.LoginResult {
        if (!AccountInputChecker.isUsernameValid(username)) return Users.LoginResult.UserNotFound
        val user = xd.transactional { XdUser.filter { it.username eq username }.firstOrNull() }
            ?: return Users.LoginResult.UserNotFound

        if (!passwordHash.verify(password = password, hash = user.password)) return Users.LoginResult.PasswordMismatch

        return xd.transactional {
            val refreshToken: XdRefreshToken = generateNewRefreshToken(user)
            val accessToken: XdAccessToken = generateNewAccessToken(user)
            user.refreshTokens.add(refreshToken)
            user.accessTokens.add(accessToken)

            Users.LoginResult.Success(
                accessToken = accessToken.toDto(),
                refreshToken = refreshToken.toDto(),
            )
        }
    }

    private fun generateNewRefreshToken(user: XdUser) = XdRefreshToken.new {
        this.value = generateTokenValue(user.xdId)
        this.expireAt = DateTime.now().plus(Days.days(30))
    }

    override suspend fun refreshAccessToken(username: String, refreshToken: String): Users.RefreshAccessTokenResult {
        if (!AccountInputChecker.isUsernameValid(username)) return Users.RefreshAccessTokenResult.UserNotFound
        val user =
            XdUser.filter { it.username eq username }.firstOrNull()
                ?: return Users.RefreshAccessTokenResult.UserNotFound

        return xd.transactional {
            val token = user.refreshTokens.filter { it.value eq refreshToken }.firstOrNull()
                ?: return@transactional Users.RefreshAccessTokenResult.InvalidToken

            if (token.expireAt < DateTime.now()) {
                token.delete()
                return@transactional Users.RefreshAccessTokenResult.InvalidToken
            }

            val accessToken: XdAccessToken = generateNewAccessToken(user)
            user.accessTokens.add(accessToken)

            Users.RefreshAccessTokenResult.Success(
                accessToken = accessToken.toDto(),
                refreshToken = token.toDto(),
            )
        }
    }

    private fun generateNewAccessToken(user: XdUser) = XdAccessToken.new {
        this.value = generateTokenValue(user.xdId)
        this.expireAt = DateTime.now().plus(Hours.hours(1))
    }

    override suspend fun getUserIdByToken(token: String): UserId? {
        return xd.transactional {
            XdAccessToken.filter { it.value eq token }.firstOrNull()?.owner?.xdId
        }
    }

    override suspend fun getUserInfo(userId: UserId): UserInfo {
        return xd.transactional {
            XdUser.findById(userId).let {
                UserInfo(
                    username = it.username,
                    timeRegistered = it.timeRegistered.millis,
                )
            }
        }
    }
}