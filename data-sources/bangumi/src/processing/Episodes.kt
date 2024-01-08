package me.him188.ani.datasources.bangumi.processing

import org.openapitools.client.models.EpisodeDetail

fun EpisodeDetail.renderEpisodeSp() = when (type) {
    0 -> { // 本篇
        (ep ?: sort).toInt().fixToString(2)
    } // "01", "12", "26", "120"

    1 -> { // SP
        "SP" + (sort.toInt().takeIf { it != 0 } ?: "")
    } // "SP", "SP1", "SP10"

    else -> {
        "PV" + (sort.toInt().takeIf { it != 0 } ?: "")
    } // "PV", "PV1", "PV10"
}


fun EpisodeDetail.nameCNOrName() =
    nameCn.takeIf { it.isNotBlank() } ?: name
