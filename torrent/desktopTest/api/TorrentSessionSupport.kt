package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal abstract class TorrentSessionSupport {
    @TempDir
    lateinit var tempDir: File

    inline fun CoroutineScope.withSession(
        block: DefaultTorrentDownloadSession.() -> Unit
    ) {
        val session = DefaultTorrentDownloadSession(
            "test",
            tempDir,
            {},
            true,
            SupervisorJob()
        )
        coroutineContext.job.invokeOnCompletion {
            session.close()
        }
        session.block()
    }

    inline fun DefaultTorrentDownloadSession.setHandle(
        name: String = "test",
        builderAction: TestAniTorrentHandle.() -> Unit = {},
    ) {
        val handle = TestAniTorrentHandle(name)
        listener.onEvent(
            TorrentAddEvent(handle.apply(builderAction))
        )
    }
}