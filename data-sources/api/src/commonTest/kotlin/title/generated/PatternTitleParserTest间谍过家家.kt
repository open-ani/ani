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
 * 原名: `间谍过家家`
 * 数据源: `dmhy`
 *
 * 由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
 * 如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
 * 如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
 */
public class PatternTitleParserTest间谍过家家 : PatternBasedTitleParserTestSuite() {
  @Test
  public fun `668367_DBD_Raws_Spy_x_Family_01_25TV_1080P_BDRip_HEVC_10bit_FLAC_MKV`() {
    val r =
        parse("[DBD-Raws][间谍过家家/间谍家家酒/Spy x Family][01-25TV全集+特典映像][1080P][BDRip][HEVC-10bit][简繁日双语外挂][FLAC][MKV]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `663342_Spy_x_Family_S2_01_12_1080P_MP4`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][间谍过家家 第二季][Spy x Family S2][01-12][1080P][MP4][简中]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662721_Spy_x_Family_S2_26_37_WebRip_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [26-37] [WebRip] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("26..37", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662719_Season_02_Spy_x_Family_Season_2_01_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 02 / Spy x Family Season 2 [01-12][1080p][简体内嵌]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662718_Season_2_Spy_x_Family_Season_2_01_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [01-12][1080p][简繁内封]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662342_jibaketa_SPY_FAMILY_Season_2___12_END_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 12 END [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662341_jibaketa_SPY_FAMILY_Season_2___11_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 11 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662340_jibaketa_SPY_FAMILY_Season_2___10_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 10 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660443_jibaketa_SPY_FAMILY_Season_2___09_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 09 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660442_jibaketa_SPY_FAMILY_Season_2___08_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 08 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660441_jibaketa_SPY_FAMILY_Season_2___07_WEB_1920x1080_AVC_AACx2_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 07 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRT MUSE CHT)")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659917_10_SPYxFAMILY_37_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][37][1080p][繁日雙語][招募翻譯]")
    assertEquals("37..37", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659912_10_SPYxFAMILY_37_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][37][1080p][简日双语][招募翻译]")
    assertEquals("37..37", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659911_10_SPYxFAMILY_36_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][36][1080p][繁日雙語][招募翻譯]")
    assertEquals("36..36", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659910_10_SPYxFAMILY_36_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][36][1080p][简日双语][招募翻译]")
    assertEquals("36..36", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659841_10_Spy_x_Family_S2_12_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][12][1080P][MP4][GB][简中]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659839_10_Spy_x_Family_S2_11_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][11][1080P][MP4][GB][简中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659665_Spy_x_Family_S2_37_END_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [37] [END] [1080p] [繁日内嵌] [2023年10月番]")
    assertEquals("37..37", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659663_Spy_x_Family_S2_37_END_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [37] [END] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("37..37", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659552_Spy_x_Family_Season_2_1080p`() {
    val r = parse("[云光字幕组]间谍过家家第二季Spy x Family Season 2[合集][简体双语][1080p]招募翻译")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659551_Spy_x_Family_37_END_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [37][END][简体双语][1080p]招募翻译")
    assertEquals("37..37", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659338_Season_2_Spy_x_Family_Season_2_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [12][1080p][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659336_Season_2_Spy_x_Family_Season_2_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [12][1080p][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659300_Spy_x_Family_36_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [36][简体双语][1080p]招募翻译")
    assertEquals("36..36", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659204_10_SPYxFAMILY_35_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][35][1080p][繁日雙語][招募翻譯]")
    assertEquals("35..35", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659203_10_SPYxFAMILY_35_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][35][1080p][简日双语][招募翻译]")
    assertEquals("35..35", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659161_Spy_x_Family_S2_36_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [36] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("36..36", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659143_jibaketa_SPY_FAMILY_Season_2___06_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 06 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659142_jibaketa_SPY_FAMILY_Season_2___05_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 05 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659141_jibaketa_SPY_FAMILY_Season_2___04_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 04 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659005_Spy_x_Family_35_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [35][简体双语][1080p]招募翻译")
    assertEquals("35..35", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658897_10_Spy_x_Family_S2_10_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][10][1080P][MP4][GB][简中]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658895_10_Spy_x_Family_S2_09_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][09][1080P][MP4][GB][简中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658893_10_Spy_x_Family_S2_08_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][08][1080P][MP4][GB][简中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658891_10_Spy_x_Family_S2_07_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][07][1080P][MP4][GB][简中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658814_Season_2_Spy_x_Family_Season_2_11_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [11][1080p][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `658812_Season_2_Spy_x_Family_Season_2_11_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [11][1080p][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `658742_10_SPYxFAMILY_33_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][33][1080p][繁日雙語][招募翻譯]")
    assertEquals("33..33", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658611_Spy_x_Family_S2_35_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [35] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("35..35", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `658609_Spy_x_Family_S2_34_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [34] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("34..34", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `658570_Spy_x_Family_34_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [34][简体双语][1080p]招募翻译")
    assertEquals("34..34", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658323_10_SPYxFAMILY_34_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][34][1080p][繁日雙語][招募翻譯]")
    assertEquals("34..34", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `658234_Season_2_Spy_x_Family_Season_2_10_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [10][1080p][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `658233_Season_2_Spy_x_Family_Season_2_10_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [10][1080p][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `658173_10_SPYxFAMILY_34_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][34][1080p][简日双语][招募翻译]")
    assertEquals("34..34", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657725_Spy_x_Family_33_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [33][简体双语][1080p]招募翻译")
    assertEquals("33..33", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657619_10_SPYxFAMILY_33_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][33][1080p][简日双语][招募翻译]")
    assertEquals("33..33", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657495_Season_2_Spy_x_Family_Season_2_09_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [09][1080p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `657493_Season_2_Spy_x_Family_Season_2_09_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [09][1080p][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `657461_10_SPYxFAMILY_32_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][32][1080p][繁日雙語][招募翻譯]")
    assertEquals("32..32", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657441_10_SPYxFAMILY_32_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][32][1080p][简日双语][招募翻译]")
    assertEquals("32..32", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657398_Spy_x_Family_S2_33_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [33] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("33..33", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `657394_Spy_x_Family_S2_32_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [32] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("32..32", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `657336_jibaketa_SPY_FAMILY_Season_2___03_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 03 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `657335_jibaketa_SPY_FAMILY_Season_2___02_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 02 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `657333_jibaketa_SPY_FAMILY_Season_2___01_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家Season 2 - 01 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `657201_Season_2_Spy_x_Family_Season_2_08_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [08][1080p][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `657199_Season_2_Spy_x_Family_Season_2_08_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [08][1080p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `657099_Spy_x_Family_32_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [32][简体双语][1080p]招募翻译")
    assertEquals("32..32", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657049_10_SPYxFAMILY_31_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][31][1080p][繁日雙語][招募翻譯]")
    assertEquals("31..31", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657048_10_SPYxFAMILY_31_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][31][1080p][简日双语][招募翻译]")
    assertEquals("31..31", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `657047_10_SPYxFAMILY_30_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][30][1080p][繁日雙語][招募翻譯]")
    assertEquals("30..30", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656783_Season_2_Spy_x_Family_Season_2_07_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [07][1080p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `656782_Season_2_Spy_x_Family_Season_2_07_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [07][1080p][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `656776_10_SPYxFAMILY_30_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][30][1080p][简日双语][招募翻译]")
    assertEquals("30..30", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656710_Spy_x_Family_31_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [31][简体双语][1080p]招募翻译")
    assertEquals("31..31", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656617_10_Spy_x_Family_S2_06_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][06][1080P][MP4][GB][简中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656615_10_Spy_x_Family_S2_05_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][05][1080P][MP4][GB][简中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656612_10_Spy_x_Family_S2_04_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][04][1080P][MP4][GB][简中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656527_Spy_x_Family_S2_31_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [31] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("31..31", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `656196_Season_2_Spy_x_Family_Season_2_06_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [06][1080p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `656195_Season_2_Spy_x_Family_Season_2_06_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [06][1080p][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `656125_10_SPYxFAMILY_29_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][29][1080p][繁日雙語][招募翻譯]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656017_Spy_x_Family_30_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [30][简体双语][1080p]招募翻译")
    assertEquals("30..30", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656015_Spy_x_Family_Season_3_01_AVC_8bit_1080P_CHS_JPN`() {
    val r = parse("[织梦字幕组] 间谍过家家 第二季 Spy x Family Season 2 [01] [AVC-8bit 1080P] [CHT＆JPN]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656014_Spy_x_Family_Season_2_01_AVC_8bit_1080P_CHS_JPN`() {
    val r = parse("[织梦字幕组] 间谍过家家 第二季 Spy x Family Season 2 [01] [AVC-8bit 1080P] [CHS＆JPN]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656013_Spy_x_Family_Season_2_01_AVC_8bit_1080P_CHT_JPN`() {
    val r = parse("[织梦字幕组] 间谍过家家 第二季 Spy x Family Season 2 [01] [AVC-8bit 720P] [CHT＆JPN]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `656012_Spy_x_Family_Season_2_01_AVC_8bit_720P_CHS_JPN`() {
    val r = parse("[织梦字幕组] 间谍过家家 第二季 Spy x Family Season 2 [01] [AVC-8bit 720P] [CHS＆JPN]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655931_10_SPYxFAMILY_29_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][29][1080p][简日双语][招募翻译]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655899_Spy_x_Family_S2_30_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [30] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("30..30", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `655770_Season_2_Spy_x_Family_Season_2_05_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [05][1080p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `655769_Season_2_Spy_x_Family_Season_2_05_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [05][1080p][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `655719_LoliHouse_Spy_x_Family___29_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 29 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `655705_10_SPY_FAMILY_29_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][29][1080P][简体][MP4]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655704_10_SPY_FAMILY_29_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][29][1080P][繁體][MP4]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655703_10_SPY_FAMILY_29_720P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][29][720P][简体][MP4]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655702_10_SPY_FAMILY_29_720P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][29][720P][繁體][MP4]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655505_Spy_x_Family_29_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [29][简体双语][1080p]招募翻译")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655390_Spy_x_Family_S2_29_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [29] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `655247_Season_2_Spy_x_Family_Season_2_04_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [04][1080p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `655245_Season_2_Spy_x_Family_Season_2_04_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [04][1080p][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `655206_Spy_x_Family_28_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [28][简体双语][1080p]招募翻译")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `655078_Skymoon_Raws_2_SPY_FAMILY_Season_2___29_ViuTV_WEB_DL_1080p_AVC_AAC_MP4_ASS`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 29 [ViuTV][WEB-DL][1080p][AVC AAC][繁體外掛][MP4+ASS](正式版本)")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public
      fun `655075_Skymoon_Raws_2_SPY_FAMILY_Season_2___29_ViuTV_WEB_RIP_1080p_AVC_AAC_CHT_SRT_MKV`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 29 [ViuTV][WEB-RIP][1080p][AVC AAC][CHT][SRT][MKV](先行版本)")
    assertEquals("29..29", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654806_10_SPYxFAMILY_28_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][28][1080p][繁日雙語][招募翻譯]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654778_Spy_x_Family_S2_28_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [28] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `654761_10_SPYxFAMILY_28_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][28][1080p][简日双语][招募翻译]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654749_10_Spy_x_Family_S2_03_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][03][1080P][MP4][GB][简中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654747_10_Spy_x_Family_S2_02_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家 第二季][Spy x Family S2][02][1080P][MP4][GB][简中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654616_LoliHouse_Spy_x_Family___28_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 28 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `654610_10_SPY_FAMILY_28_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][28][1080P][简体][MP4]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654609_10_SPY_FAMILY_28_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][28][1080P][繁體][MP4]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654608_10_SPY_FAMILY_28_720P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][28][720P][简体][MP4]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654607_10_SPY_FAMILY_28_720P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][28][720P][繁體][MP4]")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654522_Season_2_Spy_x_Family_Season_2_03_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [03][1080p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `654521_Season_2_Spy_x_Family_Season_2_03_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [03][1080p][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `654423_Spy_x_Family_27_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [27][简体双语][1080p]招募翻译")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654293_Skymoon_Raws_2_SPY_FAMILY_Season_2___28_ViuTV_WEB_DL_1080p_AVC_AAC_MP4_ASS`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 28 [ViuTV][WEB-DL][1080p][AVC AAC][繁體外掛][MP4+ASS](正式版本)")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public
      fun `654286_Skymoon_Raws_2_SPY_FAMILY_Season_2___28_ViuTV_WEB_RIP_1080p_AVC_AAC_CHT_SRT_MKV`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 28 [ViuTV][WEB-RIP][1080p][AVC AAC][CHT][SRT][MKV](先行版本)")
    assertEquals("28..28", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `654160_Spy_x_Family_S2_27_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [27] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `653998_LoliHouse_Spy_x_Family___27_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 27 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `653982_10_SPYxFAMILY_27_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][27][1080p][繁日雙語][招募翻譯]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653947_Spy_x_Family_S2_26_1080p_2023_10`() {
    val r = parse("[猎户压制部] 间谍过家家 第二季 / Spy x Family S2 [26] [1080p] [简日内嵌] [2023年10月番]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `653933_10_SPY_FAMILY_27_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][27][1080P][简体][MP4]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653932_10_SPY_FAMILY_27_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][27][1080P][繁體][MP4]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653931_10_SPY_FAMILY_27_720P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][27][720P][简体][MP4]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653930_10_SPY_FAMILY_27_720P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][27][720P][繁體][MP4]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653891_10_SPYxFAMILY_27_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][27][1080p][简日双语][招募翻译]")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653882_Season_2_Spy_x_Family_Season_2_02_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [02][1080p][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `653881_Season_2_Spy_x_Family_Season_2_02_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [02][1080p][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `653774_10_SPYxFAMILY_26_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家 / 間諜家家酒 / SPYxFAMILY][26][1080p][繁日雙語][招募翻譯]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653773_10_SPYxFAMILY_26_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家 / 间谍家家酒 / SPYxFAMILY][26][1080p][简日双语][招募翻译]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653717_Spy_x_Family_26_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 第二季 Spy x Family [26][简体双语][1080p]招募翻译")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653692_Skymoon_Raws_2_SPY_FAMILY_Season_2___27_ViuTV_WEB_DL_1080p_AVC_AAC_MP4_ASS`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 27 [ViuTV][WEB-DL][1080p][AVC AAC][繁體外掛][MP4+ASS](正式版本)")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public
      fun `653691_Skymoon_Raws_2_SPY_FAMILY_Season_2___27_ViuTV_WEB_RIP_1080p_AVC_AAC_CHT_SRT_MKV`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 27 [ViuTV][WEB-RIP][1080p][AVC AAC][CHT][SRT][MKV](先行版本)")
    assertEquals("27..27", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653480_LoliHouse_Spy_x_Family___26_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 26 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `653380_10_Spy_x_Family_S2_01_1080P_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family S2][01][1080P][MP4][GB][简中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653362_10_SPY_FAMILY_26_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][26][1080P][简体][MP4]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653361_10_SPY_FAMILY_26_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][26][1080P][繁體][MP4]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653360_10_SPY_FAMILY_26_720P_MP4`() {
    val r = parse("【动漫国字幕组】★10月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][26][720P][简体][MP4]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653359_10_SPY_FAMILY_26_720P_MP4`() {
    val r = parse("【動漫國字幕組】★10月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][26][720P][繁體][MP4]")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `653278_Season_2_Spy_x_Family_Season_2_01_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [01][1080p][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `653277_Season_2_Spy_x_Family_Season_2_01_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 Season 2 / Spy x Family Season 2 [01][1080p][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `652999_Skymoon_Raws_2_SPY_FAMILY_Season_2___26_ViuTV_WEB_DL_1080p_AVC_AAC_MP4_ASS`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 26 [ViuTV][WEB-DL][1080p][AVC AAC][繁體外掛][MP4+ASS](正式版本)")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public
      fun `652998_Skymoon_Raws_2_SPY_FAMILY_Season_2___26_ViuTV_WEB_RIP_1080p_AVC_AAC_CHT_SRT_MKV`() {
    val r =
        parse("[Skymoon-Raws] 間諜過家家 第2期 / SPY×FAMILY Season 2 - 26 [ViuTV][WEB-RIP][1080p][AVC AAC][CHT][SRT][MKV](先行版本)")
    assertEquals("26..26", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `645091_SPY_FAMILY_BD_RIP_13_25_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][BD-RIP][下卷][13-25集][1080P][AVC][繁日雙語]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `645090_SPY_FAMILY_BD_RIP_13_15_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][BD-RIP][下卷][13-15集][720P][AVC][繁日雙語]")
    assertEquals("13..15", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `645089_SPY_FAMILY_BD_RIP_13_25_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][BD-RIP][下卷][13-25集][1080P][AVC][简日双语]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `645088_SPY_FAMILY_BD_RIP_13_25_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][BD-RIP][下卷][13-25集][720P][AVC][简日双语]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `642385_jibaketa_SPY_FAMILY___S1_BD_1920x1080_x264_AACx2_PGS_SRT_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - S1 [粵日雙語+內封繁體中文字幕] (BD 1920x1080 x264 AACx2 PGS+SRT MUSE CHT)")
    assertEquals("S1", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `642236_7_ACG_SPY_FAMILY_S01_01_25_BDrip_1080p_x265_FLAC_2_0_repack`() {
    val r =
        parse("[桜都字幕组&7³ACG] 间谍过家家/间谍家家酒/スパイファミリー/SPY×FAMILY S01 | 01-25 [简繁字幕] BDrip 1080p x265 FLAC 2.0 (repack)")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `642000_DDD_SPY_FAMILY_BDrip_1080p_HEVC_FLAC_10bits`() {
    val r = parse("[DDD] SPY×FAMILY 间谍过家家 [BDrip 1080p HEVC FLAC][10bits]")
    assertEquals("SPY×FAMILY..SPY×FAMILY", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `630310_jibaketa_SPY_FAMILY___25_END_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 25 END [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `630307_jibaketa_SPY_FAMILY___24_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 24 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `630259_10_SPY_FAMILY_23_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【23】GB MP4_720P")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `630256_10_SPY_FAMILY_23_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【23】GB MP4_1080P")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628569_jibaketa_SPY_FAMILY___23_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 23 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `628568_jibaketa_SPY_FAMILY___22_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 22 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `628511_LoliHouse_Spy_x_Family_01_25_WebRip_1080p_HEVC_10bit_AAC_Fin`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 [01-25 精校合集][WebRip 1080p HEVC-10bit AAC][简繁外挂字幕][Fin]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `628311_04_SPY_FAMILY_01_25_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][01-25(全集)][1080P][简体][MP4]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628310_04_SPY_FAMILY_01_25_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][01-25(全集)][1080P][繁體][MP4]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628309_04_SPY_FAMILY_01_25_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][01-25(全集)][720P][简体][MP4]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628308_04_SPY_FAMILY_01_25_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][01-25(全集)][720P][繁體][MP4]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628280_04_SPY_FAMILY_22_25_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][22-25][1080P][简体][MP4](第一季完)")
    assertEquals("22..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628279_04_SPY_FAMILY_22_25_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][22-25][1080P][繁體][MP4](第一季完)")
    assertEquals("22..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628278_04_SPY_FAMILY_22_25_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][22-25][720P][简体][MP4](第一季完)")
    assertEquals("22..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `628277_04_SPY_FAMILY_22_25_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][22-25][720P][繁體][MP4](第一季完)")
    assertEquals("22..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `627230_Spy_x_Family_25_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][25][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `627017_10_SPY_FAMILY_22_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【22】GB MP4_720P")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `627016_10_SPY_FAMILY_22_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【22】GB MP4_1080P")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `627000_Spy_x_Family_1_25_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [1-25 Fin][1080p][简体内嵌]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `626999_Spy_x_Family_1_25_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [1-25 Fin][1080p][简繁内封]")
    assertEquals("01..25", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `626998_10_SPYxFAMILY_13_25_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][13-25][1080p][繁日雙語][招募翻譯]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626997_10_SPYxFAMILY_13_25_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][13-25][1080p][简日双语][招募翻译]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626996_10_SPYxFAMILY_13_25_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][13-25][720p][繁日雙語][招募翻譯]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626993_10_SPYxFAMILY_13_25_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][13-25][720p][简日双语][招募翻译]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626915_404_24_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][24][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626910_jibaketa_SPY_FAMILY___21_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 21 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `626908_jibaketa_SPY_FAMILY___20_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 20 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `626907_jibaketa_SPY_FAMILY___19_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 19 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `626664_Spy_x_Family_13_25_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [13-25 Fin][1080p][简体内嵌]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `626660_Spy_x_Family_13_25_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [13-25 Fin][1080p][简繁内封]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `626319_10_SPYxFAMILY_25_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][25][1080p][繁日雙語][招募翻譯]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626318_10_SPYxFAMILY_25_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][25][1080p][简日双语][招募翻译]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626317_10_SPYxFAMILY_25_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][25][720p][繁日雙語][招募翻譯]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626316_10_SPYxFAMILY_25_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][25][720p][简日双语][招募翻译]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626136_SPY_FAMILY_25_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [25话合集][简体双语][1080p]招募翻译")
    assertEquals("SPY×FAMILY..SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626134_SPY_FAMILY_25_END_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [25][END][简体双语][1080p]招募时轴后期")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626055_10_SPYxFAMILY_24_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][24][1080p][繁日雙語][招募翻譯]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626054_10_SPYxFAMILY_24_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][24][1080p][简日双语][招募翻译]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626053_10_SPYxFAMILY_24_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][24][720p][繁日雙語][招募翻譯]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626052_10_SPYxFAMILY_24_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][24][720p][简日双语][招募翻译]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `626027_Spy_x_Family_24_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][24][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625972_10_SPY_FAMILY_21_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【21】GB MP4_720P")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625971_10_SPY_FAMILY_21_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【21】GB MP4_1080P")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625945_Studio_10_SPY_FAMILY_Part_2_25_END_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][25][END][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625944_Studio_10_SPY_FAMILY_Part_2_25_END_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][25][END][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625943_Studio_10_SPY_FAMILY_Part_2_25_END_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][25][END][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625942_Studio_10_SPY_FAMILY_Part_2_25_END_1080p_AVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][25][END][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625936_Spy_x_Family_25_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [25][1080p][简体内嵌]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `625935_Spy_x_Family_25_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [25][1080p][简繁内封]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `625863_10_Spy_x_Family_13_25_1080p_MP4`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][13-25][1080p][MP4][简中]")
    assertEquals("13..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625807_Spy_x_Family_25_END_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [25] [END] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `625805_10_Spy_x_Family_25_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][25][1080p][MP4][GB][简中]")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625727_SPY_FAMILY_24_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [24][简体双语][1080p]招募时轴后期")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625726_SPY_FAMILY_23_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [23][简体双语][1080p]招募时轴后期")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625644_SPY_FAMILY_25___END_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][25集 - END][1080P][AVC][繁日雙語]")
    assertEquals("間諜過家家 SPY×FAMILY..間諜過家家 SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625643_SPY_FAMILY_25___END_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][25集 - END][720P][AVC][繁日雙語]")
    assertEquals("間諜過家家 SPY×FAMILY..間諜過家家 SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625642_SPY_FAMILY_25___END_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][25集 - END][1080P][AVC][简日双语]")
    assertEquals("间谍过家家 SPY×FAMILY..间谍过家家 SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625641_SPY_FAMILY_25___END_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][25集 - END][720P][AVC][简日双语]")
    assertEquals("间谍过家家 SPY×FAMILY..间谍过家家 SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625525_10_SPYxFAMILY_23_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][23][1080p][繁日雙語][招募翻譯]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625524_10_SPYxFAMILY_23_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][23][720p][繁日雙語][招募翻譯]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625474_NC_Raws_SPY_FAMILY___25_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 25 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625432_NC_Raws_SPY_FAMILY___25_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 25 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("25..25", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625354_10_SPYxFAMILY_23_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][23][1080p][简日双语][招募翻译]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625353_10_SPYxFAMILY_23_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][23][720p][简日双语][招募翻译]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625292_10_Spy_x_Family_24_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][24][1080p][MP4][GB][简中]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625270_Spy_x_Family_23_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][23][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `625137_Spy_x_Family_24_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [24][1080p][简体内嵌]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `625136_Spy_x_Family_24_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [24][1080p][简繁内封]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `625111_404_23_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][23][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624994_Spy_x_Family_24_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [24] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `624950_Studio_10_SPY_FAMILY_Part_2_24_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][24][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624949_Studio_10_SPY_FAMILY_Part_2_24_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][24][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624948_Studio_10_SPY_FAMILY_Part_2_24_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][24][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624947_Studio_10_SPY_FAMILY_Part_2_24_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][24][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624826_SPY_FAMILY_24_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][24集][1080P][AVC][繁日雙語]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624825_SPY_FAMILY_24_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][24集][720P][AVC][繁日雙語]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624824_SPY_FAMILY_24_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][24集][1080P][AVC][简日双语]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624823_SPY_FAMILY_24_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][24集][720P][AVC][简日双语]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624757_NC_Raws_SPY_FAMILY___24_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 24 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624722_NC_Raws_SPY_FAMILY___24_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 24 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624664_jibaketa_SPY_FAMILY___18_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 18 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `624662_jibaketa_SPY_FAMILY___17_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 17 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `624646_jibaketa_SPY_FAMILY___16_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 16 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `624593_SPY_FAMILY_22_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [22][简体双语][1080p]招募时轴后期")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624558_10_SPY_FAMILY_20_22_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【20-22】【BIG5_MP4】【1280X720】")
    assertEquals("20..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624557_10_SPY_FAMILY_20_22_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【20-22】【GB_MP4】【1280X720】")
    assertEquals("20..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624553_Spy_x_Family_22_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][22][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624503_404_22_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][22][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624444_LoliHouse_Spy_x_Family___21_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 21 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `624443_LoliHouse_Spy_x_Family___20_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 20 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `624411_10_Spy_x_Family_23_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][23][1080p][MP4][GB][简中]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624370_04_SPY_FAMILY_21_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][21][1080P][简体][MP4]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624369_04_SPY_FAMILY_21_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][21][1080P][繁體][MP4]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624368_04_SPY_FAMILY_21_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][21][720P][简体][MP4]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624367_04_SPY_FAMILY_21_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][21][720P][繁體][MP4]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624337_Spy_x_Family_23_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [23][1080p][简体内嵌]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `624335_Spy_x_Family_23_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [23][1080p][简繁内封]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `624315_Spy_x_Family_23_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [23] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `624222_Studio_10_SPY_FAMILY_Part_2_23_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][23][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624221_Studio_10_SPY_FAMILY_Part_2_23_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][23][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624220_Studio_10_SPY_FAMILY_Part_2_23_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][23][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624219_Studio_10_SPY_FAMILY_Part_2_23_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][23][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624049_SPY_FAMILY_23_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][23集][1080P][AVC][繁日雙語]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624048_SPY_FAMILY_23_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][23集][720P][AVC][繁日雙語]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624047_SPY_FAMILY_23_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][23集][1080P][AVC][简日双语]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `624046_SPY_FAMILY_23_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][23集][720P][AVC][简日双语]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623999_NC_Raws_SPY_FAMILY___23_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 23 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623968_NC_Raws_SPY_FAMILY___23_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 23 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623924_10_SPYxFAMILY_22_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][22][1080p][繁日雙語][招募翻譯]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623923_10_SPYxFAMILY_22_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][22][720p][繁日雙語][招募翻譯]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623872_10_SPYxFAMILY_22_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][22][1080p][简日双语][招募翻译]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623871_10_SPYxFAMILY_22_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][22][720p][简日双语][招募翻译]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623849_SPY_FAMILY_21_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [21][简体双语][1080p]招募时轴后期")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623762_Spy_x_Family_22_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [22][1080p][简体内嵌]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `623761_Spy_x_Family_22_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [22][1080p][简繁内封]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `623716_10_Spy_x_Family_22_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][22][1080p][MP4][GB][简中]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623572_04_SPY_FAMILY_20_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][20][1080P][简体][MP4]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623571_04_SPY_FAMILY_20_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][20][1080P][繁體][MP4]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623570_04_SPY_FAMILY_20_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][20][720P][简体][MP4]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623569_04_SPY_FAMILY_20_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][20][720P][繁體][MP4]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623554_Spy_x_Family_22_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [22] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `623495_404_21_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][21][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623486_Studio_10_SPY_FAMILY_Part_2_22_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][22][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623485_Studio_10_SPY_FAMILY_Part_2_22_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][22][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623484_Studio_10_SPY_FAMILY_Part_2_22_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][22][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623483_Studio_10_SPY_FAMILY_Part_2_22_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][22][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623321_SPY_FAMILY_22_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][22集][1080P][AVC][繁日雙語]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623320_SPY_FAMILY_22_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][22集][720P][AVC][繁日雙語]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623319_SPY_FAMILY_22_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][22集][1080P][AVC][简日双语]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623318_SPY_FAMILY_22_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][22集][720P][AVC][简日双语]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623253_NC_Raws_SPY_FAMILY___22_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 22 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623223_NC_Raws_SPY_FAMILY___22_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 22 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623205_10_SPYxFAMILY_21_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][21][1080p][繁日雙語][招募翻譯]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623204_10_SPYxFAMILY_21_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][21][720p][繁日雙語][招募翻譯]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623157_10_SPYxFAMILY_21_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][21][1080p][简日双语][招募翻译]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623156_10_SPYxFAMILY_21_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][21][720p][简日双语][招募翻译]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623090_Spy_x_Family_21_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][21][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `623020_LoliHouse_Spy_x_Family___19_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 19 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `622990_10_SPY_FAMILY_20_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【20】GB MP4_720P")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622989_10_SPY_FAMILY_20_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【20】GB MP4_1080P")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622925_10_Spy_x_Family_21_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][21][1080p][MP4][GB][简中]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622807_Spy_x_Family_21_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [21][1080p][简体内嵌]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `622803_Spy_x_Family_21_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [21][1080p][简繁内封]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `622786_10_SPYxFAMILY_20_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][20][1080p][繁日雙語][招募翻譯]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622785_10_SPYxFAMILY_20_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][20][720p][繁日雙語][招募翻譯]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622768_10_SPYxFAMILY_20_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][20][1080p][简日双语][招募翻译]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622767_10_SPYxFAMILY_20_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][20][720p][简日双语][招募翻译]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622754_10_SPY_FAMILY_19_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【19】GB MP4_720P")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622753_10_SPY_FAMILY_19_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【19】GB MP4_1080P")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622744_Spy_x_Family_21_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [21] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `622701_Studio_10_SPY_FAMILY_Part_2_21_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][21][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622700_Studio_10_SPY_FAMILY_Part_2_21_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][21][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622699_Studio_10_SPY_FAMILY_Part_2_21_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][21][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622698_Studio_10_SPY_FAMILY_Part_2_21_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][21][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622694_SPY_FAMILY_20_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [20][简体双语][1080p]招募时轴后期")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622628_V2_SPY_FAMILY_21_1080P_AVC`() {
    val r = parse("[V2][織夢字幕組][間諜過家家 SPY×FAMILY][21集][1080P][AVC][繁日雙語]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622627_V2_SPY_FAMILY_21_720P_AVC`() {
    val r = parse("[V2][織夢字幕組][間諜過家家 SPY×FAMILY][21集][720P][AVC][繁日雙語]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622626_V2_SPY_FAMILY_21_1080P_AVC`() {
    val r = parse("[V2][织梦字幕组][间谍过家家 SPY×FAMILY][21集][1080P][AVC][简日双语]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622625_V2_SPY_FAMILY_21_720P_AVC`() {
    val r = parse("[V2][织梦字幕组][间谍过家家 SPY×FAMILY][21集][720P][AVC][简日双语]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622478_jibaketa_SPY_FAMILY___15_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 15 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `622470_10_SPY_FAMILY_18_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【18】GB MP4_720P")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622469_10_SPY_FAMILY_18_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【18】GB MP4_1080P")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622447_SPY_FAMILY_21_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][21集][1080P][AVC][繁日雙語]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622446_SPY_FAMILY_21_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][21集][720P][AVC][繁日雙語]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622445_SPY_FAMILY_21_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][21集][1080P][AVC][简日双语]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622444_SPY_FAMILY_21_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][21集][720P][AVC][简日双语]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622373_NC_Raws_SPY_FAMILY___21_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 21 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622363_jibaketa_SPY_FAMILY___14_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 14 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `622347_NC_Raws_SPY_FAMILY___21_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 21 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622260_404_20_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][20][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622251_jibaketa_SPY_FAMILY___13_WEB_1920x1080_AVC_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成][代理商粵語]SPY×FAMILY間諜家家酒 / 间谍过家家 - 13 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 AVC AACx2 SRTx2 MUSE CHT)")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `622142_10_Spy_x_Family_20_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][20][1080p][MP4][GB][简中]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622017_SPY_FAMILY_19_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [19][简体双语][1080p]招募时轴后期")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622013_10_SPYxFAMILY_19_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][19][1080p][繁日雙語][招募翻譯]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622012_10_SPYxFAMILY_19_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][19][1080p][简日双语][招募翻译]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622011_10_SPYxFAMILY_19_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][19][720p][繁日雙語][招募翻譯]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `622010_10_SPYxFAMILY_19_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][19][720p][简日双语][招募翻译]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621934_Spy_x_Family_20_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [20] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `621908_Spy_x_Family_20_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [20][1080p][简体内嵌]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `621907_Spy_x_Family_20_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [20][1080p][简繁内封]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `621860_Spy_x_Family_20_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][20][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621848_10_SPY_FAMILY_17_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【17】GB MP4_720P")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621847_10_SPY_FAMILY_17_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【17】GB MP4_1080P")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621783_04_SPY_FAMILY_19_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][19][1080P][简体][MP4]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621782_04_SPY_FAMILY_19_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][19][1080P][繁體][MP4]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621781_04_SPY_FAMILY_19_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][19][720P][简体][MP4]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621780_04_SPY_FAMILY_19_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][19][720P][繁體][MP4]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621745_Studio_10_SPY_FAMILY_Part_2_20_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][20][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621744_Studio_10_SPY_FAMILY_Part_2_20_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][20][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621743_Studio_10_SPY_FAMILY_Part_2_20_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][20][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621742_Studio_10_SPY_FAMILY_Part_2_20_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][20][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621718_10_SPY_FAMILY_19_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【19】【BIG5_MP4】【1280X720】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621717_10_SPY_FAMILY_19_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【19】【GB_MP4】【1280X720】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621669_LoliHouse_Spy_x_Family___18_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 18 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `621668_LoliHouse_Spy_x_Family___17_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 17 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `621667_LoliHouse_Spy_x_Family___16_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 16 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `621623_SPY_FAMILY_20_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][20集][1080P][AVC][繁日雙語]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621622_SPY_FAMILY_20_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][20集][720P][AVC][繁日雙語]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621621_SPY_FAMILY_20_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][20集][1080P][AVC][简日双语]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621620_SPY_FAMILY_20_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][20集][720P][AVC][简日双语]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621508_NC_Raws_SPY_FAMILY___20_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 20 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621476_NC_Raws_SPY_FAMILY___20_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 20 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621348_10_SPY_FAMILY_18_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【18】【BIG5_MP4】【1280X720】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621347_10_SPY_FAMILY_18_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【18】【GB_MP4】【1280X720】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621275_404_19_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][19][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621192_04_SPY_FAMILY_16_18_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][16-18][1080P][简体][MP4]")
    assertEquals("16..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621190_04_SPY_FAMILY_16_18_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][16-18][720P][简体][MP4]")
    assertEquals("16..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621191_04_SPY_FAMILY_16_18_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][16-18][1080P][繁體][MP4]")
    assertEquals("16..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621189_04_SPY_FAMILY_16_18_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][16-18][720P][繁體][MP4]")
    assertEquals("16..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621174_SPY_FAMILY_18_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [18][简体双语][1080p]招募时轴后期")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621173_SPY_FAMILY_17_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [17][简体双语][1080p]招募时轴后期")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621134_10_Spy_x_Family_19_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][19][1080p][MP4][GB][简中]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621128_7_ACG_x_SPY_FAMILY_01_12_Part_1_BDrip_1080p_x265_FLAC`() {
    val r =
        parse("[7³ACG x 桜都字幕组] SPY×FAMILY/间谍过家家/间谍家家酒/スパイファミリー | 01-12(Part 1) [简繁字幕] BDrip 1080p x265 FLAC")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `621076_Spy_x_Family_19_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [19][1080p][简体内嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `621074_Spy_x_Family_19_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [19][1080p][简繁内封]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `621028_Spy_x_Family_19_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [19] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `620970_Studio_10_SPY_FAMILY_Part_2_19_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][19][1080p][HEVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620969_Studio_10_SPY_FAMILY_Part_2_19_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][19][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620968_Studio_10_SPY_FAMILY_Part_2_19_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][19][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620967_Studio_10_SPY_FAMILY_Part_2_19_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][19][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620946_10_SPY_FAMILY_17_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【17】【BIG5_MP4】【1280X720】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620936_Spy_x_Family_19_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][19][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620888_10_SPYxFAMILY_18_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][18][1080p][繁日雙語][招募翻譯]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620887_10_SPYxFAMILY_18_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][18][1080p][简日双语][招募翻译]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620886_10_SPYxFAMILY_18_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][18][720p][繁日雙語][招募翻譯]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620885_10_SPYxFAMILY_18_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][18][720p][简日双语][招募翻译]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620800_SPY_FAMILY_19_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][19集][1080P][AVC][繁日雙語]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620799_SPY_FAMILY_19_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][19集][720P][AVC][繁日雙語]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620798_SPY_FAMILY_19_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][19集][1080P][AVC][简日双语]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620797_SPY_FAMILY_19_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][19集][720P][AVC][简日双语]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620750_NC_Raws_SPY_FAMILY___19_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 19 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620712_NC_Raws_SPY_FAMILY___19_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 19 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620656_404_18_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][18][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620551_Studio_10_SPY_FAMILY_Part_2_18_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][18][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620550_Studio_10_SPY_FAMILY_Part_2_18_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][18][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620549_Studio_10_SPY_FAMILY_Part_2_18_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][18][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620548_Studio_10_SPY_FAMILY_Part_2_18_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][18][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620395_10_SPY_FAMILY_17_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【17】【GB_MP4】【1280X720】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620384_10_SPY_FAMILY_16_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【16】GB MP4_720P")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620383_10_SPY_FAMILY_16_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【16】GB MP4_1080P")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620360_10_Spy_x_Family_18_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][18][1080p][MP4][GB][简中]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620325_Spy_x_Family_18_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [18][1080p][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `620320_Spy_x_Family_18_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [18][1080p][简繁内封]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `620263_Spy_x_Family_18_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][18][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620259_Spy_x_Family_18_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [18] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `620009_10_SPYxFAMILY_17_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][17][1080p][繁日雙語][招募翻譯]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620008_10_SPYxFAMILY_17_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][17][1080p][简日双语][招募翻译]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620007_10_SPYxFAMILY_17_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][17][720p][繁日雙語][招募翻譯]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `620006_10_SPYxFAMILY_17_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][17][720p][简日双语][招募翻译]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619946_SPY_FAMILY_18_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][18集][1080P][AVC][繁日雙語]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619945_SPY_FAMILY_18_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][18集][720P][AVC][繁日雙語]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619944_SPY_FAMILY_18_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][18集][1080P][AVC][简日双语]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619943_SPY_FAMILY_18_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][18集][720P][AVC][简日双语]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619876_NC_Raws_SPY_FAMILY___18_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 18 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619864_NC_Raws_SPY_FAMILY___18_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 18 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619640_404_17_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][17][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619546_SPY_FAMILY_16_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [16][简体双语][1080p]招募时轴后期")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619461_V2_SPY_FAMILY_13_1080P_AVC`() {
    val r = parse("[V2][織夢字幕組][間諜過家家 SPY×FAMILY][13集][1080P][AVC][繁日雙語]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619460_V2_SPY_FAMILY_13_720P_AVC`() {
    val r = parse("[V2][織夢字幕組][間諜過家家 SPY×FAMILY][13集][720P][AVC][繁日雙語]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619459_V2_SPY_FAMILY_13_1080P_AVC`() {
    val r = parse("[V2][织梦字幕组][间谍过家家 SPY×FAMILY][13集][1080P][AVC][简日双语]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619458_V2_SPY_FAMILY_13_720P_AVC`() {
    val r = parse("[V2][织梦字幕组][间谍过家家 SPY×FAMILY][13集][720P][AVC][简日双语]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619414_10_Spy_x_Family_17_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][17][1080p][MP4][GB][简中]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619401_LoliHouse_Spy_x_Family___15_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 15 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `619376_10_SPY_FAMILY_16_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【16】【BIG5_MP4】【1280X720】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619375_10_SPY_FAMILY_16_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【16】【GB_MP4】【1280X720】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619371_Studio_10_SPY_FAMILY_Part_2_17_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][17][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619370_Studio_10_SPY_FAMILY_Part_2_17_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][17][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619369_Studio_10_SPY_FAMILY_Part_2_17_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][17][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619368_Studio_10_SPY_FAMILY_Part_2_17_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][17][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619364_Spy_x_Family_17_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [17][1080p][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `619363_Spy_x_Family_17_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [17][1080p][简繁内封]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `619361_Spy_x_Family_17_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [17] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `619298_10_SPYxFAMILY_16_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][16][1080p][繁日雙語][招募翻譯]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619297_10_SPYxFAMILY_16_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][16][1080p][简日双语][招募翻译]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619296_10_SPYxFAMILY_16_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][16][720p][繁日雙語][招募翻譯]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619295_10_SPYxFAMILY_16_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][16][720p][简日双语][招募翻译]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619249_Spy_x_Family_17_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][17][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619040_SPY_FAMILY_17_1080P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][17集][1080P][AVC][繁日雙語]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619039_SPY_FAMILY_17_720P_AVC`() {
    val r = parse("[織夢字幕組][間諜過家家 SPY×FAMILY][17集][720P][AVC][繁日雙語]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619038_SPY_FAMILY_17_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][17集][1080P][AVC][简日双语]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `619037_SPY_FAMILY_17_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 SPY×FAMILY][17集][720P][AVC][简日双语]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618988_NC_Raws_SPY_FAMILY___17_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 17 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618972_NC_Raws_SPY_FAMILY___17_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 17 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618754_04_SPY_FAMILY_15_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][15][1080P][简体][MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618753_04_SPY_FAMILY_15_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][15][1080P][繁體][MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618752_04_SPY_FAMILY_15_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][15][720P][简体][MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618751_04_SPY_FAMILY_15_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][15][720P][繁體][MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618715_Spy_x_Family_16_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][16][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618714_Spy_x_Family_16_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][16][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618687_404_16_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][16][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618674_10_Spy_x_Family_16_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][16][1080p][MP4][GB][简中]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618648_10_SPY_FAMILY_14_15_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【14-15】GB MP4_720P")
    assertEquals("14..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618647_10_SPY_FAMILY_14_15_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【14-15】GB MP4_1080P")
    assertEquals("14..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618615_Spy_x_Family_16_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [16][1080p][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `618614_Spy_x_Family_16_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [16][1080p][简繁内封]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `618514_Studio_10_SPY_FAMILY_Part_2_16_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][16][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618513_Studio_10_SPY_FAMILY_Part_2_16_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][16][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618511_Studio_10_SPY_FAMILY_Part_2_16_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][16][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618510_Studio_10_SPY_FAMILY_Part_2_16_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][16][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618411_Spy_x_Family_16_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [16] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `618320_SPY_FAMILY_15_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [15][简体双语][1080p]招募时轴后期")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618252_Spy_x_Family_16_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][16][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618104_16_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分][16集][1080P][AVC][简日双语]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618103_16_1080P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分][16集][1080P][AVC][繁日双语]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618102_16_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分][16集][720P][AVC][简日双语]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618101_16_720P_AVC`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分][16集][720P][AVC][繁日双语]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618049_NC_Raws_SPY_FAMILY___16_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 16 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `618041_NC_Raws_SPY_FAMILY___16_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 16 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617902_404_15_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][15][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617868_Spy_x_Family_15_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][15][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617867_Spy_x_Family_15_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][15][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617847_10_SPY_FAMILY_15_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【15】【BIG5_MP4】【1280X720】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617846_10_SPY_FAMILY_15_GB_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【15】【GB_MP4】【1280X720】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617650_10_SPY_FAMILY_14_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【14】【BIG5_MP4】【1280X720】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617649_10_SPY_FAMILY_14_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【14】【GB_MP4】【1280X720】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617636_LoliHouse_Spy_x_Family___14_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 14 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `617635_LoliHouse_Spy_x_Family___13_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 13 [WebRip 1080p HEVC-10bit AAC][简繁外挂字幕]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `617628_10_Spy_x_Family_15_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][15][1080p][MP4][GB][简中]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617569_04_SPY_FAMILY_13_14_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][13-14][1080P][简体][MP4]")
    assertEquals("13..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617568_04_SPY_FAMILY_13_14_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][13-14][1080P][繁體][MP4]")
    assertEquals("13..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617567_04_SPY_FAMILY_13_14_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][13-14][720P][简体][MP4]")
    assertEquals("13..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617566_04_SPY_FAMILY_13_14_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][13-14][720P][繁體][MP4]")
    assertEquals("13..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617543_SPY_FAMILY_14_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [14][简体双语][1080p]招募时轴后期")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617474_10_SPYxFAMILY_15_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][15][1080p][繁日雙語][招募翻譯]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617473_10_SPYxFAMILY_15_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][15][1080p][简日双语][招募翻译]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617472_10_SPYxFAMILY_15_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][15][720p][繁日雙語][招募翻譯]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617471_10_SPYxFAMILY_15_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][15][720p][简日双语][招募翻译]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617444_Spy_x_Family_15_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [15] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `617429_Spy_x_Family_15_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [15][1080p][简繁内封]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `617427_Spy_x_Family_15_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [15][1080p][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `617375_Studio_10_SPY_FAMILY_Part_2_15_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][15][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617374_Studio_10_SPY_FAMILY_Part_2_15_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][15][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617373_Studio_10_SPY_FAMILY_Part_2_15_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][15][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617372_Studio_10_SPY_FAMILY_Part_2_15_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][15][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617271_Spy_x_Family_15_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][15][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617200_SPY_FAMILY_15_AVC_1080P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][15集][AVC][簡日雙語][1080P]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617199_SPY_FAMILY_15_AVC_720P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][15集][AVC][簡日雙語][720P]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617198_SPY_FAMILY_15_AVC_1080P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][15集][AVC][简日双语][1080P]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617197_SPY_FAMILY_15_AVC_720P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][15集][AVC][简日双语][720P]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617140_NC_Raws_SPY_FAMILY___15_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 15 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617129_NC_Raws_SPY_FAMILY___15_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 15 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `617090_404_14_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][14][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616930_10_Spy_x_Family_14_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][14][1080p][MP4][GB][简中]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616869_SPY_FAMILY_13_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [13][简体双语][1080p]招募时轴后期")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616841_Spy_x_Family_14_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][14][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616840_Spy_x_Family_14_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][14][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616661_Spy_x_Family_14_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [14][1080p][简繁内封]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `616659_Spy_x_Family_14_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [14][1080p][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `616645_Spy_x_Family_14_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [14] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `616636_Spy_x_Family_14_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][14][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616534_10_SPYxFAMILY_14_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][14][1080p][繁日雙語][招募翻譯]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616533_10_SPYxFAMILY_14_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][14][1080p][简日双语][招募翻译]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616532_10_SPYxFAMILY_14_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][14][720p][繁日雙語][招募翻譯]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616531_10_SPYxFAMILY_14_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][14][720p][简日双语][招募翻译]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616381_10_SPY_FAMILY_13_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【13】GB MP4_1080P")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616379_10_SPY_FAMILY_13_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★10月新番 【间谍过家家】【SPY×FAMILY】【13】GB MP4_720P")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616370_10_SPY_FAMILY_13_BIG5_MP4_1280X720_V2`() {
    val r = parse("【幻櫻字幕組】【10月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【13】【BIG5_MP4】【1280X720】【V2】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616369_10_SPY_FAMILY_13_GB_MP4_1280X720_V2`() {
    val r = parse("【幻櫻字幕組】【10月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【13】【GB_MP4】【1280X720】【V2】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616322_Studio_10_SPY_FAMILY_Part_2_14_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][14][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616321_Studio_10_SPY_FAMILY_Part_2_14_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][14][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616320_Studio_10_SPY_FAMILY_Part_2_14_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][14][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616317_Studio_10_SPY_FAMILY_Part_2_14_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][14][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616227_SPY_FAMILY_14_AVC_1080P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][14集][AVC][簡日雙語][1080P]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616224_SPY_FAMILY_14_AVC_720P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][14集][AVC][簡日雙語][720P]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616223_SPY_FAMILY_14_AVC_1080P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][14集][AVC][简日双语][1080P]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616222_SPY_FAMILY_14_AVC_720P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][14集][AVC][简日双语][720P]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616197_Spy_x_Family_13_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][13][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616196_Spy_x_Family_13_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][13][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616166_NC_Raws_SPY_FAMILY___14_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 14 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616150_NC_Raws_SPY_FAMILY___14_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 14 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `616147_NC_Raws_SPY_FAMILY___14_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 14 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615847_Spy_x_Family_13_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / Spy x Family][13][简日双语][1080P][WEBrip][MP4]（急招翻译、校对）")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615803_10_Spy_x_Family_13_1080p_MP4_GB`() {
    val r = parse("[爱恋字幕社&猫恋汉化组][10月新番][间谍过家家][Spy x Family][13][1080p][MP4][GB][简中]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615737_404_13_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][13][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615611_10_SPYxFAMILY_13_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][13][1080p][繁日雙語][招募翻譯]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615610_10_SPYxFAMILY_13_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][13][720p][繁日雙語][招募翻譯]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615604_10_SPYxFAMILY_13_1080p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][13][1080p][简日双语][招募翻译]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615603_10_SPYxFAMILY_13_720p`() {
    val r = parse("【喵萌奶茶屋】★10月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][13][720p][简日双语][招募翻译]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615602_Spy_x_Family_13_1080p_2022_10`() {
    val r = parse("[猎户不鸽压制] 间谍过家家 Spy x Family [13] [1080p] [简日内嵌] [2022年10月番]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `615571_Spy_x_Family_13_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [13][1080p][简繁内封]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `615569_Spy_x_Family_13_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [13][1080p][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `615568_Studio_10_SPY_FAMILY_Part_2_13_1080p_HEVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][13][1080p][HEVC][BIG5]][MP4][招募翻譯校對]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615567_Studio_10_SPY_FAMILY_Part_2_13_1080p_AVC_BIG5_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[間諜過家家 第二部分/SPY×FAMILY Part 2][13][1080p][AVC][BIG5][MP4][招募翻譯校對]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615566_Studio_10_SPY_FAMILY_Part_2_13_1080p_HEVC_GB_MP4`() {
    val r =
        parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][13][1080p][HEVC][GB][MP4][招募翻译校对]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615565_Studio_10_SPY_FAMILY_Part_2_13_1080p_AVC_GB_MP4`() {
    val r = parse("【爪爪Studio】★10月新番[间谍过家家 第二部分/SPY×FAMILY Part 2][13][1080p][AVC][GB][MP4][招募翻译校对]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615367_SPY_FAMILY_13_AVC_720P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][13集][AVC][簡日雙語][720P]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615366_SPY_FAMILY_13_AVC_1080P`() {
    val r = parse("[織夢字幕組][間諜過家家 第二部分 SPY×FAMILY][13集][AVC][簡日雙語][1080P]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615365_SPY_FAMILY_13_AVC_720P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][13集][AVC][简日双语][720P]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615364_SPY_FAMILY_13_AVC_1080P`() {
    val r = parse("[织梦字幕组][间谍过家家 第二部分 SPY×FAMILY][13集][AVC][简日双语][1080P]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615284_NC_Raws_SPY_FAMILY___13_CR_1920x1080_AVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 13 (CR 1920x1080 AVC AAC MKV)")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `615279_NC_Raws_SPY_FAMILY___13_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 13 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `609032_04_SPYxFAMILY_01_12_1080p_v2`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01-12][1080p][繁日雙語][v2][招募翻譯]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `609031_04_SPYxFAMILY_01_12_1080p_v2`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01-12][1080p][简日双语][v2][招募翻译]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `609030_04_SPYxFAMILY_01_12_720p_v2`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01-12][720p][繁日雙語][v2][招募翻譯]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `609029_04_SPYxFAMILY_01_12_720p_v2`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01-12][720p][简日双语][v2][招募翻译]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `608944_04_SPYxFAMILY_01_12_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01-12][1080p][繁日雙語][招募翻譯]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `608943_04_SPYxFAMILY_01_12_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01-12][1080p][简日双语][招募翻译]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `608942_04_SPYxFAMILY_01_12_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01-12][720p][繁日雙語][招募翻譯]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `608941_04_SPYxFAMILY_01_12_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01-12][720p][简日双语][招募翻译]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `608760_4K___2160p_Spy_Family`() {
    val r = parse("V2 间谍过家家4K 粤日双语 - 2160p Spy × Family 附加字体 外挂楷体字幕")
    assertEquals("Spy..Spy", r.episodeRange.toString())
    assertEquals("CHC, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(SubtitleKind.EXTERNAL, r.subtitleKind)
  }

  @Test
  public fun `607638_jibaketa_SPY_FAMILY___12_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 12 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `607637_jibaketa_SPY_FAMILY___11_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 11 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `607557_jibaketa_SPY_FAMILY___10_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 10 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `607556_jibaketa_SPY_FAMILY___09_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 09 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `607523_jibaketa_SPY_FAMILY___08_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 08 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `607447_404_01_12_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][01-12][1080P][WebRip][简日双语(内嵌+外挂)][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `607145_jibaketa_SPY_FAMILY___07_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 07 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `606777_jibaketa_SPY_FAMILY___06_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 06 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `606524_jibaketa_SPY_FAMILY___05_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 05 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `605504_jibaketa_SPY_FAMILY___04_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 04 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `605503_jibaketa_SPY_FAMILY___03_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 03 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `605100_jibaketa_SPY_FAMILY___02_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 02 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604907_LoliHouse_Spy_x_Family___12_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 12 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604895_04_SPY_FAMILY_12_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][12][1080P][简体][MP4](前半完)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604894_04_SPY_FAMILY_12_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][12][1080P][繁體][MP4](前半完)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604893_04_SPY_FAMILY_12_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][12][720P][简体][MP4](前半完)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604892_04_SPY_FAMILY_12_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][12][720P][繁體][MP4](前半完)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604887_SPY_FAMILY_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [合集][简体双语][1080p]招募翻译")
    assertEquals("SPY×FAMILY..SPY×FAMILY", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604884_SPY_FAMILY_12_END_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [12][END][简体双语][1080p]招募后期")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604830_Spy_x_Family_01_12_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [01-12 Fin][1080p][简繁内封]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604827_Spy_x_Family_01_12_Fin_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [01-12 Fin][1080p][简体内嵌]")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `604817_jibaketa_SPY_FAMILY___01_WEB_1920x1080_x264_AACx2_SRTx2_MUSE_CHT`() {
    val r =
        parse("[jibaketa合成&壓制][代理商粵語]SPY×FAMILY間諜家家酒 / 間諜過家家 - 01 [粵日雙語+內封繁體中文字幕](WEB 1920x1080 x264 AACx2 SRTx2 MUSE CHT)")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHC, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604792_4_SPY_FAMILY_12_END_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【12】【END】GB MP4_720P")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604791_4_SPY_FAMILY_12_END_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【12】【END】GB MP4_1080P")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604712_4_SPY_FAMILY_11_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【11】GB MP4_720P")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604711_4_SPY_FAMILY_11_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【11】GB MP4_1080P")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604706_4_SPY_FAMILY_10_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【10】GB MP4_720P")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604705_4_SPY_FAMILY_10_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【10】GB MP4_1080P")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604500_04_SPYxFAMILY_10_11_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][10-11][1080p][繁日雙語][招募翻譯]")
    assertEquals("10..11", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604499_04_SPYxFAMILY_10_11_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][10-11][720p][繁日雙語][招募翻譯]")
    assertEquals("10..11", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604401_404_12_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][12][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604341_4_SPY_FAMILY_12_END_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【12】【END】【BIG5_MP4】【1280X720】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604340_4_SPY_FAMILY_12_END_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【12】【END】【GB_MP4】【1280X720】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604270_4_Spy_X_Family_BIG5_10_1080P`() {
    val r = parse("[動漫萌][4月新番][間諜過家家/間諜家家酒/Spy X Family ][BIG5][10][1080P](字幕組招募內詳)")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604269_4_Spy_X_Family_GB_10_1080P`() {
    val r = parse("[动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][10][1080P](字幕组招募内详)")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604268_SPY_FAMILY_01_12Fin_WEBRIP_1080p_AVC_AAC_2022_4`() {
    val r = parse("[爱恋&漫猫字幕组] 间谍过家家/SPY × FAMILY (01-12Fin WEBRIP 1080p AVC AAC 2022年4月 简中)")
    assertEquals("01..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604266_4_SPY_FAMILY_12_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][12][1080p][MP4][简中]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604259_4_Spy_X_Family_BIG5_09_1080P`() {
    val r = parse("[動漫萌][4月新番][間諜過家家/間諜家家酒/Spy X Family ][BIG5][09][1080P](字幕組招募內詳)")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604258_4_Spy_X_Family_GB_09_1080P`() {
    val r = parse("[动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][09][1080P](字幕组招募内详)")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604249_4_Spy_X_Family_BIG5_08_1080P`() {
    val r = parse("[動漫萌][4月新番][間諜過家家/間諜家家酒/Spy X Family ][BIG5][08][1080P](字幕組招募內詳)")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604248_4_Spy_X_Family_GB_08_1080P`() {
    val r = parse("[动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][08][1080P](字幕组招募内详)")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604240_4_Spy_X_Family_BIG5_07_1080P`() {
    val r = parse("[動漫萌][4月新番][間諜過家家/間諜家家酒/Spy X Family ][BIG5][07][1080P](字幕組招募內詳)")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604239_4_Spy_X_Family_GB_07_1080P`() {
    val r = parse("动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][07][1080P](字幕组招募内详)")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604216_4_SPY_FAMILY_09_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【09】GB MP4_720P")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604215_4_SPY_FAMILY_09_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【09】GB MP4_1080P")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604190_Spy_x_Family_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [12][1080p][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `604183_Spy_x_Family_12_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [12][1080p][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604158_4_SPY_FAMILY_08_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【08】GB MP4_720P")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604157_4_SPY_FAMILY_08_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【08】GB MP4_1080P")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604131_LoliHouse_Spy_x_Family___11_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 11 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `604122_SPY_FAMILY_11_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [11][简体双语][1080p]招募后期")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604117_04_SPY_FAMILY_11_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][11][1080P][简体][MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604116_04_SPY_FAMILY_11_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][11][1080P][繁體][MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604115_04_SPY_FAMILY_11_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][11][720P][简体][MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604114_04_SPY_FAMILY_11_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][11][720P][繁體][MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604025_Spy_x_Family_12_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][12][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604024_Spy_x_Family_12_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][12][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `604008_MingY_Spy_x_Family_12_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [12][1080p][简日内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `603990_04_SPYxFAMILY_11_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][11][1080p][简日双语][招募翻译]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603989_04_SPYxFAMILY_11_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][11][720p][简日双语][招募翻译]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603927_NC_Raws_SPY_FAMILY___12_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 12 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603775_404_11_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][11][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603707_4_SPY_FAMILY_11_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【11】【BIG5_MP4】【1280X720】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603706_4_SPY_FAMILY_11_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【11】【GB_MP4】【1280X720】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603593_4_SPY_FAMILY_11_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][11][1080p][MP4][简中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603571_Spy_x_Family_04_1080p_HEVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第04話][1080p_HEVC][簡繁內封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `603570_Spy_x_Family_04_1080p_AVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第04話][1080p_AVC][繁體]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603569_Spy_x_Family_04_1080p_AVC`() {
    val r = parse("【千夏字幕组】【间谍过家家_Spy x Family】[第04话][1080p_AVC][简体]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603549_Spy_x_Family_11_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [11][1080p][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `603545_Spy_x_Family_11_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [11][1080p][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `603542_Spy_x_Family_03_1080p_HEVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第03話][1080p_HEVC][簡繁內封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `603541_Spy_x_Family_03_1080p_AVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第03話][1080p_AVC][繁體]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603540_Spy_x_Family_03_1080p_AVC`() {
    val r = parse("【千夏字幕组】【间谍过家家_Spy x Family】[第03话][1080p_AVC][简体]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603441_LoliHouse_Spy_x_Family___10_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 10 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `603438_Spy_x_Family_11_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][11][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603437_Spy_x_Family_11_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][11][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603436_04_SPY_FAMILY_10_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][10][1080P][简体][MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603435_04_SPY_FAMILY_10_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][10][1080P][繁體][MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603434_04_SPY_FAMILY_10_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][10][720P][简体][MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603433_04_SPY_FAMILY_10_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][10][720P][繁體][MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603376_MingY_Spy_x_Family_11_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [11][1080p][简日内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `603332_NC_Raws_SPY_FAMILY___11_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 11 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603305_Spy_x_Family_10_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][10][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603304_Spy_x_Family_10_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][10][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603161_SPY_FAMILY_10_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [10][简体双语][1080p]招募后期")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603149_4_SPY_FAMILY_07_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【07】GB MP4_720P")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603148_4_SPY_FAMILY_07_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【07】GB MP4_1080P")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603142_04_SPYxFAMILY_10_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][10][1080p][简日双语][招募翻译]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603141_04_SPYxFAMILY_10_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][10][720p][简日双语][招募翻译]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603129_4_SPY_FAMILY_10_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【10】【BIG5_MP4】【1280X720】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603128_4_SPY_FAMILY_10_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【10】【GB_MP4】【1280X720】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `603055_404_10_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][10][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602892_Spy_x_Family_10_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [10][1080p][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `602890_Spy_x_Family_10_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [10][1080p][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `602837_404_09_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][09][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602807_MingY_Spy_x_Family_10_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [10][1080p][简日内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `602730_LoliHouse_Spy_x_Family___09_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 09 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `602729_04_SPY_FAMILY_09_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][09][1080P][简体][MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602728_04_SPY_FAMILY_09_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][09][1080P][繁體][MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602727_04_SPY_FAMILY_09_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][09][720P][简体][MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602726_04_SPY_FAMILY_09_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][09][720P][繁體][MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602718_NC_Raws_SPY_FAMILY___10_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 10 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602684_4_SPY_FAMILY_06_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【06】GB MP4_720P")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602683_4_SPY_FAMILY_06_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【06】GB MP4_1080P")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602665_LoliHouse_Spy_x_Family___08_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 08 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `602649_SPY_FAMILY_09_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [09][简体双语][1080p]招募后期")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602504_Spy_x_Family_06_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[间谍过家家 / Spy x Family][06][x264 1080p][CHS]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602499_4_SPY_FAMILY_09_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【09】【BIG5_MP4】【1280X720】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602498_4_SPY_FAMILY_09_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【09】【GB_MP4】【1280X720】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602492_04_SPYxFAMILY_09_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][09][1080p][繁日雙語][招募翻譯]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602491_04_SPYxFAMILY_09_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][09][1080p][简日双语][招募翻译]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602490_04_SPYxFAMILY_09_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][09][720p][繁日雙語][招募翻譯]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602489_04_SPYxFAMILY_09_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][09][720p][简日双语][招募翻译]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602424_Spy_x_Family_09v2_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [09v2][1080p][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `602358_4_SPY_FAMILY_09_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][09][1080p][MP4][简中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602334_Spy_x_Family_09_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [09][1080p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `602254_SPY_FAMILY_08_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [08][简体双语][1080p]招募后期")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602240_Spy_x_Family_09_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][09][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602239_Spy_x_Family_09_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][09][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602237_404_08_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][08][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602158_MingY_Spy_x_Family_09_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [09][1080p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `602121_04_SPY_FAMILY_08_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][08][1080P][简体][MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602120_04_SPY_FAMILY_08_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][08][1080P][繁體][MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602119_04_SPY_FAMILY_08_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][08][720P][简体][MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602118_04_SPY_FAMILY_08_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][08][720P][繁體][MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602109_NC_Raws_SPY_FAMILY___09_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 09 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602058_Spy_x_Family_08_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][08][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `602057_Spy_x_Family_08_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][08][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601854_04_SPYxFAMILY_08_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][08][1080p][繁日雙語][招募翻譯]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601853_04_SPYxFAMILY_08_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][08][720p][繁日雙語][招募翻譯]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601798_4_SPY_FAMILY_08_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][08][1080p][MP4][简中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601786_4_SPY_FAMILY_08_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【08】【BIG5_MP4】【1280X720】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601785_4_SPY_FAMILY_08_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【08】【GB_MP4】【1280X720】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601727_04_SPYxFAMILY_08_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][08][1080p][简日双语][招募翻译]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601726_04_SPYxFAMILY_08_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][08][720p][简日双语][招募翻译]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601685_Spy_x_Family_08_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [08][1080p][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `601683_Spy_x_Family_08_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [08][1080p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `601659_4_SPY_FAMILY_05_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【05】GB MP4_720P")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601658_4_SPY_FAMILY_05_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【05】GB MP4_1080P")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601607_LoliHouse_Spy_x_Family___07_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 07 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `601587_04_SPY_FAMILY_07_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][07][1080P][简体][MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601586_04_SPY_FAMILY_07_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][07][1080P][繁體][MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601585_04_SPY_FAMILY_07_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][07][720P][简体][MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601584_04_SPY_FAMILY_07_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][07][720P][繁體][MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601576_MingY_Spy_x_Family_08_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [08][1080p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `601488_NC_Raws_SPY_FAMILY___08_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 08 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601368_4_SPY_FAMILY_07_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【07】【BIG5_MP4】【1280X720】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601367_4_SPY_FAMILY_07_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【07】【GB_MP4】【1280X720】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601282_04_SPYxFAMILY_07_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][07][1080p][繁日雙語][招募翻譯]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601281_04_SPYxFAMILY_07_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][07][1080p][简日双语][招募翻译]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601280_04_SPYxFAMILY_07_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][07][720p][繁日雙語][招募翻譯]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601279_04_SPYxFAMILY_07_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][07][720p][简日双语][招募翻译]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601273_SPY_FAMILY_07_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [07][简体双语][1080p]招募翻译")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601252_404_07_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][07][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601212_4_SPY_FAMILY_07_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][07][1080p][MP4][简中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601169_4_Spy_X_Family_BIG5_05_1080P`() {
    val r = parse("[動漫萌][4月新番][間諜過家家/間諜家家酒/Spy X Family ][BIG5][05][1080P](字幕組招募內詳)")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601168_4_Spy_X_Family_GB_05_1080P`() {
    val r = parse("[动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][05][1080P](字幕组招募内详)")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601167_Spy_x_Family_07_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [07][1080p][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `601165_Spy_x_Family_07_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [07][1080p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `601152_4_Spy_X_Family_GB_04_1080P`() {
    val r = parse("[动漫萌][4月新番][间谍过家家/间谍家家酒/Spy X Family ][GB][04][1080P]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601144_Spy_x_Family_07_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][07][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601141_Spy_x_Family_07_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][07][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `601107_Spy_x_Family_05_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[间谍过家家 / Spy x Family][05][x264 1080p][CHS]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600958_MingY_Spy_x_Family_07_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [07][1080p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `600864_NC_Raws_SPY_FAMILY___07_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 07 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600862_SPY_FAMILY_06_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [06][简体双语][1080p]招募翻译")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600855_04_SPYxFAMILY_06_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][06][1080p][繁日雙語][招募翻譯]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600854_04_SPYxFAMILY_06_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][06][720p][繁日雙語][招募翻譯]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600842_04_SPYxFAMILY_06_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][06][1080p][简日双语][招募翻译]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600841_04_SPYxFAMILY_06_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][06][720p][简日双语][招募翻译]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600810_LoliHouse_Spy_x_Family___06_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 06 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `600788_04_SPY_FAMILY_06_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][06][1080P][简体][MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600787_04_SPY_FAMILY_06_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][06][1080P][繁體][MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600786_04_SPY_FAMILY_06_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][06][720P][简体][MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600785_04_SPY_FAMILY_06_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][06][720P][繁體][MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600782_Spy_x_Family_06_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][06][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600781_Spy_x_Family_06_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][06][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600776_4_SPY_FAMILY_04v2_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【04v2】【BIG5_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600775_4_SPY_FAMILY_04v2_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【04v2】【GB_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600746_4_SPY_FAMILY_06_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【06】【BIG5_MP4】【1280X720】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600745_4_SPY_FAMILY_06_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【06】【GB_MP4】【1280X720】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600625_404_06_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][06][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600605_4_SPY_FAMILY_06_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][06][1080p][MP4][简中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600510_Spy_x_Family_06_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [06][1080p][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `600509_Spy_x_Family_06_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [06][1080p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `600436_Spy_x_Family_04_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[间谍过家家 / Spy x Family][04][x264 1080p][CHS]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600374_MingY_Spy_x_Family_06_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [06][1080p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `600354_LoliHouse_Spy_x_Family___05_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 05 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `600320_04_SPY_FAMILY_05_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][05][1080P][简体][MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600319_04_SPY_FAMILY_05_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][05][1080P][繁體][MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600318_04_SPY_FAMILY_05_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][05][720P][简体][MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600317_04_SPY_FAMILY_05_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][05][720P][繁體][MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600280_NC_Raws_SPY_FAMILY___06_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 06 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600208_SPY_FAMILY_05_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [05][简体双语][1080p]招募翻译")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600100_4_SPY_FAMILY_05_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【05】【BIG5_MP4】【1280X720】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600099_4_SPY_FAMILY_05_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【05】【GB_MP4】【1280X720】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600087_4_SPY_FAMILY_04_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【04】GB MP4_720P")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600086_4_SPY_FAMILY_04_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【04】GB MP4_1080P")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600076_4_SPY_FAMILY_03_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【03】GB MP4_720P")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `600075_4_SPY_FAMILY_03_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【03】GB MP4_1080P")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599989_04_SPYxFAMILY_05_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][05][1080p][繁日雙語][招募翻譯]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599988_04_SPYxFAMILY_05_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][05][720p][繁日雙語][招募翻譯]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599970_404_05_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][05][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599946_4_SPY_FAMILY_05_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][05][1080p][MP4][简中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599899_Spy_x_Family_05_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [05][1080p][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599897_Spy_x_Family_05_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [05][1080p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `599865_Spy_x_Family_05_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][05][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599864_Spy_x_Family_05_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][05][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599863_04_SPYxFAMILY_05_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][05][1080p][简日双语][招募翻译]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599862_04_SPYxFAMILY_05_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][05][720p][简日双语][招募翻译]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599861_04_SPYxFAMILY_04_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][04][1080p][繁日雙語][招募翻譯]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599860_04_SPYxFAMILY_04_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][04][720p][繁日雙語][招募翻譯]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599750_MingY_Spy_x_Family_05_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [05][1080p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `599714_SPY_FAMILY_04_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [04][简体双语][1080p]招募翻译后期")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599649_NC_Raws_SPY_FAMILY___05_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 05 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599490_LoliHouse_Spy_x_Family___04_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 04 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599478_04_SPY_FAMILY_04_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][04][1080P][简体][MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599477_04_SPY_FAMILY_04_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][04][1080P][繁體][MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599476_04_SPY_FAMILY_04_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][04][720P][简体][MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599475_04_SPY_FAMILY_04_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][04][720P][繁體][MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599433_SPY_FAMILY_04_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / SPY×FAMILY][04][简日双语][1080P][WEBrip][MP4]（急招翻译、校对、时轴）")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599431_SPY_FAMILY_03_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / SPY×FAMILY][03][简日双语][1080P][WEBrip][MP4]（急招翻译、校对、时轴）")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599417_4_SPY_FAMILY_04_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【4月新番】【間諜過家家 / 間諜家家酒 SPY×FAMILY】【04】【BIG5_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599416_4_SPY_FAMILY_04_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 / 间谍家家酒 SPY×FAMILY】【04】【GB_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599410_04_SPYxFAMILY_04_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][04][1080p][简日双语][招募翻译]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599409_04_SPYxFAMILY_04_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][04][720p][简日双语][招募翻译]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599382_Spy_x_Family_03_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[间谍过家家 / Spy x Family][03][x264 1080p][CHS]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599346_4_SPY_FAMILY_04_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][04][1080p][MP4][简中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599326_SPY_FAMILY_03_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [03][简体双语][1080p]招募翻译")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599313_Spy_x_Family_04_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][04][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599310_Spy_x_Family_04_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][04][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599304_404_04_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][04][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599286_LuoXinSpot_MingYSub_Spy_x_Family_04_WEBRip_2160p_HEVC_10bit_AAC`() {
    val r =
        parse("[LuoXinSpot & MingYSub] 间谍过家家 / Spy x Family [04][WEBRip 2160p HEVC 10bit AAC][简体内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599274_Spy_x_Family_04_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [04][1080p][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599272_Spy_x_Family_04_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [04][1080p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `599270_4_SPY_FAMILY_03v2_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 SPY×FAMILY】【03v2】【GB_MP4】【1280X720】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599269_4_SPY_FAMILY_03v2_BIG5_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 SPY×FAMILY】【03v2】【BIG5_MP4】【1280X720】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599173_Spy_x_Family_02_1080p_HEVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第02話][1080p_HEVC][簡繁內封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599172_Spy_x_Family_02_1080p_AVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第02話][1080p_AVC][繁體]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599171_Spy_x_Family_02_1080p_AVC`() {
    val r = parse("【千夏字幕组】【间谍过家家_Spy x Family】[第02话][1080p_AVC][简体]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599144_4_SPY_FAMILY_02_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【02】GB MP4_720P")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599143_4_SPY_FAMILY_01_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【02】GB MP4_1080P")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `599126_MingY_Spy_x_Family_04_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [04][1080p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `599086_LoliHouse_Spy_x_Family___03_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 03 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `599064_NC_Raws_SPY_FAMILY___04_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 04 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598934_04_SPY_FAMILY_03_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][03][1080P][简体][MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598933_04_SPY_FAMILY_03_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][03][1080P][繁體][MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598932_04_SPY_FAMILY_03_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][03][720P][简体][MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598931_04_SPY_FAMILY_03_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][03][720P][繁體][MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598899_Spy_x_Family_02_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[间谍过家家 / Spy x Family][02][x264 1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598794_404_03_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][03][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598787_4_SPY_FAMILY_03_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][03][1080p][MP4][简中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598755_Spy_x_Family_03_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][03][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598754_Spy_x_Family_03_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][03][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598728_04_SPYxFAMILY_03_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][03][1080p][繁日雙語][招募翻譯]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598727_04_SPYxFAMILY_03_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][03][720p][繁日雙語][招募翻譯]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598694_04_SPYxFAMILY_03_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][03][1080p][简日双语][招募翻译]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598693_04_SPYxFAMILY_03_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][03][720p][简日双语][招募翻译]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598593_Spy_x_Family_03_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [03][1080p][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `598591_Spy_x_Family_03_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [03][1080p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `598522_LuoXinSpot_MingYSub_Spy_x_Family_02_WEBRip_2160p_HEVC_10bit_AAC`() {
    val r =
        parse("[LuoXinSpot & MingYSub] 间谍过家家 / Spy x Family [03][WEBRip 2160p HEVC 10bit AAC][简体内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `598453_MingY_Spy_x_Family_03_1080p`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [03][1080p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `598388_NC_Raws_SPY_FAMILY___03_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 03 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598317_LuoXinSpot_MingYSub_Spy_x_Family_01_WEBRip_2160p_HEVC_10bit_AAC`() {
    val r =
        parse("[LuoXinSpot & MingYSub] 间谍过家家 / Spy x Family [01][WEBRip 2160p HEVC 10bit AAC][简体内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `598283_SPY_FAMILY_02_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [02][简体双语][1080p]招募翻译")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598156_SPY_FAMILY_02_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / SPY×FAMILY][02][简日双语][1080P][WEBrip][MP4]（急招翻译、校对、时轴）")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598154_SPY_FAMILY_01_1080P_WEBrip_MP4`() {
    val r = parse("[星空字幕组][间谍过家家 / SPY×FAMILY][01][简日双语][1080P][WEBrip][MP4]（急招翻译、校对、时轴）")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598151_4_SPY_FAMILY_02_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 SPY×FAMILY】【02】【GB_MP4】【1280X720】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598150_4_SPY_FAMILY_02_BIG5_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 SPY×FAMILY】【02】【BIG5_MP4】【1280X720】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598147_LoliHouse_Spy_x_Family___02_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 02 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `598102_04_SPY_FAMILY_02_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][02][1080P][简体][MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598101_04_SPY_FAMILY_02_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][02][1080P][繁體][MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598100_04_SPY_FAMILY_02_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][02][720P][简体][MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598099_04_SPY_FAMILY_02_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][02][720P][繁體][MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598009_NC_Raws_SPY_FAMILY___01_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 01 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598006_NC_Raws_SPY_FAMILY___02_B_Global_3840x2160_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 02 (B-Global 3840x2160 HEVC AAC MKV)")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598004_04_SPYxFAMILY_02_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][02][1080p][繁日雙語][招募翻譯]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598003_04_SPYxFAMILY_02_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][02][720p][繁日雙語][招募翻譯]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598002_04_SPYxFAMILY_02_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][02][1080p][简日双语][招募翻译]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `598001_4_SPY_FAMILY_02_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][02][1080p][MP4][简中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597966_404_02_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][02][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597898_04_SPYxFAMILY_02_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][02][720p][简日双语][招募翻译]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597894_Spy_x_Family_02_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [02][1080p][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `597891_Spy_x_Family_02_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [02][1080p][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `597824_Spy_x_Family_01_1080p_HEVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第01話][1080p_HEVC][簡繁內封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `597822_Spy_x_Family_01_1080p_AVC`() {
    val r = parse("【千夏字幕組】【間諜過家家_Spy x Family】[第01話][1080p_AVC][繁體]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597821_Spy_x_Family_01_1080p_AVC`() {
    val r = parse("【千夏字幕组】【间谍过家家_Spy x Family】[第01话][1080p_AVC][简体]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597820_Spy_x_Family_02_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][02][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597819_Spy_x_Family_02_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][02][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597723_SPY_FAMILY_01_1080p`() {
    val r = parse("[云光字幕组]间谍过家家 SPY×FAMILY [01][简体双语][1080p]招募翻译")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597717_LuoXinSpot_MingYSub_Spy_x_Family_02_WEBRip_2160p_AVC_8bit_AAC`() {
    val r =
        parse("[LuoXinSpot & MingYSub] 间谍过家家 / Spy x Family [02][WEBRip 2160p AVC 8bit AAC][简体内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("4K", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `597695_MingY_Spy_x_Family_02v2_1080p_CHS`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [02v2][1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597655_MingY_Spy_x_Family_02_1080p_CHS`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [02][1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597628_NC_Raws_SPY_FAMILY___02_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 02 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597610_404_01_1080P_WebRip_AVC_AAC_MP4`() {
    val r = parse("[酷漫404][间谍过家家][01][1080P][WebRip][简日双语][AVC AAC][MP4][字幕组招人内详]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597428_LoliHouse_Spy_x_Family___01_WebRip_1080p_HEVC_10bit_AAC`() {
    val r =
        parse("[动漫国字幕组&LoliHouse] Spy x Family / 间谍过家家 - 01 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `597409_Spy_x_Family_01_1080P_MKV_HEVC`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][01][简繁日语字幕][1080P][MKV HEVC]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597408_Spy_x_Family_01_720P_CHS_HEVC_MP4`() {
    val r = parse("[诸神字幕组][间谍过家家][Spy x Family][01][简日双语字幕][720P][CHS HEVC MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597393_04_SPY_FAMILY_01_1080P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][01][1080P][简体][MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597392_04_SPY_FAMILY_01_1080P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][01][1080P][繁體][MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597391_04_SPY_FAMILY_01_720P_MP4`() {
    val r = parse("【动漫国字幕组】★04月新番[SPY×FAMILY间谍家家酒 / 间谍过家家][01][720P][简体][MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597390_04_SPY_FAMILY_01_720P_MP4`() {
    val r = parse("【動漫國字幕組】★04月新番[SPY×FAMILY間諜家家酒 / 間諜過家家][01][720P][繁體][MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597355_4_SPY_FAMILY_01_1080p_MP4`() {
    val r = parse("[爱恋&漫猫字幕组][4月新番][间谍过家家][SPY × FAMILY][01][1080p][MP4][简中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597343_4_Spy_Family_01_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 Spy × Family】【01】【GB_MP4】【1280X720】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597342_4_Spy_Family_01_BIG5_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【4月新番】【间谍过家家 Spy × Family】【01】【BIG5_MP4】【1280X720】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597331_04_SPYxFAMILY_01_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01][1080p][繁日雙語][招募翻譯]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597330_04_SPYxFAMILY_01_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[間諜過家家/間諜家家酒/SPYxFAMILY][01][720p][繁日雙語][招募翻譯]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597248_4_SPY_FAMILY_01_GB_MP4_720P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【01】GB MP4_720P")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597247_4_SPY_FAMILY_01_GB_MP4_1080P`() {
    val r = parse("【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【01】GB MP4_1080P")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597238_04_SPYxFAMILY_01_1080p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01][1080p][简日双语][招募翻译]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597237_04_SPYxFAMILY_01_720p`() {
    val r = parse("【喵萌奶茶屋】★04月新番★[间谍过家家/间谍家家酒/SPYxFAMILY][01][720p][简日双语][招募翻译]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597229_MingY_Spy_x_Family_01_1080p_CHS`() {
    val r = parse("[MingY] 间谍过家家 / Spy x Family [01][1080p][CHS]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `597154_Spy_x_Family_01_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [01][1080p][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `597152_Spy_x_Family_01_1080p`() {
    val r = parse("[桜都字幕组] 间谍过家家 / Spy x Family [01][1080p][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `596979_NC_Raws_SPY_FAMILY___01_B_Global_1920x1080_HEVC_AAC_MKV`() {
    val r = parse("[NC-Raws] 间谍过家家 / SPY×FAMILY - 01 (B-Global 1920x1080 HEVC AAC MKV)")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }
}

// @formatter:on
