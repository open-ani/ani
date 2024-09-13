package me.him188.ani.app.ui.subject.components.comment

import me.him188.ani.app.Res
import me.him188.ani.app.bgm_01
import me.him188.ani.app.bgm_02
import me.him188.ani.app.bgm_03
import me.him188.ani.app.bgm_04
import me.him188.ani.app.bgm_05
import me.him188.ani.app.bgm_06
import me.him188.ani.app.bgm_07
import me.him188.ani.app.bgm_08
import me.him188.ani.app.bgm_09
import me.him188.ani.app.bgm_10
import me.him188.ani.app.bgm_11
import me.him188.ani.app.bgm_12
import me.him188.ani.app.bgm_13
import me.him188.ani.app.bgm_14
import me.him188.ani.app.bgm_15
import me.him188.ani.app.bgm_16
import me.him188.ani.app.bgm_17
import me.him188.ani.app.bgm_18
import me.him188.ani.app.bgm_19
import me.him188.ani.app.bgm_20
import me.him188.ani.app.bgm_21
import me.him188.ani.app.bgm_22
import me.him188.ani.app.bgm_23
import me.him188.ani.app.tv_01
import me.him188.ani.app.tv_02
import me.him188.ani.app.tv_03
import me.him188.ani.app.tv_04
import me.him188.ani.app.tv_05
import me.him188.ani.app.tv_06
import me.him188.ani.app.tv_07
import me.him188.ani.app.tv_08
import me.him188.ani.app.tv_09
import me.him188.ani.app.tv_10
import me.him188.ani.app.tv_100
import me.him188.ani.app.tv_101
import me.him188.ani.app.tv_102
import me.him188.ani.app.tv_11
import me.him188.ani.app.tv_12
import me.him188.ani.app.tv_13
import me.him188.ani.app.tv_14
import me.him188.ani.app.tv_15
import me.him188.ani.app.tv_16
import me.him188.ani.app.tv_17
import me.him188.ani.app.tv_18
import me.him188.ani.app.tv_19
import me.him188.ani.app.tv_20
import me.him188.ani.app.tv_21
import me.him188.ani.app.tv_22
import me.him188.ani.app.tv_23
import me.him188.ani.app.tv_24
import me.him188.ani.app.tv_25
import me.him188.ani.app.tv_26
import me.him188.ani.app.tv_27
import me.him188.ani.app.tv_28
import me.him188.ani.app.tv_29
import me.him188.ani.app.tv_30
import me.him188.ani.app.tv_31
import me.him188.ani.app.tv_32
import me.him188.ani.app.tv_33
import me.him188.ani.app.tv_34
import me.him188.ani.app.tv_35
import me.him188.ani.app.tv_36
import me.him188.ani.app.tv_37
import me.him188.ani.app.tv_38
import me.him188.ani.app.tv_39
import me.him188.ani.app.tv_40
import me.him188.ani.app.tv_41
import me.him188.ani.app.tv_42
import me.him188.ani.app.tv_43
import me.him188.ani.app.tv_44
import me.him188.ani.app.tv_45
import me.him188.ani.app.tv_46
import me.him188.ani.app.tv_47
import me.him188.ani.app.tv_48
import me.him188.ani.app.tv_49
import me.him188.ani.app.tv_50
import me.him188.ani.app.tv_51
import me.him188.ani.app.tv_52
import me.him188.ani.app.tv_53
import me.him188.ani.app.tv_54
import me.him188.ani.app.tv_55
import me.him188.ani.app.tv_56
import me.him188.ani.app.tv_57
import me.him188.ani.app.tv_58
import me.him188.ani.app.tv_59
import me.him188.ani.app.tv_60
import me.him188.ani.app.tv_61
import me.him188.ani.app.tv_62
import me.him188.ani.app.tv_63
import me.him188.ani.app.tv_64
import me.him188.ani.app.tv_65
import me.him188.ani.app.tv_66
import me.him188.ani.app.tv_67
import me.him188.ani.app.tv_68
import me.him188.ani.app.tv_69
import me.him188.ani.app.tv_70
import me.him188.ani.app.tv_71
import me.him188.ani.app.tv_72
import me.him188.ani.app.tv_73
import me.him188.ani.app.tv_74
import me.him188.ani.app.tv_75
import me.him188.ani.app.tv_76
import me.him188.ani.app.tv_77
import me.him188.ani.app.tv_78
import me.him188.ani.app.tv_79
import me.him188.ani.app.tv_80
import me.him188.ani.app.tv_81
import me.him188.ani.app.tv_82
import me.him188.ani.app.tv_83
import me.him188.ani.app.tv_84
import me.him188.ani.app.tv_85
import me.him188.ani.app.tv_86
import me.him188.ani.app.tv_87
import me.him188.ani.app.tv_88
import me.him188.ani.app.tv_89
import me.him188.ani.app.tv_90
import me.him188.ani.app.tv_91
import me.him188.ani.app.tv_92
import me.him188.ani.app.tv_93
import me.him188.ani.app.tv_94
import me.him188.ani.app.tv_95
import me.him188.ani.app.tv_96
import me.him188.ani.app.tv_97
import me.him188.ani.app.tv_98
import me.him188.ani.app.tv_99
import org.jetbrains.compose.resources.DrawableResource

object BangumiCommentSticker {
    infix operator fun get(id: Int): DrawableResource? {
        return STICKER_RES[id]
    }

    fun <R> map(block: (Pair<Int, DrawableResource>) -> R): List<R> {
        return STICKER_RES.entries.map { (k, v) -> block(k to v) }
    }

    private val STICKER_RES: Map<Int, DrawableResource> = mapOf(
        1 to Res.drawable.bgm_01,
        2 to Res.drawable.bgm_02,
        3 to Res.drawable.bgm_03,
        4 to Res.drawable.bgm_04,
        5 to Res.drawable.bgm_05,
        6 to Res.drawable.bgm_06,
        7 to Res.drawable.bgm_07,
        8 to Res.drawable.bgm_08,
        9 to Res.drawable.bgm_09,
        10 to Res.drawable.bgm_10,
        11 to Res.drawable.bgm_11,
        12 to Res.drawable.bgm_12,
        13 to Res.drawable.bgm_13,
        14 to Res.drawable.bgm_14,
        15 to Res.drawable.bgm_15,
        16 to Res.drawable.bgm_16,
        17 to Res.drawable.bgm_17,
        18 to Res.drawable.bgm_18,
        19 to Res.drawable.bgm_19,
        20 to Res.drawable.bgm_20,
        21 to Res.drawable.bgm_21,
        22 to Res.drawable.bgm_22,
        23 to Res.drawable.bgm_23,
        24 to Res.drawable.tv_01,
        25 to Res.drawable.tv_02,
        26 to Res.drawable.tv_03,
        27 to Res.drawable.tv_04,
        28 to Res.drawable.tv_05,
        29 to Res.drawable.tv_06,
        30 to Res.drawable.tv_07,
        31 to Res.drawable.tv_08,
        32 to Res.drawable.tv_09,
        33 to Res.drawable.tv_10,
        34 to Res.drawable.tv_11,
        35 to Res.drawable.tv_12,
        36 to Res.drawable.tv_13,
        37 to Res.drawable.tv_14,
        38 to Res.drawable.tv_15,
        39 to Res.drawable.tv_16,
        40 to Res.drawable.tv_17,
        41 to Res.drawable.tv_18,
        42 to Res.drawable.tv_19,
        43 to Res.drawable.tv_20,
        44 to Res.drawable.tv_21,
        45 to Res.drawable.tv_22,
        46 to Res.drawable.tv_23,
        47 to Res.drawable.tv_24,
        48 to Res.drawable.tv_25,
        49 to Res.drawable.tv_26,
        50 to Res.drawable.tv_27,
        51 to Res.drawable.tv_28,
        52 to Res.drawable.tv_29,
        53 to Res.drawable.tv_30,
        54 to Res.drawable.tv_31,
        55 to Res.drawable.tv_32,
        56 to Res.drawable.tv_33,
        57 to Res.drawable.tv_34,
        58 to Res.drawable.tv_35,
        59 to Res.drawable.tv_36,
        60 to Res.drawable.tv_37,
        61 to Res.drawable.tv_38,
        62 to Res.drawable.tv_39,
        63 to Res.drawable.tv_40,
        64 to Res.drawable.tv_41,
        65 to Res.drawable.tv_42,
        66 to Res.drawable.tv_43,
        67 to Res.drawable.tv_44,
        68 to Res.drawable.tv_45,
        69 to Res.drawable.tv_46,
        70 to Res.drawable.tv_47,
        71 to Res.drawable.tv_48,
        72 to Res.drawable.tv_49,
        73 to Res.drawable.tv_50,
        74 to Res.drawable.tv_51,
        75 to Res.drawable.tv_52,
        76 to Res.drawable.tv_53,
        77 to Res.drawable.tv_54,
        78 to Res.drawable.tv_55,
        79 to Res.drawable.tv_56,
        80 to Res.drawable.tv_57,
        81 to Res.drawable.tv_58,
        82 to Res.drawable.tv_59,
        83 to Res.drawable.tv_60,
        84 to Res.drawable.tv_61,
        85 to Res.drawable.tv_62,
        86 to Res.drawable.tv_63,
        87 to Res.drawable.tv_64,
        88 to Res.drawable.tv_65,
        89 to Res.drawable.tv_66,
        90 to Res.drawable.tv_67,
        91 to Res.drawable.tv_68,
        92 to Res.drawable.tv_69,
        93 to Res.drawable.tv_70,
        94 to Res.drawable.tv_71,
        95 to Res.drawable.tv_72,
        96 to Res.drawable.tv_73,
        97 to Res.drawable.tv_74,
        98 to Res.drawable.tv_75,
        99 to Res.drawable.tv_76,
        100 to Res.drawable.tv_77,
        101 to Res.drawable.tv_78,
        102 to Res.drawable.tv_79,
        103 to Res.drawable.tv_80,
        104 to Res.drawable.tv_81,
        105 to Res.drawable.tv_82,
        106 to Res.drawable.tv_83,
        107 to Res.drawable.tv_84,
        108 to Res.drawable.tv_85,
        109 to Res.drawable.tv_86,
        110 to Res.drawable.tv_87,
        111 to Res.drawable.tv_88,
        112 to Res.drawable.tv_89,
        113 to Res.drawable.tv_90,
        114 to Res.drawable.tv_91,
        115 to Res.drawable.tv_92,
        116 to Res.drawable.tv_93,
        117 to Res.drawable.tv_94,
        118 to Res.drawable.tv_95,
        119 to Res.drawable.tv_96,
        120 to Res.drawable.tv_97,
        121 to Res.drawable.tv_98,
        122 to Res.drawable.tv_99,
        123 to Res.drawable.tv_100,
        124 to Res.drawable.tv_101,
        125 to Res.drawable.tv_102,
    )
}