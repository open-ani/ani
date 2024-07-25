package me.him188.ani.app.data.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResponseTest {
    @Test
    fun `unwrap success`() {
        val result = ApiResponse.success(1)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isFailure)
        assertEquals(false, result.isException)

        assertEquals(1, result.getOrNull())
        assertEquals(null, result.failureOrNull())
        assertEquals(null, result.exceptionOrNull())
    }
}