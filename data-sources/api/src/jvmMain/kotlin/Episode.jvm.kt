package me.him188.ani.datasources.api

import me.him188.ani.datasources.api.EpisodeSort.Special
import java.math.BigDecimal

fun EpisodeSort(int: BigDecimal): EpisodeSort {
    if (int < BigDecimal.ZERO) return Special(int.toString())
    return EpisodeSort(int.toString())
}
