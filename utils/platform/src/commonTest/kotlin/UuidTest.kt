package me.him188.ani.utils.platform

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class UuidTest {
    @Test
    fun random() {
        assertEquals("026e07f2-752f-46f3-9b5b-7680d00b6d25", Uuid.random(Random(100L)).toString())
        assertEquals("1a2ac523-b005-41a4-a148-93e06d33b6a0", Uuid.random(Random(1L)).toString())
    }

    @Test
    fun randomString() {
        assertEquals("026e07f2-752f-46f3-9b5b-7680d00b6d25", Uuid.randomString(Random(100L)))
        assertEquals("1a2ac523-b005-41a4-a148-93e06d33b6a0", Uuid.randomString(Random(1L)))
    }
}
