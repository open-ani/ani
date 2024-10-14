/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.tools.ldc

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A remote source of data.
 */
@Stable
interface Source<T> {
    /**
     * A flow of data flows.
     *
     * [dataFlows] emits `Flow<T>`, which represents the data loaded from the remote.
     * [dataFlows] is a nested flow because the remote may change and hence the consumer should collect from the new data flow.
     */
    val dataFlows: Flow<Flow<T>>

    companion object {
        /**
         * Construct a [Source] from a flow of data flows.
         *
         * When [dataFlows] emits a new data flow, all the local caches should be cleared and all the data should be reloaded from the new flow.
         */
        fun <T> restartable(
            dataFlows: Flow<Flow<T>>,
        ): Source<T> = object : Source<T> {
            override val dataFlows: Flow<Flow<T>> = dataFlows
        }

        /**
         * Constructs a [Source] with a single data flow.
         */
        fun <T> of(
            data: Flow<T>,
        ): Source<T> = object : Source<T> {
            override val dataFlows: Flow<Flow<T>> = flowOf(data)
        }
    }
}