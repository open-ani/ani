package me.him188.ani.app.ui.subject.episode.danmaku

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.danmaku.DanmakuManager
import me.him188.ani.app.data.repositories.PreferencesRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHostState
import me.him188.ani.danmaku.ui.DanmakuProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel that supports presenting and sending danmaku with configs loaded from the data stores.
 */
@Stable
interface PlayerDanmakuViewModel : AutoCloseable {
    val danmakuHostState: DanmakuHostState

    val enabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)

    val config: Flow<DanmakuConfig>

    val isSending: Boolean

    suspend fun send(info: DanmakuInfo)
}

fun PlayerDanmakuViewModel(): PlayerDanmakuViewModel = PlayerDanmakuStateImpl()

internal class PlayerDanmakuStateImpl(
    danmakuProperties: DanmakuProperties = DanmakuProperties.Default,
) : PlayerDanmakuViewModel, AbstractViewModel(), KoinComponent {
    private val preferencesRepository: PreferencesRepository by inject()
    override val danmakuHostState: DanmakuHostState = DanmakuHostState(danmakuProperties)
    private val danmakuManager: DanmakuManager by inject()

    override val enabled: Flow<Boolean> = preferencesRepository.danmakuEnabled.flow

    override suspend fun setEnabled(enabled: Boolean) {
        preferencesRepository.danmakuEnabled.set(enabled)
    }

    override val config: Flow<DanmakuConfig> = preferencesRepository.danmakuConfig.flow

    override var isSending: Boolean by mutableStateOf(false)

    override suspend fun send(info: DanmakuInfo) {
        try {
            danmakuManager.post(info)
        } finally {
            withContext(Dispatchers.Main) {
                isSending = false
            }
        }
    }
}

