package me.him188.animationgarden.api.model

interface User {
    val id: String

    val name: String
}

data class UserImpl(
    override val id: String,
    override val name: String
) : User

