/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

package me.him188.ani.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.bangumi.models.BangumiAccessToken
import me.him188.ani.datasources.bangumi.models.users.BangumiAccount
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

class LocalSession : AbstractViewModel() {
    internal class Session(
        val account: BangumiAccount,
        val token: BangumiAccessToken,
    )

    internal var _session: MutableStateFlow<Session?> = MutableStateFlow(null)

    val account: StateFlow<BangumiAccount?>
        get() = _session.map { it?.account }.stateInBackground()

    val token: StateFlow<BangumiAccessToken?>
        get() = _session.map { it?.token }.stateInBackground()

    fun setSession(account: BangumiAccount, token: BangumiAccessToken) {

        _session.value = Session(account, token)
    }
}

@Composable
fun LocalSession.tokenOrNavigate(navigateToAuth: () -> Nothing): BangumiAccessToken {
    val token by token.collectAsStateWithLifecycle(null)
    return token ?: navigateToAuth()
}