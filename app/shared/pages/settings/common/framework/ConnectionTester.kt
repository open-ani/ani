package me.him188.ani.app.ui.settings.framework

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.settings.SettingsScope
import me.him188.ani.app.ui.subject.episode.mediaFetch.getMediaSourceIcon
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSource
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSourceDescription
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue


enum class ConnectionTestResult {
    SUCCESS,
    FAILED,
    NOT_ENABLED
}

fun Boolean.toConnectionTestResult() =
    if (this) ConnectionTestResult.SUCCESS else ConnectionTestResult.FAILED

@Stable
class ConnectionTester(
    val id: String,
    private val testConnection: suspend () -> ConnectionTestResult,
) {
    var isTesting by mutableStateOf(false)
    var result: ConnectionTestResult? by mutableStateOf(null)
    var time: Duration? by mutableStateOf(null)

    fun reset() {
        isTesting = false
        result = null
        time = null
    }

    suspend fun test() {
        withContext(Dispatchers.Main) {
            isTesting = true
        }
        try {
            val (res, t) = measureTimedValue { testConnection() }
            withContext(Dispatchers.Main) {
                time = t
                result = res
            }
        } catch (e: Throwable) {
            withContext(Dispatchers.Main) {
                time = Duration.INFINITE
                result = ConnectionTestResult.FAILED
            }
            throw e
        } finally {
            // We can't use `withContext` be cause this scope has already been cancelled
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.Main) {
                isTesting = false
            }
        }
    }
}

@Composable
fun SettingsScope.MediaSourceTesterView(
    tester: ConnectionTester,
    showTime: Boolean,
    title: @Composable RowScope.() -> Unit = { Text(remember(tester.id) { renderMediaSource(tester.id) }) },
    description: (@Composable () -> Unit)? = if (tester.id == BangumiSubjectProvider.ID) {
        { Text("提供观看记录数据，无需代理") }
    } else {
        renderMediaSourceDescription(tester.id)?.let {
            { Text(it) }
        }
    },
    icon: (@Composable () -> Unit)? = {
        val ic = getMediaSourceIcon(tester.id)
        Image(
            ic
                ?: rememberVectorPainter(Icons.Rounded.DisplaySettings),
            null,
            Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            colorFilter = if (ic == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurface) else null,
        )
    },
) {
    TextItem(
        title = title,
        icon = icon,
        description = description,
        action = {
            if (tester.isTesting) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                when (tester.result) {
                    ConnectionTestResult.SUCCESS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                            if (showTime) {
                                if (tester.time == Duration.INFINITE) {
                                    Text("超时")
                                } else {
                                    Text(
                                        tester.time?.toString(
                                            DurationUnit.SECONDS,
                                            decimals = 2
                                        ) ?: ""
                                    )
                                }
                            }
                        }
                    }

                    ConnectionTestResult.FAILED -> {
                        Icon(Icons.Rounded.Cancel, null, tint = MaterialTheme.colorScheme.error)
                    }

                    ConnectionTestResult.NOT_ENABLED -> {
                        Text("未启用")
                    }

                    null -> {
                        Text("等待测试")
                    }
                }
            }
        }
    )
}
