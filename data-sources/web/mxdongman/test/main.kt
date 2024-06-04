package me.him188.ani.datasources.nyafun

import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.delay
import me.him188.ani.utils.ktor.createDefaultHttpClient
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val outFile = File("bangumi.html")
    createDefaultHttpClient() {
        BrowserUserAgent()
    }.get("https://www.mxdm4.com/dongman/1850.html") {
    }.bodyAsChannel().copyTo(outFile.writeChannel())
    delay(3.seconds)
    exitProcess(0)

//    NyafunMediaSource(MediaSourceConfig()).run {
//        File("play.html").writeText(client.get("https://www.nyafun.net/play/7168-1-1.html").bodyAsText())
//    }
//    exitProcess(0)
}
