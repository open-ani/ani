package me.him188.animationgarden.api.impl

internal abstract class AbstractTest

internal fun Any.getResourceAsString(name: String) =
    this::class.java.classLoader.getResourceAsStream(name)!!.readAllBytes().decodeToString()
