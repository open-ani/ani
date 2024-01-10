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

package me.him188.ani.app.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import me.him188.ani.app.platform.Context


expect val Context.settingStore: DataStore<Preferences>

expect val Context.tokenStore: DataStore<Preferences>

object TokenStoreKeys {
    val USER_ID = longPreferencesKey("user_id")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val ACCESS_TOKEN_EXPIRE_AT = longPreferencesKey("access_token_expire_at")
}