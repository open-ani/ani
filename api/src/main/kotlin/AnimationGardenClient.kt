package me.him188.animationgarden.api

import me.him188.animationgarden.api.impl.AnimationGardenClientImpl
import me.him188.animationgarden.api.model.SearchFilter
import me.him188.animationgarden.api.model.SearchSession


public interface AnimationGardenClient {
//    public val user: User?
//
//    public fun login()
//    public fun logout()

    public fun startSearchSession(filter: SearchFilter): SearchSession

    public object Factory {
        public fun create(): AnimationGardenClient = AnimationGardenClientImpl()
    }
}