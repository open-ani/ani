package me.him188.ani.app.update

import me.him188.ani.app.platform.ContextMP
import java.io.File

interface UpdateInstaller {
    fun install(file: File, context: ContextMP)
}
