package me.him188.ani.app.data.source.media.selector

import androidx.compose.runtime.Stable
import me.him188.ani.utils.coroutines.Symbol
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 一个可选的偏好, 有三个状态:
 *
 * - 偏好一个值, 例如: 使用简体中文过滤
 * - 偏好没有值, 例如: 不要用字幕语言过滤
 * - 没有偏好, 例如: 使用默认设置
 */
@Stable
@JvmInline
value class OptionalPreference<@Suppress("unused") T : Any> private constructor(
    @PublishedApi internal val rawValue: Any
) {
    /**
     * 当偏好一个值时返回 `true`, 例如: 使用简体中文过滤
     */
    val isPreferValue: Boolean get() = rawValue !== PREFER_NO_VALUE && rawValue !== NO_PREFERENCE

    /**
     * 当偏好没有值时返回 `true`, 例如: 不要用字幕语言过滤
     */
    val isPreferNoValue: Boolean get() = rawValue === PREFER_NO_VALUE

    /**
     * 当有偏好时返回 `true`, 可以是偏好一个值 [isPreferValue] 也可以是偏好没有值 [isPreferNoValue]
     */
    val hasPreference: Boolean get() = rawValue !== NO_PREFERENCE

    val hasNoPreference: Boolean get() = rawValue === NO_PREFERENCE

    companion object {
        fun <T : Any> prefer(value: T): OptionalPreference<T> = OptionalPreference(value)
        fun <T : Any> preferIfNotNull(value: T?): OptionalPreference<T> =
            if (value == null) noPreference() else prefer(value)

        fun <T : Any> preferNoValue(): OptionalPreference<T> = OptionalPreference(PREFER_NO_VALUE)
        fun <T : Any> noPreference(): OptionalPreference<T> = OptionalPreference(NO_PREFERENCE)
    }
}

private val PREFER_NO_VALUE = Symbol("PREFER_NO_VALUE")
private val NO_PREFERENCE = Symbol("NO_PREFERENCE")

inline val <T : Any> OptionalPreference<T>.preferredValueOrNull: T?
    get() =
        @Suppress("UNCHECKED_CAST")
        if (isPreferValue) rawValue as T else null

inline val <T : Any> OptionalPreference<T>.preferredValueOrFail: T
    get() =
        @Suppress("UNCHECKED_CAST")
        if (isPreferValue) rawValue as T else throw IllegalStateException("No value is preferred")

/**
 * 返回偏好的值, 当[无偏好][OptionalPreference.hasNoPreference]时使用 [default].
 */
inline fun <T : R, R> OptionalPreference<T & Any>.orElse(default: () -> R): R? {
    contract { callsInPlace(default, InvocationKind.AT_MOST_ONCE) }
    return when {
        isPreferNoValue -> null
        isPreferValue -> return preferredValueOrNull
        else -> return default()
    }
}

/**
 * 返回偏好的值, 当[无偏好][OptionalPreference.hasNoPreference]时使用 [default].
 */
inline fun <T : Any> OptionalPreference<T>.flatMapNoPreference(
    default: () -> OptionalPreference<T>
): OptionalPreference<T> {
    contract { callsInPlace(default, InvocationKind.AT_MOST_ONCE) }
    if (this.hasNoPreference) {
        return default()
    }
    return this
}
