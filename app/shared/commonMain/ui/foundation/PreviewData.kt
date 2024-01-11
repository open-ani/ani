package me.him188.ani.app.ui.foundation

import me.him188.ani.datasources.api.Subject
import me.him188.ani.datasources.api.SubjectImages

@Suppress("SpellCheckingInspection")
object PreviewData {
    const val MADE_IN_ABYSS_MOVIE_SUBJECT_ID = 230914
    const val MADE_IN_ABYSS_MOVIE_EPISODE_ID = 920023

    const val SOSOU_NO_FURILEN_SUBJECT_ID = 400602
    const val SOSOU_NO_FURILEN_EPISODE_ID = 1227087
    val SosouNoFurilen = Subject(
        id = 400602,
        originalName = "葬送的芙莉莲",
        chineseName = "葬送的芙莉莲",
        score = 8.0,
        rank = 100,
        sourceUrl = "https://bgm.tv/subject/400602",
        images = SubjectImages(
            "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA.",
            "https://lain.bgm.tv/pic/cover/l/13/c5/400602_ZI8Y9.jpg?_gl=1*isepc9*_ga*NDQzNzcwOTYyLjE3MDM4NjE5NzQ.*_ga_1109JLGMHN*MTcwNDQwNjE1MS4xMC4xLjE3MDQ0MDYxNzYuMC4wLjA."
        ),

        tags = listOf(),
        summary = "",
    )

}