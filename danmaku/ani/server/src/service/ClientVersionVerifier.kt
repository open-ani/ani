package me.him188.ani.danmaku.server.service

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred

interface ClientVersionVerifier {
    suspend fun verify(clientVersion: String): Boolean
}

class GithubVersionVerifierImpl : ClientVersionVerifier {
    private val bufferDeferred: CompletableDeferred<ServerLocalSetBuffer> = CompletableDeferred()
    private val buffer get() = bufferDeferred.getCompleted()
    private val httpClient by lazy {
        HttpClient()
    }
    private val githubReleasesUrl = "https://api.github.com/repos/open-ani/ani/releases/tags"
    
    override suspend fun verify(clientVersion: String): Boolean {
        if (!bufferDeferred.isCompleted) {
            bufferDeferred.complete(FileServerLocalSetBuffer.get("github-client-versions"))
        }
        
        if (buffer.contains(clientVersion)) {
            return true
        }

        val response = httpClient.head("$githubReleasesUrl/v$clientVersion")
        if (response.status == HttpStatusCode.OK) {
            buffer.put(clientVersion)
            return true
        } else {
            return false
        }
    }
}

class AniClientVersionVerifierImpl(
    private val versionWhitelist: List<String> = listOf(),
    private val versionWhitelistRegex: List<String> = listOf(),
): ClientVersionVerifier {
    private val githubVersionVerifier = GithubVersionVerifierImpl()
    
    override suspend fun verify(clientVersion: String): Boolean {
        if (versionWhitelist.contains(clientVersion)) {
            return true
        }
        if (versionWhitelistRegex.any { clientVersion.matches(it.toRegex()) }) {
            return true
        }
        return githubVersionVerifier.verify(clientVersion)
    }
}
