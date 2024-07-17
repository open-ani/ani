package me.him188.ani.app.desktop

import dev.dirs.ProjectDirectories
import me.him188.ani.app.platform.AniBuildConfigDesktop
import java.io.File

val projectDirectories: ProjectDirectories by lazy {
    ProjectDirectories.from(
        "me",
        "Him188",
        if (AniBuildConfigDesktop.isDebug) "Ani-debug" else "Ani",
    )
}

val ProjectDirectories.torrentCacheDir: File
    get() = File(cacheDir).resolve("torrent-data")