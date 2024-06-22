package me.him188.ani.app.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import org.koin.core.context.GlobalContext

@Composable
fun ChangelogDialog(
    latestVersion: NewVersion,
    onDismissRequest: () -> Unit,
    onStartDownload: () -> Unit,
    modifier: Modifier = Modifier,
    currentVersion: String = currentAniBuildConfig.versionName,
) {
    BasicAlertDialog(onDismissRequest, modifier) {
        RichDialogLayout(
            title = {
                Text("有新版本")
            },
            description = {
            },
            buttons = {
                val context by rememberUpdatedState(LocalContext.current)
                TextButton(onClick = onDismissRequest) {
                    Text("取消")
                }
                OutlinedButton({ GlobalContext.get().get<BrowserNavigator>().openBrowser(context, RELEASES) }) {
                    Icon(Icons.Rounded.ArrowOutward, null)
                }
                Button(
                    {
                        latestVersion.downloadUrlAlternatives.firstOrNull()
                            ?.let {
                                onStartDownload()
                                onDismissRequest()
                            }
                    },
                ) {
                    Icon(Icons.Rounded.Download, null)
                }
            },
        ) {
            Column(
                Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "当前版本为 $currentVersion, 最新版本为 ${latestVersion.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                for (changelog in latestVersion.changelogs) {
                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            changelog.version,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            changelog.publishedAt,
                            Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Text(
                        changelog.changes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            }
        }
    }
}

//@Composable
//fun CurrentChangelogDialog(
//    onDismissRequest: () -> Unit,
//    modifier: Modifier = Modifier,
//    currentVersion: String = currentAniBuildConfig.versionName,
//) {
//    BasicAlertDialog(onDismissRequest, modifier) {
//        RichDialogLayout(
//            title = {
//                Text("更新日志")
//            },
//            subtitle = {
//                Text(currentVersion)
//            },
//            buttons = {
//                val context by rememberUpdatedState(LocalContext.current)
//                TextButton({
//                    GlobalContext.get().get<BrowserNavigator>().openBrowser(
//                        context,
//                        "$RELEASES/tag/v3.1.0-beta03"
//                    )
//                }) {
//                    Text("在 GitHub 查看")
//                }
//                TextButton(onClick = onDismissRequest) {
//                    Text("关闭")
//                }
//            }
//        ) {
//            Column(
//                Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                for (changelog in latestVersion.changelogs) {
//                    HorizontalDivider()
//
//                    Row(verticalAlignment = Alignment.Bottom) {
//                        Text(
//                            changelog.version,
//                            style = MaterialTheme.typography.titleMedium,
//                        )
//
//                        Text(
//                            changelog.publishedAt,
//                            Modifier.padding(start = 16.dp),
//                            style = MaterialTheme.typography.labelMedium,
//                        )
//                    }
//                    Text(
//                        changelog.changes,
//                        style = MaterialTheme.typography.bodyMedium,
//                    )
//                }
//
//            }
//        }
//    }
//}

private const val RELEASES = "https://github.com/open-ani/ani/releases"
