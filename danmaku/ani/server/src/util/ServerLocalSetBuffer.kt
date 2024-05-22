package me.him188.ani.danmaku.server.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.server.ServerConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.util.concurrent.ConcurrentHashMap


interface ServerLocalSetBuffer {
    suspend fun put(value: String): Boolean
    suspend fun contains(value: String): Boolean
}

class SimpleServerLocalSetBuffer : ServerLocalSetBuffer {
    private val set = mutableSetOf<String>()

    override suspend fun put(value: String): Boolean {
        return set.add(value)
    }

    override suspend fun contains(value: String): Boolean {
        return set.contains(value)
    }
}


class FileServerLocalSetBuffer private constructor(
    filename: String,
) : ServerLocalSetBuffer, KoinComponent {
    private val file = File(get<ServerConfig>().rootDir.path, "buffer/$filename.txt")
    private val set = mutableSetOf<String>()
    private val mutex = Mutex()

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (file.exists()) {
                file.readLines().forEach {
                    set.add(it)
                }
            } else {
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
            }
        }
    }

    override suspend fun put(value: String): Boolean {
        mutex.withLock {
            val result = set.add(value)
            if (result) {
                withContext(Dispatchers.IO) {
                    file.appendText("$value\n")
                }
            }
            return result
        }
    }

    override suspend fun contains(value: String): Boolean {
        mutex.withLock {
            return set.contains(value)
        }
    }

    companion object {
        private val buffers = ConcurrentHashMap<String, ServerLocalSetBuffer>()

        suspend fun get(filename: String): ServerLocalSetBuffer {
            return buffers.getOrPut(filename) {
                FileServerLocalSetBuffer(filename).also {
                    it.initialize()
                }
            }
        }
    }
}