package me.him188.ani.datasources.api

import me.him188.ani.datasources.api.EpisodeSort.Special
import java.math.BigDecimal

fun EpisodeSort(int: BigDecimal, type: EpisodeType = EpisodeType.MainStory): EpisodeSort {
    if (int < BigDecimal.ZERO) return Special(EpisodeType.Unknown, int.toFloat())
    return EpisodeSort(int, type)
}
