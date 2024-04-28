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
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.him188.ani.app.data.repositories.MikanIndexes
import me.him188.ani.app.platform.Context
import java.io.File
import java.io.InputStream
import java.io.OutputStream


expect val Context.preferencesStore: DataStore<Preferences>

expect val Context.tokenStore: DataStore<Preferences>

/**
 * Must not be stored
 */
expect val Context.dataStoresImpl: PlatformDataStoreManager

// workaround for compiler bug
inline val Context.dataStores: PlatformDataStoreManager get() = dataStoresImpl

abstract class PlatformDataStoreManager {
    val mikanIndexStore: DataStore<MikanIndexes>
        get() = DataStoreFactory.create(
            serializer = MikanIndexes.serializer().asDataStoreSerializer(MikanIndexes.Empty),
            produceFile = { resolveDataStoreFile("mikanIndexes") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                MikanIndexes.Empty
            },
        )

    abstract fun resolveDataStoreFile(name: String): File
}

fun <T> KSerializer<T>.asDataStoreSerializer(
    defaultValue: T,
    format: Json = Json {
        ignoreUnknownKeys = true
    },
): Serializer<T> {
    val serializer = this
    return object : Serializer<T> {
        override val defaultValue: T get() = defaultValue

        override suspend fun readFrom(input: InputStream): T = format.decodeFromStream(serializer, input)

        override suspend fun writeTo(t: T, output: OutputStream) {
            format.encodeToStream(serializer, t, output)
        }
    }
}