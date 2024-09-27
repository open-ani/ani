/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.him188.ani.datasources.api.source.FactoryId
import kotlin.reflect.KClass

/**
 * 负责序列化和反序列化 [T], 用做导出和导入数据源的目的.
 * @see DefaultMediaSourceCodec
 */
abstract class MediaSourceCodec<T : MediaSourceArguments>(
    val factoryId: FactoryId,
    val forClass: KClass<T>,
) {
    abstract val currentVersion: Int

    abstract fun MediaSourceCodecContext.encode(arguments: T): ExportedMediaSourceData

    /**
     * 考虑版本兼容性. 潜在地会转换不兼容数据的为当前支持的
     * [ExportedMediaSourceData.factoryId] must match current [factoryId]
     */
    @Throws(UnsupportedVersionException::class)
    abstract fun MediaSourceCodecContext.decode(data: ExportedMediaSourceData): T

    /**
     * 直接使用 [KSerializer] 反序列化 [data]
     */
    abstract fun MediaSourceCodecContext.deserialize(data: JsonElement): T

    override fun toString(): String {
        return "MediaSourceArgumentCodec(factoryId=$factoryId, currentVersion=$currentVersion, forClass=$forClass)"
    }
}

sealed class MediaSourceDecodeException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception()

class UnsupportedVersionException(
    unexpectedVersion: Int,
    currentVersion: Int,
) : MediaSourceDecodeException(message = "Current version $currentVersion < $unexpectedVersion")

open class DefaultMediaSourceCodec<T : MediaSourceArguments>(
    factoryId: FactoryId,
    forClass: KClass<T>,
    override val currentVersion: Int,
    private val serializer: KSerializer<T>,
) : MediaSourceCodec<T>(factoryId, forClass) {
    override fun MediaSourceCodecContext.encode(arguments: T): ExportedMediaSourceData {
        return ExportedMediaSourceData(factoryId, currentVersion, json.encodeToJsonElement(serializer, arguments))
    }

    override fun MediaSourceCodecContext.decode(data: ExportedMediaSourceData): T {
        require(data.factoryId == factoryId) { "Invalid factory id: $factoryId, expected $factoryId" }
        if (data.version > currentVersion) {
            throw UnsupportedVersionException(unexpectedVersion = data.version, currentVersion = currentVersion)
        }

        return deserialize(data.arguments)
    }

    override fun MediaSourceCodecContext.deserialize(data: JsonElement): T {
        return json.decodeFromJsonElement(serializer, data)
    }
}

interface MediaSourceCodecContext {
    val json: Json
}
