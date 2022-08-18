package me.him188.animationgarden.api

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
        fun create(): AnimationGardenClient = AnimationGardenClientImpl()
    }
}