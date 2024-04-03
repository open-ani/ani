package me.him188.ani.danmaku.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Note, use "Run Preview" to preview on your phone or the emulator if you only see the initial danmaku-s.
 */
@Composable
@Preview(showBackground = true)
internal actual fun PreviewDanmakuHost() = ProvideCompositionLocalsForPreview {
    var emitted by remember {
        mutableIntStateOf(0)
    }
    val data = remember {
        flow {
            var counter = 0
            fun danmaku() =
                Danmaku(
                    counter++.toString(), 0.0, "1",
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
        DanmakuHostState(data)
    }
    DanmakuHost(
        state,
        Modifier
            .fillMaxWidth()
    )
}

@Composable
@Preview("Light", showBackground = true)
@Preview("Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun PreviewDanmakuText() {
    ProvideCompositionLocalsForPreview {
        Surface(color = Color.White) {
            DanmakuText(
                remember {
                    object : DanmakuState {
                        override val danmaku: Danmaku = Danmaku(
                            "1", 0.0, "1",
                            DanmakuLocation.NORMAL,
                            text = "Hello, world!",
                            0
                        )
                    }
                },
                style = DanmakuStyle()
            )
        }
    }
}