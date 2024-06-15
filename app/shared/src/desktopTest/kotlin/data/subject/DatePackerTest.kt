package me.him188.ani.app.data.subject

import kotlin.test.Test
import kotlin.test.assertEquals

class DatePackerTest {
    @Test
    fun `can pack`() {
        val pack = DatePacker.pack(2024, 5, 18)
        assertEquals(2024, DatePacker.unpack1(pack))
        assertEquals(5, DatePacker.unpack2(pack))
        assertEquals(18, DatePacker.unpack3(pack))
    }
}
