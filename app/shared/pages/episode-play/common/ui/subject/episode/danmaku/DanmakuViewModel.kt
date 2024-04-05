package me.him188.ani.app.ui.subject.episode.danmaku

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.PreferencesRepository
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHostState
import me.him188.ani.danmaku.ui.DanmakuProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel that supports presenting and sending danmaku with configs loaded from the data stores.
 */
@Stable
interface PlayerDanmakuViewModel {
    val danmakuHostState: DanmakuHostState

    val enabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)

    val config: Flow<DanmakuConfig>

    suspend fun send(text: String)
}

fun PlayerDanmakuViewModel(): PlayerDanmakuViewModel = PlayerDanmakuStateImpl()

internal class PlayerDanmakuStateImpl(
    danmakuProperties: DanmakuProperties = DanmakuProperties.Default,
) : PlayerDanmakuViewModel, KoinComponent {
    private val preferencesRepository: PreferencesRepository by inject()
    override val danmakuHostState: DanmakuHostState = DanmakuHostState(danmakuProperties)

    override val enabled: Flow<Boolean> = preferencesRepository.danmakuEnabled.flow

    override suspend fun setEnabled(enabled: Boolean) {
        preferencesRepository.danmakuEnabled.set(enabled)
    }

    override val config: Flow<DanmakuConfig> = preferencesRepository.danmakuConfig.flow
    override suspend fun send(text: String) {
        // TODO:  
    }
}

