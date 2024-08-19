package me.him188.ani.datasources.api

import me.him188.ani.utils.serialization.BigNum
import java.math.BigDecimal

fun EpisodeSort(int: BigDecimal, type: EpisodeType = EpisodeType.MainStory): EpisodeSort {
    if (int < BigDecimal.ZERO) return EpisodeSort.Unknown(int.toString())
    return EpisodeSort(BigNum(int), type)
}
