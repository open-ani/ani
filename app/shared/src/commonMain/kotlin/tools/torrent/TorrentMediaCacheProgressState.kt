package me.him188.ani.app.tools.torrent

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.PieceState
import me.him188.ani.app.videoplayer.ui.state.Chunk
import me.him188.ani.app.videoplayer.ui.state.ChunkState
import me.him188.ani.app.videoplayer.ui.state.UpdatableMediaCacheProgressState

class TorrentMediaCacheProgressState(
    pieces: List<Piece>,
    isFinished: () -> Boolean,
) : UpdatableMediaCacheProgressState {
    private class State(
        // size 不能变, 会同时在后台更新和 UI 读取. 否则会 ConcurrentModificationException
        val currentChunkStates: MutableList<ChunkState>,
        val pieces: List<Piece>,
        val totalSize: Long,
        val version: MutableIntState,
        var alreadyFinished: Boolean = false,
    )

    private val state = kotlin.run {
        val totalSize = pieces.sumOf { it.size }

        val lastStates = pieces.mapTo(ArrayList(pieces.size)) { piece ->
            piece.state.value.toChunkState()
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
        for ((index, item) in state.pieces.withIndex()) { // compiler can optimize this
            val chunkState = item.state.value.toChunkState()
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
