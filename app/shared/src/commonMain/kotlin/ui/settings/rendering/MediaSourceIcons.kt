@file:Suppress("MemberVisibilityCanBePrivate")

package me.him188.ani.app.ui.settings.rendering

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.runtime.Stable
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
}