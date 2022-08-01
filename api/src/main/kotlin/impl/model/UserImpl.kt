package me.him188.animationgarden.api.impl.model

import me.him188.animationgarden.api.model.User

internal data class UserImpl(
    override val id: String,
    override val name: String
) : User

