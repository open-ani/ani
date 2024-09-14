/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

// @formatter:off
@file:Suppress(
  "FunctionName",
  "ClassName",
  "RedundantVisibilityModifier",
  "PackageDirectoryMismatch",
  "NonAsciiCharacters",
  "SpellCheckingInspection",
)

import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.title.PatternBasedTitleParserTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 原名: `剧场版 紫罗兰永恒花园`
 * 数据源: `dmhy`
 *
 * 由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
 * 如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
 * 如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
 */
public class `PatternTitleParserTest剧场版 紫罗兰永恒花园` : PatternBasedTitleParserTestSuite() {
  @Test
  public
      fun `651397_VCB_Studio_Gekijouban_Violet_Evergarden_10_bit_2160p_1080p_HEVC_BDRip_MOVIE`() {
    val r =
        parse("[VCB-Studio] 剧场版 紫罗兰永恒花园 / Gekijouban Violet Evergarden 10-bit 2160p/1080p HEVC BDRip [MOVIE]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615844_Violet_Evergarden_the_Movie_4K_SDR`() {
    val r = parse("[云光字幕组]剧场版 紫罗兰永恒花园 Violet Evergarden the Movie [简体双语][4K SDR]招募时轴翻译")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `588811_Violet_Evergarden_The_Movie_BDrip_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【剧场版紫罗兰永恒花园 Violet Evergarden The Movie】【BDrip】【GB_MP4】【1280X720】")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `588810_Violet_Evergarden_The_Movie_BDrip_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【剧场版紫罗兰永恒花园 Violet Evergarden The Movie】【BDrip】【GB_MP4】【1920X1080】")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `587366_Violet_Evergarden_Eien_to_Jidou_Shuki_Ningyou_BDrip_GB_MP4_1920X1080`() {
    val r =
        parse("【幻樱字幕组】【剧场版】【紫罗兰永恒花园外传：永远与自动手记人偶 Violet Evergarden Eien to Jidou Shuki Ningyou】【BDrip】【GB_MP4】【1920X1080】")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `587364_Violet_Evergarden_Eien_to_Jidou_Shuki_Ningyou_BDrip_GB_MP4_1280X720`() {
    val r =
        parse("【幻樱字幕组】【剧场版】【紫罗兰永恒花园外传：永远与自动手记人偶 Violet Evergarden Eien to Jidou Shuki Ningyou】【BDrip】【GB_MP4】【1280X720】")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `585468_subbers_project_Violet_Evergarden_the_Movie_BDRip_2K_SDR_rev2`() {
    val r =
        parse("[.subbers project] 剧场版 紫罗兰永恒花园 / 劇場版 ヴァイオレット・エヴァーガーデン / Violet Evergarden the Movie [BDRip][2K SDR][简繁日字幕内封][rev2]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `583084_subbers_project_Violet_Evergarden_the_Movie_BDRip_4K_HDR_2K_SDR_BD`() {
    val r =
        parse("[.subbers project] 剧场版 紫罗兰永恒花园 / 劇場版 ヴァイオレット・エヴァーガーデン / Violet Evergarden the Movie [BDRip][4K HDR+2K SDR][简繁日字幕内封](附BD及剧场特典扫图、相关音乐、小说翻译等)")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `582838_Violet_Evergarden_the_Movie_BDRip_Full_HD_HEVC`() {
    val r =
        parse("【千夏字幕组】【剧场版 紫罗兰永恒花园/薇尔莉特·伊芙嘉登_Violet Evergarden the Movie】[剧场版][BDRip_Full HD_HEVC][简繁内封]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `582836_Violet_Evergarden_the_Movie_BDRip_Full_HD_AVC`() {
    val r =
        parse("【千夏字幕组】【剧场版 紫罗兰永恒花园/薇尔莉特·伊芙嘉登_Violet Evergarden the Movie】[剧场版][BDRip_Full HD_AVC][简体]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `582144_DBD_Raws_Violet_Evergarden_The_Movie_1080P_BDRip_HEVC_10bit_FLAC_MKV`() {
    val r =
        parse("[DBD-Raws][剧场版 紫罗兰永恒花园/Violet Evergarden The Movie/劇場版 ヴァイオレット・エヴァーガーデ][1080P][BDRip][HEVC-10bit][FLAC][MKV]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `582130_DBD_Raws_4K_HDR_Violet_Evergarden_The_Movie_2160P_BDRip_HEVC_10bit_FLAC_MKV`() {
    val r =
        parse("[DBD-Raws][4K_HDR][剧场版 紫罗兰永恒花园/Violet Evergarden The Movie/劇場版 ヴァイオレット・エヴァーガーデ][2160P][BDRip][HEVC-10bit][FLAC][MKV]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `582129_Violet_Evergarden_the_Movie_1080P_GB_BDrip`() {
    val r = parse("【幻之字幕组】剧场版 紫罗兰永恒花园[Violet Evergarden the Movie] [1080P][GB][BDrip]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `581912_subbers_project_Violet_Evergarden_the_Movie_BDRip_FullHD`() {
    val r =
        parse("[.subbers project] 剧场版 紫罗兰永恒花园 / 劇場版 ヴァイオレット・エヴァーガーデン / Violet Evergarden the Movie [BDRip][FullHD][简体中文字幕内嵌]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `571322_WEBRip_MOVIE_Violet_Evergarden_1920x804_x_264_AAC_MP4`() {
    val r =
        parse("[森之屋动画组][WEBRip][剧场版 紫罗兰永恒花园 / 劇場版ヴァイオレット・エヴァーガーデン / MOVIE Violet Evergarden][1920x804][x.264 AAC MP4][俄语音轨][内嵌简中]")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `571317_WEBRip_MOVIE_Violet_Evergarden_1920x804_x_264_AAC_MP4`() {
    val r =
        parse("[森之屋动画组][WEBRip][剧场版 紫罗兰永恒花园 / 劇場版ヴァイオレット・エヴァーガーデン / MOVIE Violet Evergarden][1920x804][x.264 AAC MP4][日语剧场录音音轨（先行版）][内嵌简中]")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `549455_MCE_Violet_Evergarden_2020_Trial_Version_1080P_x264_AAC`() {
    val r =
        parse("【MCE汉化组】[剧场版 紫罗兰永恒花园 冒头影像 / Violet Evergarden 2020][Trial Version][简体][1080P][x264 AAC]")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `537654_Violet_Evergarden_Side_Story__Eternity_and_Auto_Memory_Doll__BDRip_1080p_HEVC`() {
    val r =
        parse("【千夏字幕组】【紫罗兰永恒花园·外传 —永远与自动手记人偶—_Violet Evergarden Side Story -Eternity and Auto Memory Doll-】[剧场版][BDRip_1080p_HEVC][简繁外挂]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL_DISCOVER, r.subtitleKind)
  }

  @Test
  public
      fun `537653_Violet_Evergarden_Side_Story__Eternity_and_Auto_Memory_Doll__BDRip_1080p_AVC`() {
    val r =
        parse("【千夏字幕组】【紫罗兰永恒花园·外传 —永远与自动手记人偶—_Violet Evergarden Side Story -Eternity and Auto Memory Doll-】[剧场版][BDRip_1080p_AVC][简体]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `537305_Moozzi2_Violet_Evergarden_Eien_to_Jidou_Shuki_Ningyou_BD_1920x804_x264_10Bit_4Audio`() {
    val r =
        parse("[Moozzi2] 劇場版 紫羅蘭永恒花園外傳:永遠與自動手記人偶 Violet Evergarden Eien to Jidou Shuki Ningyou (BD 1920x804 x264-10Bit 4Audio)")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `537233_Violet_Evergarden_Gaiden_Eien_to_Jidou_Shuki_Ningyou_1080P_CHT_JPN_BDrip_AVC_AAC_YUV420P8`() {
    val r =
        parse("【幻之字幕组】劇場版 紫羅蘭永恒花園外傳：永遠與自動手記人偶 Violet Evergarden Gaiden: Eien to Jidou Shuki Ningyou [1080P][雙語][CHT&JPN][BDrip][AVC AAC YUV420P8]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `537232_Violet_Evergarden_Gaiden_Eien_to_Jidou_Shuki_Ningyou_1080P_CHS_JPN_BDrip_AVC_AAC_YUV420P8`() {
    val r =
        parse("【幻之字幕组】剧场版 紫罗兰永恒花园外传：永远与自动手记人偶 Violet Evergarden Gaiden: Eien to Jidou Shuki Ningyou [1080P][双语][CHS&JPN][BDrip][AVC AAC YUV420P8]")
    assertEquals("S?", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }
}

// @formatter:on
