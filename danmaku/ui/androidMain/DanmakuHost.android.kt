package me.him188.ani.danmaku.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Note, use "Run Preview" to preview on your phone or the emulator if you only see the initial danmaku-s.
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview(showBackground = true, device = Devices.TABLET)
@Preview(showBackground = true)
internal actual fun PreviewDanmakuHost() = ProvideCompositionLocalsForPreview {
    var emitted by remember {
        mutableIntStateOf(0)
    }
    var config by remember {
        mutableStateOf(DanmakuConfig())
    }
    val data = remember {
        flow {
            var counter = 0
            fun danmaku() =
                Danmaku(
                    counter++.toString(),
                    "dummy",
                    0L, "1",
                    DanmakuLocation.NORMAL,
                    text = LoremIpsum(Random.nextInt(1..5)).values.first(),
                    0
                )

            emit(danmaku())
            emit(danmaku())
            emit(danmaku())
            while (true) {
                emit(danmaku())
                emitted++
                delay(Random.nextLong(50, 1000).milliseconds)
            }
        }
    }
    Text("Emitted: $emitted")
    val state = remember {
        DanmakuHostState().apply {
            GlobalScope.launch {
                data.collect {
                    trySend(
                        DanmakuPresentation(
                            it,
                            isSelf = Random.nextBoolean()
                        )
                    )
                }
            }
        }
    }

    if (isInLandscapeMode()) {
        Row {
            DanmakuHost(
                state,
                Modifier.weight(1f),
                config
            )
            VerticalDivider()
            EpisodeVideoSettings(
                config,
                { config = it },
                Modifier.weight(1f)
            )
        }
    } else {
        Column {
            DanmakuHost(
                state,
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                config
            )
            HorizontalDivider()
            EpisodeVideoSettings(
                config,
                { config = it },
                Modifier.weight(1f)
            )
        }

    }
}

@Composable
@Preview("Light", showBackground = true)
@Preview("Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun PreviewDanmakuText() {
    ProvideCompositionLocalsForPreview {
        Surface(color = Color.White) {
            DanmakuText(
                DummyDanmakuState,
                style = DanmakuStyle()
            )
        }
    }
}