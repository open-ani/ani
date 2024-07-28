package me.him188.ani.test

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

actual fun Any.readTestResourceAsString(path: String): String {
    // pathParts looks like 
    // [, , test_case_input_one, bin]
    val p = NSBundle.mainBundle
        .pathForResource(
            "resources/${path.substringBeforeLast(".").removePrefix("/")}",
            path.substringAfterLast("."),
        )
    val data = NSData.dataWithContentsOfFile(p!!)
    return data!!.toByteArray().decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}
