package me.him188.animationgarden.api.impl.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.model.TopicCategory
import me.him188.animationgarden.api.model.User
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.api.tags.Tag

internal fun interface Observer<T> {
    fun onChange(newValue: T)

    fun onAttach() {}
    fun onDetach() {}
}

internal interface ObservableProperty<T> {
    val value: T

    val observers: Iterator<Observer<T>>
    fun addObserver(observer: Observer<T>)
    fun removeObserver(observer: Observer<T>)
}

internal interface MutableObservableProperty<T> : ObservableProperty<T> {
    override var value: T
}


interface Cache {
    val users: KeyedMutableListFlow<String, User>
    val categories: KeyedMutableListFlow<String, TopicCategory>
    val alliances: KeyedMutableListFlow<String, Alliance>
    val subtitleLanguages: KeyedMutableListFlow<String, SubtitleLanguage>
    val tags: KeyedMutableListFlow<String, Tag>

    fun mergeFrom(cache: Cache)
}

internal class CacheImpl : Cache {
    override val users: KeyedMutableListFlow<String, User> = KeyedMutableListFlowImpl { it.id }
    override val categories: KeyedMutableListFlow<String, TopicCategory> = KeyedMutableListFlowImpl { it.id }
    override val alliances: KeyedMutableListFlow<String, Alliance> = KeyedMutableListFlowImpl { it.id }
    override val subtitleLanguages: KeyedMutableListFlow<String, SubtitleLanguage> = KeyedMutableListFlowImpl { it.id }
    override val tags: KeyedMutableListFlow<String, Tag> = KeyedMutableListFlowImpl { it.id }

    override fun mergeFrom(cache: Cache) {
        users.mutate { list -> (list + cache.users).distinctBy { it.id } }
        categories.mutate { list -> (list + cache.categories).distinctBy { it } }
    }

    override fun toString(): String {
        return buildString {
            appendLine("Cache {")
            appendLine("  users: $users")
            appendLine("  categories: $categories")
            appendLine("}")
        }
    }
}

interface MutableListFlow<T : Any> : Iterable<T> {
    var value: List<T>
    fun asFlow(): StateFlow<List<T>>
    fun asMutableFlow(): MutableStateFlow<List<T>>
    fun asList(): List<T>
    fun asSequence(): Sequence<T> = value.asSequence()
}

interface KeyedMutableListFlow<K, T : Any> : MutableListFlow<T> {
    operator fun get(key: K): T?
    operator fun set(key: K, value: T?)
}

inline fun <K, T : Any> KeyedMutableListFlow<K, T>.getOrSet(key: K, default: () -> T): T {
    var value = get(key)
    if (value == null) {
        value = default()
        set(key, value)
    }
    return value
}

inline fun <K, T : R, R> KeyedMutableListFlow<K, T & Any>.getOrDefault(key: K, default: () -> R): R {
    return get(key) ?: return default()
}

open class MutableListFlowImpl<T : Any>(
    private val delegate: MutableStateFlow<List<T>> = MutableStateFlow(listOf())
) :
    Iterable<T>, MutableListFlow<T> {
    override var value: List<T>
        get() = delegate.value
        set(value) {
            delegate.value = value
        }

    override fun asFlow(): StateFlow<List<T>> = delegate

    override fun asMutableFlow(): MutableStateFlow<List<T>> = delegate

    private val list by lazy { DynamicDelegateList { delegate.value } }
    override fun asList(): List<T> = list

    override operator fun iterator(): Iterator<T> = value.iterator()
}

class KeyedMutableListFlowImpl<K, T : Any>(
    private val getKey: (T) -> K
) : KeyedMutableListFlow<K, T>, MutableListFlowImpl<T>() {
    private inline val T.key get() = getKey(this)

    override fun get(key: K): T? {
        return this.asSequence().find { it.key == key }
    }

    override fun set(key: K, value: T?) {
        return mutate { list ->
            buildList(list.size) {
                for (element in list) {
                    if (element.key == key) {
                        if (value != null) add(value)
                    } else {
                        add(element)
                    }
                }
            }
        }
    }

}

inline fun <T : Any> MutableListFlow<T>.mutate(transform: (List<T>) -> List<T>) {
    value = value.let(transform)
}

private class DynamicDelegateList<T>(
    private val supplier: () -> List<T>
) : List<T>, AbstractList<T>() {
    override val size: Int get() = supplier().size
    override fun get(index: Int): T = supplier()[index]
    override fun isEmpty(): Boolean = supplier().isEmpty()
    override fun iterator(): Iterator<T> = supplier().iterator()
    override fun listIterator(): ListIterator<T> = supplier().listIterator()
    override fun listIterator(index: Int): ListIterator<T> = supplier().listIterator()
    override fun subList(fromIndex: Int, toIndex: Int): List<T> = supplier().subList(fromIndex, toIndex)
    override fun lastIndexOf(element: T): Int = supplier().lastIndexOf(element)
    override fun indexOf(element: T): Int = supplier().indexOf(element)
    override fun containsAll(elements: Collection<T>): Boolean = supplier().containsAll(elements)
    override fun contains(element: T): Boolean = supplier().contains(element)
}

inline fun <T> MutableStateFlow<T>.mutate(block: (T) -> T) {
    value = value.let(block)
}
