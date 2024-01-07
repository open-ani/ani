/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.framework

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.shared.Uuid


fun <T> LoadingUuidItem(uuid: Uuid, ready: T?): LoadingUuidItem<T> =
    ComputedLoadingUuidItem(uuid, ready)

fun <T> LoadingUuidItem(uuid: Uuid, ready: StateFlow<T?>): LoadingUuidItem<T> =
    StateFlowLoadingUuidItem(uuid, ready)

abstract class LoadingUuidItem<T> {
    abstract val ready: T?
    abstract val uuid: Uuid

    @Stable
    abstract fun asFlow(): Flow<T>

    abstract override fun toString(): String

    final override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (other !is LoadingUuidItem<*>) return false
        return other.uuid == this.uuid && this.ready == other.ready
    }

    override fun hashCode(): Int {
        var result = ready?.hashCode() ?: 0
        result = 31 * result + uuid.hashCode()
        return result
    }
}

@Stable
internal class ComputedLoadingUuidItem<T>(
    override val uuid: Uuid,
    override val ready: T?,
) : LoadingUuidItem<T>() {
    override fun asFlow(): Flow<T> = if (ready == null) emptyFlow() else flowOf(ready)

    override fun toString(): String = "ComputedLoadingUuidItem(uuid=$uuid, ready=$ready)"
}

internal class StateFlowLoadingUuidItem<T>(
    @Stable
    override val uuid: Uuid,
    @Stable
    val flow: StateFlow<T?>,
) : LoadingUuidItem<T>() {
    override val ready: T?
        get() = flow.value

    override fun asFlow(): Flow<T> = flow.filterNotNull()

    override fun toString(): String = "StateFlowLoadingUuidItem(uuid=$uuid, ready=$ready)"
}

