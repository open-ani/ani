package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastAll
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.mediaSource.renderMediaSource
import me.him188.ani.app.ui.mediaSource.renderMediaSourceDescription
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SorterItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.app.ui.subject.episode.details.renderResolution
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes

@Composable
internal fun SettingsScope.MediaSelectionGroup(vm: MediaSettingsViewModel) {
    Group(
        title = {
            Text("资源选择偏好")
        },
        description = {
            Column {
                Text("设置默认的资源选择偏好。将同时影响在线播放和缓存")
                Text("每部番剧在播放时的选择将覆盖这里的设置")
            }
        },
    ) {
        val textAny = "任意"
        val textNone = "无"

        SorterItem(
            values = { vm.sortedMediaSources },
            onSort = { list ->
                vm.updateDefaultMediaPreference(
                    // 总是启用本地并且在最高优先级
                    vm.defaultMediaPreference.copy(
                        fallbackMediaSourceIds = listOf(MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID) + list.filter { it.selected }
                            .map { it.item }
                    )
                )
            },
            exposed = { list ->
                Text(
                    remember(list) {
                        if (list.fastAll { it.selected }) {
                            textAny
                        } else if (list.fastAll { !it.selected }) {
                            textNone
                        } else
                            list.asSequence().filter { it.selected }
                                .joinToString { renderMediaSource(it.item) }
                    },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            item = { Text(renderMediaSource(it)) },
            key = { it },
            modifier = Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            dialogItemDescription = { id ->
                renderMediaSourceDescription(id)?.let { Text(it) }
            },
            dialogDescription = { Text(TIPS_LONG_CLICK_SORT) },
            icon = { Icon(Icons.Rounded.DisplaySettings, null) },
            title = { Text("数据源") },
        )

        HorizontalDividerItem()

        SorterItem(
            values = { vm.sortedLanguages },
            onSort = { list ->
                vm.updateDefaultMediaPreference(
                    vm.defaultMediaPreference.copy(fallbackSubtitleLanguageIds = list.filter { it.selected }
                        .map { it.item })
                )
            },
            exposed = { list ->
                Text(
                    remember(list) {
                        if (list.fastAll { it.selected }) {
                            textAny
                        } else if (list.fastAll { !it.selected }) {
                            textNone
                        } else
                            list.asSequence().filter { it.selected }
                                .joinToString { renderSubtitleLanguage(it.item) }
                    },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            item = { Text(renderSubtitleLanguage(it)) },
            key = { it },
            modifier = Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            dialogDescription = {
                Text(
                    TIPS_LONG_CLICK_SORT
                )
            },
            icon = { Icon(Icons.Rounded.Language, null) },
            title = { Text("字幕语言") },
        )

        HorizontalDividerItem()

        SorterItem(
            values = { vm.sortedResolutions },
            onSort = { list ->
                vm.updateDefaultMediaPreference(
                    vm.defaultMediaPreference.copy(fallbackResolutions = list.filter { it.selected }
                        .map { it.item })
                )
            },
            exposed = { list ->
                Text(
                    remember(list) {
                        if (list.fastAll { it.selected }) {
                            textAny
                        } else if (list.fastAll { !it.selected }) {
                            textNone
                        } else
                            list.asSequence().filter { it.selected }
                                .joinToString { renderResolution(it.item) }
                    },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            item = { Text(renderResolution(it)) },
            key = { it },
            modifier = Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            dialogDescription = { Text(TIPS_LONG_CLICK_SORT) },
            icon = { Icon(Icons.Rounded.Hd, null) },
            title = { Text("分辨率") },
            description = { Text("在播放和手动缓存时，未选择的分辨率也会显示，但不会自动选择") },
        )

        HorizontalDividerItem()

        val allianceRegexes by remember(vm) {
            derivedStateOf { vm.defaultMediaPreference.alliancePatterns?.joinToString() ?: "" }
        }
        TextFieldItem(
            value = allianceRegexes,
            title = { Text("字幕组") },
            modifier = Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            description = {
                Text("支持使用正则表达式，使用逗号分隔。越靠前的表达式的优先级越高\n\n示例: 桜都, 喵萌, 北宇治\n将优先采用桜都字幕组资源，否则采用喵萌，以此类推")
            },
            icon = { Icon(Icons.Rounded.Subtitles, null) },
            placeholder = { Text(textAny) },
            onValueChangeCompleted = { new ->
                vm.updateDefaultMediaPreference(
                    vm.defaultMediaPreference.copy(
                        alliancePatterns = new.split(",", "，").map { it.trim() })
                )
            },
            sanitizeValue = { it.replace("，", ",") },
        )

        HorizontalDividerItem()

        val mediaSelectorSettings by vm.mediaSelectorSettings
        SwitchItem(
            checked = mediaSelectorSettings.showDisabled,
            onCheckedChange = {
                vm.mediaSelectorSettings.update(
                    mediaSelectorSettings.copy(showDisabled = it)
                )
            },
            title = { Text("显示禁用的数据源") },
            Modifier.placeholder(vm.mediaSelectorSettings.loading),
            description = { Text("以便在偏好数据源中未找到资源时，可临时启用禁用的数据源") },
        )

        HorizontalDividerItem()

        SwitchItem(
            checked = vm.defaultMediaPreference.showWithoutSubtitle,
            onCheckedChange = {
                vm.updateDefaultMediaPreference(
                    vm.defaultMediaPreference.copy(showWithoutSubtitle = it)
                )
            },
            title = { Text("显示无字幕资源") },
            Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            description = { Text("这可能是资源本身是生肉，也可能是字幕未识别到。是生肉的可能性更高") },
        )

        HorizontalDividerItem()

        SwitchItem(
            checked = mediaSelectorSettings.hideSingleEpisodeForCompleted,
            onCheckedChange = {
                vm.mediaSelectorSettings.update(
                    mediaSelectorSettings.copy(hideSingleEpisodeForCompleted = it)
                )
            },
            title = { Text("完结后隐藏单集 BT 资源") },
            Modifier.placeholder(vm.mediaSelectorSettings.loading),
            description = { Text("在番剧完结后，单集资源通常会没有速度") },
        )

        HorizontalDividerItem()

        SwitchItem(
            checked = mediaSelectorSettings.preferSeasons,
            onCheckedChange = {
                vm.mediaSelectorSettings.update(
                    mediaSelectorSettings.copy(preferSeasons = it)
                )
            },
            title = { Text("BT 资源优先选择季度全集") },
            Modifier.placeholder(vm.defaultMediaPreferenceLoading),
            description = { Text("季度全集资源通常更快，仅对 BT 数据源有效") },
        )
    }
}

fun autoCacheDescription(sliderValue: Float) = when (sliderValue) {
    0f -> "当前设置: 不自动缓存"
    10f -> "当前设置: 自动缓存全部未观看剧集, "
    else -> "当前设置: 自动缓存观看进度之后的 ${sliderValue.toInt()} 话, " +
            "预计占用空间 ${600.megaBytes * sliderValue}/番剧"
}

private const val TIPS_LONG_CLICK_SORT = "长按排序，优先选择顺序较高的项目。\n" +
        "选中数量越少，查询越快。"
