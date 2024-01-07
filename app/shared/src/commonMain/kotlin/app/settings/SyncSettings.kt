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

package me.him188.ani.app.app.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.app.app.data.MutableProperty
import java.io.File
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
data class SyncSettings(
    /**
     * If disabled, this app will run with fresh data every time.
     */
    val localSyncEnabled: Boolean = true,
    val localSync: LocalSyncSettings = LocalSyncSettings(),
    val remoteSyncEnabled: Boolean = false,
    val remoteSync: RemoteSyncSettings = RemoteSyncSettings(),
)

@Serializable
data class RemoteSyncSettings(
    val apiUrl: String = "https://sync.ani.him188.moe",
    val token: String = randomSyncToken(),
    val useProxy: Boolean = false,
) {
    companion object {
        private val nonceRanges = arrayOf('a'..'z', 'A'..'z', '0'..'9')

        fun randomSyncToken(length: Int = 32, random: Random = Random): String {
            return generateSequence { nonceRanges.random(random).random(random) }
                .take(length)
                .joinToString(separator = "")
        }
    }
}

@Serializable
class LocalSyncSettings(
    /**
     * Interval in saving local data.
     */
    val checkInternal: @Serializable(DurationSerializer::class) Duration = 1.minutes,
)

private object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.time.Duration", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString(value.toIsoString())
    }

    override fun deserialize(decoder: Decoder): Duration {
        return Duration.parseIsoString(decoder.decodeString())
    }
}


fun createFileDelegatedMutableProperty(file: File): MutableProperty<String> = object : MutableProperty<String> {
    override suspend fun get(): String {
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                file.readText()
            } else {
                ""
            }
        }
    }

    override suspend fun set(value: String) {
        return withContext(Dispatchers.IO) {
            try {
                file.parentFile.mkdirs()
            } catch (_: Exception) {
            }
            file.writeText(value)
        }
    }
}

