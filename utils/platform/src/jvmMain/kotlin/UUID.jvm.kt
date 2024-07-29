package me.him188.ani.utils.platform

import java.util.UUID
import kotlin.random.Random

@JvmInline
actual value class Uuid internal constructor(
    internal val delegate: UuidDelegate,
) {
    actual override fun toString(): String = delegate.toString()

    actual companion object {
        actual fun random(random: Random): Uuid {
            return Uuid(UUID.fromString(generateRandomUuid(random)))
        }

        actual fun randomString(random: Random): String {
            return generateRandomUuid(random)
        }
    }
}

@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias UuidDelegate = UUID
