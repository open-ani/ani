package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.Serializable

@Serializable
enum class IkarosEpisodeGroup {
    MAIN,
    PROMOTION_VIDEO,
    OPENING_SONG,
    ENDING_SONG,
    SPECIAL_PROMOTION,
    SMALL_THEATER,
    LIVE,
    COMMERCIAL_MESSAGE,
    MUSIC_DIST1,
    MUSIC_DIST2,
    MUSIC_DIST3,
    MUSIC_DIST4,
    MUSIC_DIST5,
    OTHER;
}
