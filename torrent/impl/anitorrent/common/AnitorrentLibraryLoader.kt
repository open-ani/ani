package me.him188.ani.app.torrent.anitorrent

import me.him188.ani.app.platform.Arch
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.torrent.api.TorrentLibraryLoader
import java.io.File

private fun loadAnitorrentLibrary(libraryName: String) {
    val platform = currentPlatform as Platform.Desktop
    val filename = when (platform) {
        is Platform.Linux -> "lib$libraryName.so"
        is Platform.MacOS -> "lib$libraryName.dylib"
        is Platform.Windows -> "$libraryName.dll"
    }
    System.getProperty("compose.application.resources.dir")?.let {
        val file = File(it).resolve(filename)
        if (file.exists()) {
            System.load(file.absolutePath)
            return
        }
    }

    val arch = when (platform.arch) {
        Arch.X86_64 -> "x64"
        Arch.AARCH64 -> "arm64"
    }

    val triple = when (platform) {
        is Platform.Linux -> "linux-$arch"
        is Platform.MacOS -> "macos-$arch"
        is Platform.Windows -> "windows-$arch"
    }

    System.getProperty("user.dir")?.let { File(it) }?.resolve("../appResources/$triple/$filename")
        ?.let {
            if (it.exists()) {
                System.load(it.absolutePath)
                return
            }
        }

    throw UnsatisfiedLinkError("Could not find anitorrent library: $filename")
}

object AnitorrentLibraryLoader : TorrentLibraryLoader {

    @Volatile
    private var libraryLoaded = false

    private val _initAnitorrent by lazy {
        // 注意, JVM 也会 install signal handler, 它需要 sig handler 才能工作. 
        // 这里覆盖之后会导致 JVM crash (SIGSEGV/SIGBUS). crash 如果遇到一个无 symbol 的比较低的地址, 那就大概率是 JVM.
        // 应当仅在需要 debug 一个已知的 anitorrent 的 crash 时才开启这个.
        // 其实不开的话, OS 也能输出 crash report. macOS 输出的 crash report 会包含 native 堆栈.

        // 如果需要调试, 可以在 anitorrent 搜索 ENABLE_TRACE_LOGGING 并修改为 true. 将会打印非常详细的 function call 记录.

//    anitorrent.install_signal_handlers()
    }

    @Synchronized
    @Throws(UnsatisfiedLinkError::class)
    override fun loadLibraries() {
        if (libraryLoaded) return

        try {
            when (currentPlatform as Platform.Desktop) {
                is Platform.Windows -> {
                    loadAnitorrentLibrary("libcrypto-3-x64")
                    loadAnitorrentLibrary("libssl-3-x64")
                    loadAnitorrentLibrary("torrent-rasterbar")
                }

                is Platform.Linux -> throw UnsupportedOperationException("Linux is not supported yet")
                is Platform.MacOS -> {
                    loadAnitorrentLibrary("torrent-rasterbar.2.0.10")
                }
            }
            loadAnitorrentLibrary("anitorrent")
            _initAnitorrent
            libraryLoaded = true
        } catch (e: Throwable) {
            libraryLoaded = false
            throw e
        }
    }
}