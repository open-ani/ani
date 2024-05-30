package me.him188.ani.datasources.ikaros

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MediaMatch

class IkarosSizeSource(
    override val results: Flow<MediaMatch>,
    override val finished: Flow<Boolean>,
    override val totalSize: Flow<Int?>
) :SizedSource<MediaMatch> {
}