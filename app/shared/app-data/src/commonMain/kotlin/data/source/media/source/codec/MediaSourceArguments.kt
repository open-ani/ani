/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:OptIn(ExperimentalSubclassOptIn::class)

package me.him188.ani.app.data.source.media.source.codec

/**
 * Marker interface. 表示一个 [me.him188.ani.datasources.api.source.MediaSource] 的可导出配置.
 *
 * @see me.him188.ani.datasources.api.source.MediaSourceConfig.serializedArguments
 * @see MediaSourceCodecManager.deserializeArgument
 * 
 * @see MediaSourceCodec
 * @see MediaSourceCodecManager
 */
@SubclassOptInRequired(DontForgetToRegisterCodec::class)
interface MediaSourceArguments {
    val name: String // used as id
}

@RequiresOptIn("实现新的 MediaSourceArgument 时, 还需要在 MediaSourceCodecManager 注册此 Argument 类型的 codec")
annotation class DontForgetToRegisterCodec