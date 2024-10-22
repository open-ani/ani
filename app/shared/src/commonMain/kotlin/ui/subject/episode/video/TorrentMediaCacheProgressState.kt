/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.api.pieces.PieceList
import me.him188.ani.app.torrent.api.pieces.PieceState
import me.him188.ani.app.torrent.api.pieces.count
import me.him188.ani.app.torrent.api.pieces.forEachIndexed
import me.him188.ani.app.torrent.api.pieces.mapIndexed
import me.him188.ani.app.torrent.api.pieces.mapTo
import me.him188.ani.app.torrent.api.pieces.sumOf
import me.him188.ani.app.videoplayer.ui.state.Chunk
import me.him188.ani.app.videoplayer.ui.state.ChunkState
import me.him188.ani.app.videoplayer.ui.state.UpdatableMediaCacheProgressState

class TorrentMediaCacheProgressState(
    pieces: PieceList,
    isFinished: () -> Boolean,
) : UpdatableMediaCacheProgressState {
    private class State(
        // size 不能变, 会同时在后台更新和 UI 读取. 否则会 ConcurrentModificationException
        val currentChunkStates: MutableList<ChunkState>,
        val pieces: PieceList,
        val totalSize: Long,
        val version: MutableIntState,
        var alreadyFinished: Boolean = false,
    )

    private val state = kotlin.run {
        val totalSize = pieces.sumOf { it.size }

        val lastStates = pieces.mapTo(ArrayList(pieces.count)) { piece ->
            piece.state.toChunkState()
        }

        State(
            currentChunkStates = lastStates,
            pieces,
            totalSize = totalSize,
            version = mutableIntStateOf(0),
        )
    }

    override suspend fun update() {
        // 在后台每秒算一下有哪些 piece 更新了. piece 数量庞大, 为它们分别启动一个协程或分配一个 state 是会有性能问题的
        if (state.alreadyFinished) return
        var anyChanged = false
        var allFinished = true

        state.pieces.forEachIndexed { index, item ->
            val chunkState = item.state.toChunkState()
            if (chunkState != state.currentChunkStates[index]) {
                // 变了
                anyChanged = true
                state.currentChunkStates[index] = chunkState
            }
            if (chunkState != ChunkState.DONE) {
                allFinished = false
            }
        }
        if (allFinished) {
            state.alreadyFinished = true
        }

        if (anyChanged) {
            withContext(Dispatchers.Main.immediate) { state.version.value++ }
        }
    }

    override val isFinished: Boolean by derivedStateOf(isFinished)
    override val version: Int by state.version
    override val chunks: List<Chunk> = state.let { state ->
        state.pieces.mapIndexed { index, piece ->
            object : Chunk {
                override val weight: Float = piece.size.toFloat() / state.totalSize.toFloat()
                override val state: ChunkState get() = state.currentChunkStates[index]
            }
        }
    }
}

private fun PieceState.toChunkState(): ChunkState = when (this) {
    PieceState.READY -> ChunkState.NONE
    PieceState.DOWNLOADING -> ChunkState.DOWNLOADING
    PieceState.FINISHED -> ChunkState.DONE
    PieceState.NOT_AVAILABLE -> ChunkState.NOT_AVAILABLE
}
