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

package me.him188.ani.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json


fun <T> StringFormat.saverFor(serializer: KSerializer<T>): Saver<T, *> = Saver(
    save = { encodeToString(serializer, it) },
    restore = { decodeFromString(serializer, it) }
)

@Composable
fun <T> rememberSaverFor(
    serializer: KSerializer<T>,
    format: StringFormat = LocalSerialFormat.current
): Saver<T, *> {
    return remember(serializer, format) {
        format.saverFor(serializer)
    }
}


private val defaultSerialFormat = Json
val LocalSerialFormat = staticCompositionLocalOf<StringFormat> {
    defaultSerialFormat
}
