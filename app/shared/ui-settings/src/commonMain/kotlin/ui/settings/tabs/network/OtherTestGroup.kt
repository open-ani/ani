package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.Res
import me.him188.ani.app.ui.foundation.bangumi
import me.him188.ani.app.ui.settings.framework.ConnectionTester
import me.him188.ani.app.ui.settings.framework.ConnectionTesterResultIndicator
import me.him188.ani.app.ui.settings.framework.ConnectionTesterRunner
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SettingsScope.OtherTestGroup(
    otherTesters: ConnectionTesterRunner<ConnectionTester>,
) {
    Group(
        title = { Text("其他测试") },
    ) {
        for (tester in otherTesters.testers) {
            TextItem(
                description = { Text("提供观看记录数据") },
                icon = {
                    Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp)) {
                        Image(
                            painterResource(Res.drawable.bangumi), null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                        )
                    }
                },
                action = {
                    ConnectionTesterResultIndicator(tester, showTime = false)
                },
                title = {
                    Text("Bangumi")
                },
            )
        }

        TextButtonItem(
            onClick = { otherTesters.toggleTest() },
            title = {
                if (otherTesters.anyTesting) {
                    Text("终止测试")
                } else {
                    Text("开始测试")
                }
            },
        )
    }
}
