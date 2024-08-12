package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable

@Serializable
enum class EpisodeType(val code: Int) {
    MainStory(0),
    SP(1),
    OP(2),
    ED(3),
    PV(4),
    MAD(5),
    OTHER(6);

    companion object {
        fun codeOf(code: Int): EpisodeType {
            return when (code) {
                0 -> MainStory
                1 -> SP
                2 -> OP
                3 -> ED
                4 -> PV
                5 -> MAD
                6 -> OTHER
                else -> OTHER
            }
        }
    }
}

