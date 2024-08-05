package me.him188.ani.app

import android.content.Context
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun checkMockContext() {
        val mockContext = mock<Context> {
            on { packageName } doReturn "me.him188.ani"
        }

        assertEquals("me.him188.ani", mockContext.packageName)
    }
}