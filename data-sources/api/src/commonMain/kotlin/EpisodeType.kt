package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable

@Serializable
enum class EpisodeType(val value: Int) {
    MainStory(0),
    SP(1),
    OP(2),
    ED(3),
    PV(4),
    MAD(5),
    OTHER(6)
}