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

package me.him188.animationgarden.app.ui.auth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.animationgarden.app.ui.LocalSession
import me.him188.animationgarden.app.ui.framework.AbstractViewModel
import me.him188.animationgarden.datasources.bangumi.BangumiClient
import me.him188.animationgarden.datasources.bangumi.client.BangumiClientAccounts
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountViewModel(
    isRegister: Boolean,
) : AbstractViewModel(), KoinComponent {
    private val client: BangumiClient by inject()
    private val localSession: LocalSession by inject()

    val isRegister = MutableStateFlow(isRegister)

    private val _username: MutableState<String> = mutableStateOf("")
    val username: State<String> get() = _username

    private val _password: MutableState<String> = mutableStateOf("")
    val password: State<String> get() = _password

    private val _verifyPassword: MutableState<String> = mutableStateOf("")
    val verifyPassword: State<String> get() = _verifyPassword

    val usernameError: MutableStateFlow<String?> = MutableStateFlow(null)
    val usernameValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val passwordError: MutableStateFlow<String?> = MutableStateFlow(null)
    val verifyPasswordError: MutableStateFlow<String?> = MutableStateFlow(null)


    val isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun setUsername(username: String) {
        flushErrors()
        _username.value = username.trim()
        usernameError.value = null
    }

    fun setPassword(password: String) {
        flushErrors()
        _password.value = password
        passwordError.value = null
    }

    fun setVerifyPassword(password: String) {
        flushErrors()
        _verifyPassword.value = password
        verifyPasswordError.value = null
    }

    suspend fun onClickProceed() {
        if (!checkInputs()) return

        val username = username.value
        val password = password.value

        doAuth(username, password, isRegister.value)
    }

    private suspend fun doAuth(email: String, password: String, isRegister: Boolean) {
        if (isRegister) {
            TODO("Registering is not supported yet")
        }
        when (val resp = client.accounts.login(email, password)) {
            is BangumiClientAccounts.LoginResponse.Success -> {
                localSession.setSession(resp.account, resp.token)
            }

            is BangumiClientAccounts.LoginResponse.UnknownError -> {
                verifyPasswordError.value = "Unknown error: ${resp.trace}"
            }

            BangumiClientAccounts.LoginResponse.UsernameOrPasswordMismatch -> {
                verifyPasswordError.value = "Username or password mismatch"
            }
        }
    }

    private fun checkInputs(): Boolean {
        val username = username.value
        if (username.isEmpty()) {
            usernameError.value = "Please enter username"
            return false
        }
        val password = password.value
        if (password.isEmpty()) {
            passwordError.value = "Please enter password"
            return false
        }
        val verifyPassword = verifyPassword.value
        if (verifyPassword.isEmpty() && isRegister.value) {
            verifyPasswordError.value = "Please re-enter your password"
            return false
        }
        if (password != verifyPassword && isRegister.value) {
            verifyPasswordError.value = "Passwords do not match. Please re-enter your password"
            return false
        }
        return true
    }

    fun onClickSwitch() {
        flush()
        if (isProcessing.value) return
    }

    private fun flush() {
        _username.value = ""
        _password.value = ""
        _verifyPassword.value = ""
        flushErrors()
    }

    private fun flushErrors() {
        usernameError.value = null
        passwordError.value = null
        usernameValid.value = false
    }
}
