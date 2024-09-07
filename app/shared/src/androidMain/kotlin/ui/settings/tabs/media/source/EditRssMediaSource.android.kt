package me.him188.ani.app.ui.settings.tabs.media.source

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.utils.platform.annotations.TestOnly


@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
fun PreviewEditRssMediaSourcePagePhone() = ProvideCompositionLocalsForPreview {
    val scope = rememberCoroutineScope()
    EditRssMediaSourcePage(
        rememberTestEditRssMediaSourceState(scope),
        remember { RssTestPaneState() },
    )
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
fun PreviewEditRssMediaSourcePagePhoneTest() = ProvideCompositionLocalsForPreview {
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()
    EditRssMediaSourcePage(
        rememberTestEditRssMediaSourceState(scope),
        remember { RssTestPaneState() },
        navigator = navigator,
    )
    SideEffect {
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }
}

@OptIn(TestOnly::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET)
@Preview(device = Devices.PIXEL_TABLET, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
fun PreviewEditRssMediaSourcePageLaptop() = ProvideCompositionLocalsForPreview {
    val scope = rememberCoroutineScope()
    EditRssMediaSourcePage(
        rememberTestEditRssMediaSourceState(scope),
        remember { RssTestPaneState() },
    )
}

@TestOnly
@Composable
internal fun rememberTestEditRssMediaSourceState(scope: CoroutineScope) = remember {
    EditRssMediaSourceState(
        arguments = RssMediaSourceArguments.Default,
        editMediaSourceMode = EditMediaSourceMode.Add,
        instanceId = "test-id",
        onSave = {},
        backgroundScope = scope,
    )
}
