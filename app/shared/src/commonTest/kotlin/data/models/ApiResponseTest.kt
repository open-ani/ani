package me.him188.ani.app.data.models

import kotlinx.io.IOException
import me.him188.ani.utils.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class ApiResponseTest {
    @Test
    fun `unwrap success`() {
        val result = ApiResponse.success(1)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isFailure)

        assertEquals(1, result.getOrNull())
        assertEquals(1, result.getOrThrow())
        assertEquals(null, result.failureOrNull())
    }

    @Test
    fun failure() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isFailure)

        assertEquals(null, result.getOrNull())
        assertEquals(ApiFailure.NetworkError, result.failureOrNull())
    }

    @Test
    fun `unwrap null`() {
        val result = ApiResponse.success(null)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isFailure)

        assertEquals(null, result.getOrNull())
        assertEquals(null, result.getOrThrow())
        assertEquals(null, result.failureOrNull())
    }

    ///////////////////////////////////////////////////////////////////////////
    // getOrThrow
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `getOrThrow success`() {
        val result = ApiResponse.success(1)
        assertEquals(1, result.getOrThrow())
    }

    @Test
    fun `getOrThrow failure`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertFailsWith<IllegalStateException> {
            result.getOrThrow()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // map
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `map success`() {
        val result = ApiResponse.success(1)
        val mapped = result.map { it + 1 }
        assertEquals(2, mapped.getOrNull())
    }

    @Test
    fun `map to supertype`() {
        val result = ApiResponse.success(1)
        val mapped = result.map<_, Number> { 1.0 }
        assertEquals(1.0, mapped.getOrNull())
    }

    @Test
    fun `map failure`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        val mapped = result.map { it + 1 }
        assertEquals(ApiFailure.NetworkError, mapped.failureOrNull())
    }

    @Test
    fun `map null`() {
        val result = ApiResponse.success(null)
        val mapped = result.map { 1 }
        assertEquals(1, mapped.getOrNull())
    }

    @Test
    fun `map null to supertype`() {
        val result = ApiResponse.success(null)
        val mapped = result.map<_, Number> { 1.0 }
        assertEquals(1.0, mapped.getOrNull())
    }

    ///////////////////////////////////////////////////////////////////////////
    // valueOrElse
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `valueOrElse success`() {
        val result = ApiResponse.success(1)
        assertEquals(1, result.valueOrElse { 2 })
    }

    @Test
    fun `valueOrElse failure`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertEquals(2, result.valueOrElse { 2 })
    }

    @Test
    fun `valueOrElse failure to null`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertEquals(null, result.valueOrElse { null })
    }

    @Test
    fun `valueOrElse null`() {
        val result = ApiResponse.success(null)
        assertEquals(null, result.valueOrElse { 2 })
    }

    @Test
    fun `valueOrElse to null`() {
        val result = ApiResponse.success(1)
        assertEquals(1, result.valueOrElse { null })
    }

    ///////////////////////////////////////////////////////////////////////////
    // fold
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `fold success`() {
        val result = ApiResponse.success(1)
        assertEquals(2, result.fold({ it + 1 }, { 2 }))
    }

    @Test
    fun `fold success to null`() {
        val result = ApiResponse.success(1)
        assertEquals(null, result.fold({ null }, { 2 }))
    }

    @Test
    fun `fold failure`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertEquals(2, result.fold({ 1 }, { 2 }))
    }

    @Test
    fun `fold failure to null`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        assertEquals(null, result.fold({ 1 }, { null }))
    }

    @Test
    fun `fold null`() {
        val result = ApiResponse.success(null)
        assertEquals(1, result.fold({ 1 }, { 2 }))
    }

    ///////////////////////////////////////////////////////////////////////////
    // flatMap
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `flatMap success`() {
        val result = ApiResponse.success(1)
        val mapped = result.flatMap { ApiResponse.success(it + 1) }
        assertEquals(2, mapped.getOrNull())
    }

    @Test
    fun `flatMap success to another type`() {
        val result = ApiResponse.success(1)
        val mapped = result.flatMap { ApiResponse.success(it + 1.0) }
        assertEquals(2.0, mapped.getOrNull())
    }

    @Test
    fun `flatMap failure to success`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        val mapped = result.flatMap { ApiResponse.success(it + 1) }
        assertEquals(ApiFailure.NetworkError, mapped.failureOrNull())
    }

    @Test
    fun `flatMap null to success`() {
        val result = ApiResponse.success(null)
        val mapped = result.flatMap { ApiResponse.success(1) }
        assertEquals(1, mapped.getOrNull())
    }

    @Test
    fun `flatMap null to failure`() {
        val result = ApiResponse.success(null)
        val mapped = result.flatMap { ApiResponse.failure<Int>(ApiFailure.NetworkError) }
        assertEquals(ApiFailure.NetworkError, mapped.failureOrNull())
    }

    @Test
    fun `flatMap failure to null`() {
        val result = ApiResponse.failure<Int>(ApiFailure.NetworkError)
        val mapped = result.flatMap { ApiResponse.success(null) }
        assertEquals(ApiFailure.NetworkError, mapped.failureOrNull())
    }

    ///////////////////////////////////////////////////////////////////////////
    // runApiRequest
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `runApiRequest success`() {
        val result = runApiRequest { 1 }
        assertEquals(true, result.isSuccess)
        assertEquals(1, result.getOrNull())
    }

    @Test
    fun `runApiRequest failure with IOException`() {
        val result = runApiRequest {
            throw IOException()
        }
        assertEquals(false, result.isSuccess)
        assertEquals(ApiFailure.NetworkError, result.failureOrNull())
    }

    @Test
    fun `runApiRequest failure with CancellationException`() {
        val e = CancellationException()
        assertEquals(
            e,
            assertFails {
                runApiRequest { throw e }
            },
        )
    }

    @Test
    fun `runApiRequest failure with other exceptions`() {
        assertFailsWith<IllegalStateException> {
            runApiRequest {
                throw NoSuchElementException("a bug")
            }
        }
    }
}
