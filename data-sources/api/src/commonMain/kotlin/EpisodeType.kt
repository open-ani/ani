package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable

@Serializable
enum class EpisodeType(val value: String) {
    MainStory("MainStory"),
    SP("SP"),
    OP("OP"),
    ED("ED"),
    PV("PV"),
    MAD("MAD"),
    OVA("OVA"),
    OAD("OAD"),
    Unknown("Unknown");
}

