/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package me.him188.ani.app.ui.settings.rendering

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.runtime.Stable
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation

object MediaSourceIcons {
    // MediaSourceKind
    inline val KindWeb get() = Icons.Rounded.Public
    inline val KindBT get() = Icons.Filled.P2p
    inline val KindLocal get() = Icons.Rounded.DownloadDone

    @Stable
    fun kind(kind: MediaSourceKind) = when (kind) {
        MediaSourceKind.WEB -> KindWeb
        MediaSourceKind.BitTorrent -> KindBT
        MediaSourceKind.LocalCache -> KindLocal
    }

    // MediaLocation
    inline val LocationLocal get() = Icons.Rounded.DownloadDone
    inline val LocationLan get() = Icons.Rounded.Radar
    inline val LocationOnline get() = Icons.Rounded.Public

    @Stable
    fun location(location: MediaSourceLocation, kind: MediaSourceKind) = when (location) {
        MediaSourceLocation.Local -> LocationLocal
        MediaSourceLocation.Lan -> LocationLan
        MediaSourceLocation.Online -> kind(kind)
    }

    @Stable
    fun getDefaultIconUrl(info: MediaSourceInfo): String {
        return URLBuilder().apply {
            takeFrom("https://ui-avatars.com/api")
            parameters.append("name", info.displayName)
        }.buildString()
    }
}