package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable

@Serializable
enum class EpisodeType(val code: Int, val value: String) {
    MainStory(0, "MainStory"),
    SP(1, "SP"),
    OP(2, "OP"),
    ED(3, "ED"),
    PV(4, "PV"),
    MAD(5, "MAD"),
    OTHER(6, "OTHER");
}

