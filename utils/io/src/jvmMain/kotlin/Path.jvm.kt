package me.him188.ani.utils.io

import kotlinx.io.files.Path
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.useDirectoryEntries
import java.nio.file.Path as NioPath

fun Path.toFile(): File = File(this.toString())
fun SystemPath.toFile(): File = path.toFile()

fun Path.toNioPath(): NioPath = Paths.get(this.toString())
fun SystemPath.toNioPath(): NioPath = path.toNioPath()

fun NioPath.toKtPath(): Path = Path(this.toString())
fun File.toKtPath(): Path = Path(this.toString())

actual inline fun <T> SystemPath.useDirectoryEntries(block: (Sequence<SystemPath>) -> T): T {
    return this.toNioPath().useDirectoryEntries { seq ->
        block(seq.map { SystemPath(it.toKtPath()) })
    }
}

actual fun SystemPath.length(): Long {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.length()
}

actual fun SystemPath.isDirectory(): Boolean {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.isDirectory
}

actual fun SystemPath.isRegularFile(): Boolean {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.isFile
}

actual val SystemPath.absolutePath: String
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    get() = path.file.absolutePath

actual fun SystemPaths.createTempDirectory(
    prefix: String,
): SystemPath = kotlin.io.path.createTempDirectory(prefix = prefix).toKtPath().inSystem

actual fun SystemPaths.createTempFile(
    prefix: String,
    suffix: String
): SystemPath = kotlin.io.path.createTempFile(prefix = prefix, suffix = suffix).toKtPath().inSystem
