package me.him188.ani.danmaku.server.service

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.http.HttpStatusCode

interface GithubVersionVerifier {
    suspend fun verify(clientVersion: String): Boolean
}

class GithubVersionVerifierImpl : GithubVersionVerifier {
    private val buffer: ServerLocalSetBuffer = SimpleServerLocalSetBuffer()
    private val httpClient by lazy {
        HttpClient()
    }
    private val githubReleasesUrl = " https://api.github.com/repos/open-ani/ani/releases/tags"
    
    override suspend fun verify(clientVersion: String): Boolean {
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
