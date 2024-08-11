package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * [Bangumi API](https://bangumi.github.io/api) EpisodeType
 */
@JvmInline
@Serializable
value class EpisodeType(val value: Int) {
    companion object {
        val MainStory = EpisodeType(0)
        val SP = EpisodeType(1)

        val OP = EpisodeType(2)

        val ED = EpisodeType(3)
        
        val PV = EpisodeType(4)
        
        val MAD = EpisodeType(5)
        
        val OTHER = EpisodeType(6)
    }
}