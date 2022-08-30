/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.app.data

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface MutableProperty<T> {
    suspend fun get(): T
    suspend fun set(value: T)
}

class InMemoryMutableProperty<T>(
    private val initial: suspend () -> T
) : MutableProperty<T> {
    private val value = atomic<T?>(null)
    private val lock = Mutex()

    private suspend fun initValue(): T {
        lock.withLock {
            val initial = initial()
            value.compareAndSet(null, initial)
        }
        return value.value!!
    }

    override suspend fun get(): T {
        return this.value.value ?: initValue()
    }

    override suspend fun set(value: T) {
        this.value.value = value
    }
}
