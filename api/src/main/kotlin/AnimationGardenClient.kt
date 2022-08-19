package me.him188.animationgarden.api

import io.ktor.client.engine.*
import me.him188.animationgarden.api.impl.AnimationGardenClientImpl
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession


interface AnimationGardenClient {
//    public val user: User?
//
//    public fun login()
//    public fun logout()

    fun startSearchSession(filter: SearchQuery): SearchSession

    object Factory {
        fun create(engineConfig: HttpClientEngineConfig.() -> Unit): AnimationGardenClient =
            AnimationGardenClientImpl(engineConfig)
    }
}