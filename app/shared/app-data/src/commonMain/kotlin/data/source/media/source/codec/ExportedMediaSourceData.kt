/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.codec

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.him188.ani.datasources.api.source.FactoryId

@Serializable
data class ExportedMediaSourceData(
    val factoryId: FactoryId,
    /**
     * 每个 factory 自己定义的 version. 通常的约束是, 高版本导出的数据源无法在低版本导入
     */
    val version: Int,
    val data: JsonElement, // 不使用多态序列化, 为了兼容性
)

@Serializable
data class ExportedMediaSourceDataList(
    val mediaSources: List<ExportedMediaSourceData>,
) 
