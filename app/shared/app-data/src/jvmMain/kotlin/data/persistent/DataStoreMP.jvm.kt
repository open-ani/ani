/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.persistent

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.toFile
import java.io.InputStream
import java.io.OutputStream

// Datastore 忘了给 expect 加 default constructor
actual fun <T> ReplaceFileCorruptionHandler(produceNewData: (CorruptionException) -> T): ReplaceFileCorruptionHandler<T> {
    return ReplaceFileCorruptionHandler(produceNewData)
}

actual fun <T> KSerializer<T>.asDataStoreSerializer(
    defaultValue: () -> T,
    format: Json,
): DataStoreSerializer<T> {
    val serializer = this
    return object : Serializer<T> {
        override val defaultValue: T by lazy(defaultValue)

        override suspend fun readFrom(input: InputStream): T {
            try {
                return format.decodeFromStream(serializer, input)
            } catch (e: Exception) {
                throw CorruptionException("Failed to decode data", e)
            }
        }

        override suspend fun writeTo(t: T, output: OutputStream) {
            format.encodeToStream(serializer, t, output)
        }
    }
}

actual fun <T> DataStoreFactory.create(
    serializer: DataStoreSerializer<T>,
    corruptionHandler: ReplaceFileCorruptionHandler<T>?,
    migrations: List<DataMigration<T>>,
    scope: CoroutineScope,
    produceFile: () -> SystemPath
): DataStore<T> = create(
    serializer = serializer,
    corruptionHandler = corruptionHandler,
    migrations = migrations,
    scope = scope,
    produceFile = { produceFile().toFile() },
)

actual typealias DataStoreSerializer<T> = Serializer<T>
