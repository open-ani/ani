/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.client

import android.os.RemoteException
import kotlinx.coroutines.suspendCancellableCoroutine
import me.him188.ani.app.domain.torrent.IRemoteTorrentFileHandle
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle

class RemoteTorrentFileHandle(
    private val remote: IRemoteTorrentFileHandle
) : TorrentFileHandle {
    override val entry: TorrentFileEntry by lazy { RemoteTorrentFileEntry(remote.torrentFileEntry) }
    
    override fun resume(priority: FilePriority) {
        remote.resume(priority.ordinal)
    }

    override fun pause() {
        remote.pause()
    }

    override suspend fun close() {
        return suspendCancellableCoroutine { cont ->
            try {
                val result = remote.close()
                cont.resumeWith(Result.success(result))
            } catch (re: RemoteException) {
                cont.resumeWith(Result.failure(re))
            }
        }
    }

    override suspend fun closeAndDelete() {
        return suspendCancellableCoroutine { cont ->
            try {
                val result = remote.closeAndDelete()
                cont.resumeWith(Result.success(result))
            } catch (re: RemoteException) {
                cont.resumeWith(Result.failure(re))
            }
        }
    }


}