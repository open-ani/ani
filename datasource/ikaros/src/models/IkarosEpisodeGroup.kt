package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.Serializable

@Serializable
enum class IkarosEpisodeGroup {
    MAIN,

    /**
     * PV.
     */
    PROMOTION_VIDEO,

    /**
     * OP.
     */
    OPENING_SONG,

    /**
     * ED.
     */
    ENDING_SONG,

    /**
     * SP.
     */
    SPECIAL_PROMOTION,

    /**
     * ST.
     */
    SMALL_THEATER,

    /**
     * Live.
     */
    LIVE,

    /**
     * CM: Commercial Message.
     */
    COMMERCIAL_MESSAGE,

    /**
     * OST: Original Sound Track.
     */
    ORIGINAL_SOUND_TRACK,

    /**
     * OVA: Original Video Animation.
     */
    ORIGINAL_VIDEO_ANIMATION,

    /**
     * OAD: Original Animation Disc.
     */
    ORIGINAL_ANIMATION_DISC,
    MUSIC_DIST1,
    MUSIC_DIST2,
    MUSIC_DIST3,
    MUSIC_DIST4,
    MUSIC_DIST5,
    OTHER
}
