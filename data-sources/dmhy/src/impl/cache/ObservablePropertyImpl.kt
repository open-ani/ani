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

package me.him188.animationgarden.datasources.dmhy.impl.cache

import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentLinkedQueue

internal fun <T : Any> MutableObservablePropertyImpl(): MutableObservablePropertyImpl<T?> {
    return MutableObservablePropertyImpl(null)
}

internal class MutableObservablePropertyImpl<T>(initial: T) : MutableObservableProperty<T> {
    private val _value = atomic(initial)
    override var value: T
        get() = _value.value
        set(value) {
            _value.value = value
            notifyObservers(value)
        }

    private val _observers: MutableCollection<Observer<T>> = ConcurrentLinkedQueue()
    override val observers: Iterator<Observer<T>> get() = _observers.iterator()

    override fun addObserver(observer: Observer<T>) {
        _observers.add(observer)
        observer.onAttach()
    }

    override fun removeObserver(observer: Observer<T>) {
        _observers.remove(observer)
        observer.onDetach()
    }

    private fun notifyObservers(newValue: T) {
        for (observer in _observers) {
            observer.onChange(newValue)
        }
    }
}