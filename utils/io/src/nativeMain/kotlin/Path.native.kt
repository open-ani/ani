package me.him188.ani.utils.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import me.him188.ani.utils.platform.Uuid
import platform.posix.getenv

actual fun SystemPath.length(): Long = SystemFileSystem.metadataOrNull(path)?.size ?: 0

actual fun SystemPath.isDirectory(): Boolean = SystemFileSystem.metadataOrNull(path)?.isDirectory ?: false

actual fun SystemPath.isRegularFile(): Boolean = SystemFileSystem.metadataOrNull(path)?.isRegularFile ?: false

actual fun SystemPath.moveDirectoryRecursively(target: SystemPath, visitor: ((SystemPath) -> Unit)?) {
    // TODO: move directory recursively for native target
}

actual inline fun <T> SystemPath.useDirectoryEntries(block: (Sequence<SystemPath>) -> T): T {
    return block(SystemFileSystem.list(path).asSequence().map { SystemPath(it) })
}

private fun resolveImpl(parent: String, child: String): String {
    if (child.isEmpty()) return parent
    if (child[0] == '/') {
        if (parent == "/") return child
        return parent + child
    }
    if (parent == "/") return parent + child
    return "$parent/$child"
}

@OptIn(ExperimentalForeignApi::class)
actual val SystemPath.absolutePath: String
    get() {
        val pwd = getenv("pwd")?.toKString() ?: error("Cannot get current working directory")
        return resolveImpl(pwd, path.toString())
    }

actual fun SystemPaths.createTempDirectory(
    prefix: String,
): SystemPath = SystemPath(SystemTemporaryDirectory.resolve(prefix + Uuid.randomString().take(8))).apply {
    createDirectories()
}

actual fun SystemPaths.createTempFile(
    prefix: String,
    suffix: String
): SystemPath = SystemPath(SystemTemporaryDirectory.resolve(prefix + Uuid.randomString().take(8) + suffix)).apply {
    writeBytes(byteArrayOf())
}
