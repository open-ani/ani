package me.him188.ani.app.data.models

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

sealed interface ApiFailure {
    data object Unauthorized : ApiFailure
    data object NetworkError : ApiFailure // IOException
    data object ServiceUnavailable : ApiFailure // 500..599
}

/**
 * 用于需要授权的 HTTP 请求, 将已知错误解析为 [ApiFailure].
 */
@JvmInline
value class ApiResponse<out T> private constructor(
    private val value: Any?,
) {
    val isSuccess: Boolean get() = !isFailure

    val isFailure: Boolean get() = value is ApiFailure

    fun getOrNull(): T? =
        @Suppress("UNCHECKED_CAST")
        if (isSuccess) value as T else null

    fun failureOrNull(): ApiFailure? = value as? ApiFailure

    fun <T> getOrThrow(): T {
        val value = value
        @Suppress("UNCHECKED_CAST")
        return if (isSuccess) value as T else {
            check(value is ApiFailure)
            throw IllegalStateException("Request failed: $value")
        }
    }

    companion object {
        fun <T> success(value: T): ApiResponse<T> {
            require(value !is ApiFailure) { "value must not be a RequestFailure" }
            return ApiResponse(value)
        }

        fun <T> failure(failure: ApiFailure): ApiResponse<T> {
            return ApiResponse(failure)
        }
    }
}

inline fun <T> runApiRequest(block: () -> T): ApiResponse<T> {
    try {
        return ApiResponse.success(block())
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.Unauthorized || e.response.status == HttpStatusCode.Forbidden) {
            return ApiResponse.failure(ApiFailure.Unauthorized)
        }
        throw e
    } catch (e: ServerResponseException) {
        return ApiResponse.failure(ApiFailure.ServiceUnavailable)
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        return ApiResponse.failure(ApiFailure.NetworkError)
    }
}


inline fun <T, R> ApiResponse<T>.map(transform: (T) -> R): ApiResponse<R> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (isSuccess) {
        @Suppress("UNCHECKED_CAST")
        ApiResponse.success(transform(getOrNull() as T))
    } else {
        @Suppress("UNCHECKED_CAST")
        this as ApiResponse<R>
    }
}

inline fun <T : R, R> ApiResponse<T>.valueOrElse(
    block: () -> R,
): R {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isSuccess) {
        @Suppress("UNCHECKED_CAST")
        getOrNull() as T
    } else {
        block()
    }
}

inline fun <T, R> ApiResponse<T>.fold(
    onSuccess: (value: T) -> R,
    onKnownFailure: (ApiFailure) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onKnownFailure, InvocationKind.AT_MOST_ONCE)
    }

    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> onSuccess(getOrNull() as T) // T can be null
        else -> onKnownFailure(failureOrNull()!!)
    }
}
