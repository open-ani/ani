package me.him188.animationgarden.api.impl.model

import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("FunctionName")
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