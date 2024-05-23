// @formatter:off
@file:Suppress(
  "FunctionName",
  "ClassName",
  "RedundantVisibilityModifier",
  "PackageDirectoryMismatch",
  "NonAsciiCharacters",
  "SpellCheckingInspection",
)

import me.him188.ani.datasources.api.title.PatternBasedTitleParserTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 原名: `無職轉生_到了異世界就拿出真本事_`
 * 数据源: `dmhy`
 *
 * 由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
 * 如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
 * 如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
 */
public class PatternTitleParserTest無職轉生_到了異世界就拿出真本事_ : PatternBasedTitleParserTestSuite() {
  @Test
  public fun `669857_4_Mushoku_Tensei_II_18_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][18][1080p][MP4][简中]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669856_4_Mushoku_Tensei_II_18_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][18][1080p][MP4][繁中]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669850_S2_Mushoku_Tensei_S2_18_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [18][1080p][简繁内封]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669849_S2_Mushoku_Tensei_S2_18_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [18][1080p@60FPS][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669848_S2_Mushoku_Tensei_S2_18_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [18][1080p][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669847_S2_Mushoku_Tensei_S2_18_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [18][1080p@60FPS][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669846_S2_Mushoku_Tensei_S2_18_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [18][1080p][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669757_Mushoku_Tensei___Season_2_18_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [18][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669756_Mushoku_Tensei___Season_2_18_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [18][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669755_Mushoku_Tensei___Season_2_18_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [18][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669263_4_Mushoku_Tensei_II_17_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][17][1080p][MP4][简中]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669262_4_Mushoku_Tensei_II_17_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][17][1080p][MP4][繁中]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669227_Mushoku_Tensei___Season_2_17_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [17][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669226_Mushoku_Tensei___Season_2_17_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [17][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669225_Mushoku_Tensei___Season_2_17_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [17][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669139_Mushoku_Tensei___Season_2_16_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [16][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669138_Mushoku_Tensei___Season_3_15_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [16][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669134_Mushoku_Tensei___Season_2_16_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [16][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `669132_S2_Mushoku_Tensei_S2_17_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [17][1080p][简繁内封]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669131_S2_Mushoku_Tensei_S2_17_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [17][1080p@60FPS][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669130_S2_Mushoku_Tensei_S2_17_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [17][1080p][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669129_S2_Mushoku_Tensei_S2_17_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [17][1080p@60FPS][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `669128_S2_Mushoku_Tensei_S2_17_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [17][1080p][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668753_4_Mushoku_Tensei_II_16_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][16][1080p][MP4][简中]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668752_4_Mushoku_Tensei_II_16_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][16][1080p][MP4][繁中]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668580_S2_Mushoku_Tensei_S2_16_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [16][1080p][简繁内封]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668579_S2_Mushoku_Tensei_S2_16_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [16][1080p@60FPS][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668578_S2_Mushoku_Tensei_S2_16_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [16][1080p@60FPS][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668577_S2_Mushoku_Tensei_S2_16_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [16][1080p][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668576_S2_Mushoku_Tensei_S2_16_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [16][1080p][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668175_4_Mushoku_Tensei_II_15_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][15][1080p][MP4][简中]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668174_4_Mushoku_Tensei_II_15_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][15][1080p][MP4][繁中]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668075_S2_Mushoku_Tensei_S2_15_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [15][1080p][简繁内封]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668074_S2_Mushoku_Tensei_S2_15_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [15][1080p@60FPS][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668073_S2_Mushoku_Tensei_S2_15_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [15][1080p@60FPS][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668072_S2_Mushoku_Tensei_S2_15_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [15][1080p][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668071_S2_Mushoku_Tensei_S2_15_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [15][1080p][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `668051_Mushoku_Tensei___Season_2_15_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [15][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `668050_Mushoku_Tensei___Season_2_15_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [15][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `668049_Mushoku_Tensei___Season_2_15_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [15][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `667600_S2_Mushoku_Tensei_S2_14_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [14][1080p][简繁内封]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667599_S2_Mushoku_Tensei_S2_14_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [14][1080p@60FPS][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667598_S2_Mushoku_Tensei_S2_14_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [14][1080p@60FPS][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667596_S2_Mushoku_Tensei_S2_14_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [14][1080p][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667594_S2_Mushoku_Tensei_S2_14_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [14][1080p][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667477_4_Mushoku_Tensei_II_14_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][14][1080p][MP4][简中]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667476_4_Mushoku_Tensei_II_14_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][14][1080p][MP4][繁中]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `667448_Mushoku_Tensei___Season_2_14_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [14][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `667442_Mushoku_Tensei___Season_2_14_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [14][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `667441_Mushoku_Tensei___Season_2_14_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [14][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `666958_S2_Mushoku_Tensei_S2_13_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [13][1080p][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666957_S2_Mushoku_Tensei_S2_13_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [13][1080p@60FPS][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666956_S2_Mushoku_Tensei_S2_13_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [13][1080p@60FPS][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666954_S2_Mushoku_Tensei_S2_13_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [13][1080p][简繁内封]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666952_S2_Mushoku_Tensei_S2_13_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [13][1080p][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666610_4_Mushoku_Tensei_II_13_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][4月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][13][1080p][MP4][简中]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666609_4_Mushoku_Tensei_II_13_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][4月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][13][1080p][MP4][繁中]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `666604_Mushoku_Tensei___Season_2_13_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [13][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `666603_Mushoku_Tensei___Season_2_13_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [13][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `666602_Mushoku_Tensei___Season_2_13_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [13][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
  }

  @Test
  public fun `653838_Mushoku_Tensei_II_00_12Fin_WEBRIP_1080p_AVC_AAC_MP4_2023_7`() {
    val r =
        parse("[爱恋字幕社&漫猫字幕社] 无职转生Ⅱ ～到了异世界就拿出真本事～/Mushoku Tensei II (00-12Fin WEBRIP 1080p AVC AAC MP4 2023年7月 简中)")
    assertEquals("00..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `653837_Mushoku_Tensei_II_00_12Fin_WEBRIP_1080p_AVC_AAC_MP4_2023_7`() {
    val r =
        parse("[愛戀字幕社&漫貓字幕社] 無職轉生Ⅱ ～到了異世界就拿出真本事～/Mushoku Tensei II (00-12Fin WEBRIP 1080p AVC AAC MP4 2023年7月 繁中)")
    assertEquals("00..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652097_S2_Mushoku_Tensei_S2_12_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [12][1080p][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652096_S2_Mushoku_Tensei_S2_12_1080p_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [12][1080p@60FPS][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652095_S2_Mushoku_Tensei_S2_12_1080p_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [12][1080p@60FPS][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652094_S2_Mushoku_Tensei_S2_12_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [12][1080p][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652093_S2_Mushoku_Tensei_S2_12_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [12][1080p][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652077_Mushoku_Tensei___Season_2_12_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [12][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652076_Mushoku_Tensei___Season_2_12_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [12][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652075_Mushoku_Tensei___Season_2_12_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [12][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652045_Mushoku_Tensei_S2_08_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][08][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `652044_Mushoku_Tensei_S2_08_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][08][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651837_7_Mushoku_Tensei_II_11_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][11][1080p][MP4][简中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651836_7_Mushoku_Tensei_II_11_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][11][1080p][MP4][繁中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651835_7_Mushoku_Tensei_II_10_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][10][1080p][MP4][简中]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651834_7_Mushoku_Tensei_II_10_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][10][1080p][MP4][繁中]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651701_Mushoku_Tensei_S2_07_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][07][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651699_Mushoku_Tensei_S2_07_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][07][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651553_S2_Mushoku_Tensei_S2_11_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [11][1080p][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651552_S2_Mushoku_Tensei_S2_11_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [11][1080P@60FPS][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651551_S2_Mushoku_Tensei_S2_11_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [11][1080P@60FPS][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651549_S2_Mushoku_Tensei_S2_11_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [11][1080P][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651548_S2_Mushoku_Tensei_S2_11_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [11][1080P][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651519_Mushoku_Tensei___Season_2_11_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [11][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651518_Mushoku_Tensei___Season_2_11_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [11][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651516_Mushoku_Tensei___Season_2_11_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [11][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651007_Mushoku_Tensei___Season_2_10_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [10][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651006_Mushoku_Tensei___Season_2_10_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [10][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `651005_Mushoku_Tensei___Season_2_10_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [10][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650947_S2_Mushoku_Tensei_S2_10_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [10][1080p][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650946_S2_Mushoku_Tensei_S2_10_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [10][1080P@60FPS][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650945_S2_Mushoku_Tensei_S2_10_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [10][1080P@60FPS][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650944_S2_Mushoku_Tensei_S2_10_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [10][1080P][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650943_S2_Mushoku_Tensei_S2_10_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [10][1080P][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650775_7_Mushoku_Tensei_II_09_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][09][1080p][MP4][简中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650774_7_Mushoku_Tensei_II_09_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][09][1080p][MP4][繁中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650451_S2_Mushoku_Tensei_S2_09_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [09][1080p][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650450_S2_Mushoku_Tensei_S2_09_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [09][1080P@60FPS][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650449_S2_Mushoku_Tensei_S2_09_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [09][1080P@60FPS][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650448_S2_Mushoku_Tensei_S2_09_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [09][1080P][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650447_S2_Mushoku_Tensei_S2_09_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [09][1080P][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650310_Mushoku_Tensei___Season_2_09_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [09][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650309_Mushoku_Tensei___Season_2_09_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [09][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `650308_Mushoku_Tensei___Season_2_09_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [09][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649892_7_Mushoku_Tensei_II_08_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][08][1080p][MP4][简中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649891_7_Mushoku_Tensei_II_08_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][08][1080p][MP4][繁中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649763_S2_Mushoku_Tensei_S2_08_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [08][1080p][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649762_S2_Mushoku_Tensei_S2_08_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [08][1080P@60FPS][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649761_S2_Mushoku_Tensei_S2_08_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [08][1080P@60FPS][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649760_S2_Mushoku_Tensei_S2_08_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [08][1080P][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649759_S2_Mushoku_Tensei_S2_08_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [08][1080P][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649717_Mushoku_Tensei___Season_2_08_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [08][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649716_Mushoku_Tensei___Season_2_08_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [08][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649715_Mushoku_Tensei___Season_2_08_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [08][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649363_7_Mushoku_Tensei_II_07_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][07][1080p][MP4][简中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649362_7_Mushoku_Tensei_II_07_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][07][1080p][MP4][繁中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649234_Mushoku_Tensei_S2_06_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][06][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649233_Mushoku_Tensei_S2_06_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][06][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649162_S2_Mushoku_Tensei_S2_07_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [07][1080p][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649161_S2_Mushoku_Tensei_S2_07_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [07][1080P@60FPS][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649160_S2_Mushoku_Tensei_S2_07_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [07][1080P@60FPS][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649159_S2_Mushoku_Tensei_S2_07_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [07][1080P][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649158_S2_Mushoku_Tensei_S2_07_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [07][1080P][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649085_Mushoku_Tensei___Season_2_07_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [07][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649084_Mushoku_Tensei___Season_2_07_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [07][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `649083_Mushoku_Tensei___Season_2_07_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [07][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648630_7_Mushoku_Tensei_II_06_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][06][1080p][MP4][简中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648629_7_Mushoku_Tensei_II_06_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][06][1080p][MP4][繁中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648522_S2_Mushoku_Tensei_S2_06_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [06][1080p][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648521_S2_Mushoku_Tensei_S2_06_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [06][1080P@60FPS][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648520_S2_Mushoku_Tensei_S2_06_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [06][1080P@60FPS][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648519_S2_Mushoku_Tensei_S2_06_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [06][1080P][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648518_S2_Mushoku_Tensei_S2_06_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [06][1080P][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648457_Mushoku_Tensei___Season_2_06_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [06][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648456_Mushoku_Tensei___Season_2_06_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [06][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648455_Mushoku_Tensei___Season_2_06_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [06][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648329_Mushoku_Tensei_S2_05_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][05][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `648328_Mushoku_Tensei_S2_05_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][05][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647999_7_Mushoku_Tensei_II_05_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][05][1080p][MP4][简中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647998_7_Mushoku_Tensei_II_05_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][05][1080p][MP4][繁中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647908_7_Mushoku_Tensei_II_04_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][04][1080p][MP4][简中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647907_7_Mushoku_Tensei_II_04_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][04][1080p][MP4][繁中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647902_S2_Mushoku_Tensei_S2_05_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [05][1080p][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647901_S2_Mushoku_Tensei_S2_05_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [05][1080P@60FPS][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647900_S2_Mushoku_Tensei_S2_05_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [05][1080P@60FPS][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647899_S2_Mushoku_Tensei_S2_05_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [05][1080P][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647898_S2_Mushoku_Tensei_S2_05_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [05][1080P][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647872_Mushoku_Tensei___Season_2_05_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [05][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647871_Mushoku_Tensei___Season_2_05_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [05][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647870_Mushoku_Tensei___Season_2_05_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [05][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647405_S2_Mushoku_Tensei_S2_04_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [04][1080p][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647404_S2_Mushoku_Tensei_S2_04_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [04][1080P@60FPS][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647403_S2_Mushoku_Tensei_S2_04_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [04][1080P@60FPS][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647402_S2_Mushoku_Tensei_S2_04_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [04][1080P][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647401_S2_Mushoku_Tensei_S2_04_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [04][1080P][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647312_Mushoku_Tensei_S2_04_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][04][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647311_Mushoku_Tensei_S2_04_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][04][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647278_Mushoku_Tensei___Season_2_04_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [04][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647277_Mushoku_Tensei___Season_2_04_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [04][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647276_Mushoku_Tensei___Season_2_04_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [04][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647012_7_Mushoku_Tensei_II_03_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][03][1080p][MP4][简中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `647011_7_Mushoku_Tensei_II_03_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][03][1080p][MP4][繁中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646825_Mushoku_Tensei_S2_03_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][03][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646824_Mushoku_Tensei_S2_03_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][03][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646795_S2_Mushoku_Tensei_S2_03_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [03][1080p][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646794_S2_Mushoku_Tensei_S2_03_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [03][1080P@60FPS][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646793_S2_Mushoku_Tensei_S2_03_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [03][1080P@60FPS][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646792_S2_Mushoku_Tensei_S2_03_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [03][1080P][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646788_S2_Mushoku_Tensei_S2_03_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [03][1080P][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646647_Mushoku_Tensei___Season_2_03_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [03][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646646_Mushoku_Tensei___Season_2_03_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [03][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646645_Mushoku_Tensei___Season_3_02_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [03][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646168_Mushoku_Tensei_S2_02_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][02][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646167_Mushoku_Tensei_S2_02_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][02][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646115_S2_Mushoku_Tensei_S2_02_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [02][1080p][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646114_S2_Mushoku_Tensei_S2_02_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [02][1080P@60FPS][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646113_S2_Mushoku_Tensei_S2_02_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [02][1080P@60FPS][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646112_S2_Mushoku_Tensei_S2_02_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [02][1080P][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `646111_S2_Mushoku_Tensei_S2_02_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [02][1080P][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645984_7_Mushoku_Tensei_II_02_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][02][1080p][MP4][简中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645983_7_Mushoku_Tensei_II_02_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][02][1080p][MP4][繁中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645923_Mushoku_Tensei___Season_2_02_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [02][WebRip][1080p][HEVC_AAC][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645922_Mushoku_Tensei___Season_2_02_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [02][WebRip][1080p][HEVC_AAC][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645921_Mushoku_Tensei___Season_2_02_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [02][WebRip][1080p][HEVC_AAC][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645365_S2_Mushoku_Tensei_S2_01_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [01][1080p][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645364_S2_Mushoku_Tensei_S2_01_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [01][1080P@60FPS][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645363_S2_Mushoku_Tensei_S2_01_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [01][1080P@60FPS][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645362_S2_Mushoku_Tensei_S2_01_1080P`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [01][1080P][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645361_S2_Mushoku_Tensei_S2_01_1080P`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [01][1080P][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645342_Mushoku_Tensei_S2_01_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][01][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645337_Mushoku_Tensei_S2_01_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][01][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645334_7_Mushoku_Tensei_II_01_1080p_MP4`() {
    val r = parse("[爱恋字幕社&漫猫字幕社][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][01][1080p][MP4][简中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645333_7_Mushoku_Tensei_II_01_1080p_MP4`() {
    val r = parse("[愛戀字幕社&漫貓字幕社][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][01][1080p][MP4][繁中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645185_Mushoku_Tensei___Season_2_01_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [01][WebRip][1080p][HEVC_AAC][繁體內嵌][無修正]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645184_Mushoku_Tensei___Season_2_01_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [01][WebRip][1080p][HEVC_AAC][简体内嵌][无修正]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `645169_Mushoku_Tensei___Season_2_01_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [01][WebRip][1080p][HEVC_AAC][简繁内封][无修正]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644823_7_Mushoku_Tensei_II_00v2_1080p_MP4`() {
    val r =
        parse("[爱恋字幕社&猫恋汉化组][7月新番][无职转生Ⅱ ～到了异世界就拿出真本事～][Mushoku Tensei II][00v2][1080p][MP4][简中]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644822_7_Mushoku_Tensei_II_00v2_1080p_MP4`() {
    val r =
        parse("[愛戀字幕社&貓戀漢化組][7月新番][無職轉生Ⅱ ～到了異世界就拿出真本事～][Mushoku Tensei II][00v2][1080p][MP4][繁中]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644772_S2_Mushoku_Tensei_S2_00_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [00][1080p][简繁内封]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644771_S2_Mushoku_Tensei_S2_00_1080P_60FPS`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [00][1080P@60FPS][繁體內嵌]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644769_S2_Mushoku_Tensei_S2_00_1080P_60FPS`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [00][1080P@60FPS][简体内嵌]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644768_S2_Mushoku_Tensei_S2_00_1080p`() {
    val r = parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ S2 / Mushoku Tensei S2 [00][1080p][繁體內嵌]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644767_S2_Mushoku_Tensei_S2_00_1080p`() {
    val r = parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ S2 / Mushoku Tensei S2 [00][1080p][简体内嵌]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644662_Mushoku_Tensei_S2_00_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕組][無職轉生Ⅱ～到了異世界就拿出真本事～ / Mushoku Tensei S2][00][繁日雙語][1080P][WEBrip][MP4]（急招校對、後期）")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644661_Mushoku_Tensei_S2_00_1080P_WEBrip_MP4`() {
    val r =
        parse("[星空字幕组][无职转生Ⅱ～到了异世界就拿出真本事～ / Mushoku Tensei S2][00][简日双语][1080P][WEBrip][MP4]（急招校对、后期）")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644595_Mushoku_Tensei___Season_2_00_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [00][WebRip][1080p][HEVC_AAC][简繁内封][无修正]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644594_Mushoku_Tensei___Season_2_00_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 無職轉生Ⅱ ～到了異世界就拿出真本事～/ Mushoku Tensei - Season 2 [00][WebRip][1080p][HEVC_AAC][繁體內嵌][無修正]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `644593_Mushoku_Tensei___Season_2_00_WebRip_1080p_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 无职转生Ⅱ ～到了异世界就拿出真本事～/ Mushoku Tensei - Season 2 [00][WebRip][1080p][HEVC_AAC][简体内嵌][无修正]")
    assertEquals("00..00", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public
      fun `602366_VCB_Studio_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_bit_1080p_HEVC_BDRip_S1_Fin`() {
    val r =
        parse("[喵萌奶茶屋&VCB-Studio] Mushoku Tensei: Isekai Ittara Honki Dasu / 无职转生～到了异世界就拿出真本事～ 10-bit 1080p HEVC BDRip [S1 Fin]")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `595150_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_24_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [24][1080p][简繁内封]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `595148_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_24_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [24][1080p@60FPS][繁體內嵌]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `595147_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_24_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [24][1080p][繁體內嵌]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `595146_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_24_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [24][1080p@60FPS][简体内嵌]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `595145_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_24_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [24][1080p][简体内嵌]")
    assertEquals("24..24", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `589141_c_c_10_12_23_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][12-23][合集][BIG5][1080P][MP4]")
    assertEquals("12..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588508_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_23_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [23][1080p][简繁内封]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588506_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_23_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [23][1080p@60FPS][繁體內嵌]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588505_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_23_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [23][1080p@60FPS][简体内嵌]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588504_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_23_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [23][1080p][繁體內嵌]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588503_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_23_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [23][1080p][简体内嵌]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `588491_c_c_10_23_BIG5_1080P_MP4_END`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][23][BIG5][1080P][MP4][END]")
    assertEquals("23..23", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587952_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_22_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [22][1080p][简繁内封]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587951_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_22_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [22][1080p@60FPS][繁體內嵌]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587950_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_22_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [22][1080p@60FPS][简体内嵌]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587949_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_22_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [22][1080p][繁體內嵌]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587948_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_22_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [22][1080p][简体内嵌]")
    assertEquals("22..22", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587927_c_c_10_16_22_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][16-22][BIG5][1080P][MP4]")
    assertEquals("16..22", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587465_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_21_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [21][1080p][简繁内封]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587464_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_21_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [21][1080p@60FPS][繁體內嵌]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587463_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_21_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [21][1080p@60FPS][简体内嵌]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587462_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_21_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [21][1080p][繁體內嵌]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `587461_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_21_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [21][1080p][简体内嵌]")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `586263_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_19_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [19][1080p][简繁内封]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `586262_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_19_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [19][1080p@60FPS][繁體內嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `586261_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_19_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [19][1080p@60FPS][简体内嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `586260_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_19_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [19][1080p][繁體內嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `586259_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_19_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [19][1080p][简体内嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `585476_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_18_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [18][1080p][简繁内封]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `585475_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_18_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [18][1080p@60FPS][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `585474_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_18_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [18][1080p@60FPS][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `585473_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_18_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [18][1080p][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `585472_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_18_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [18][1080p][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584925_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_17v2_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [17v2][1080p@60FPS][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584923_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_17_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [17][1080p][简繁内封]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584921_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_17_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [17][1080p@60FPS][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584920_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_17_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [17][1080p][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584919_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_17_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [17][1080p][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584174_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_16_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [16][1080p][简繁内封]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584173_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_16_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [16][1080p@60FPS][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584172_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_16_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [16][1080p@60FPS][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584169_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_16_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [16][1080p][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `584168_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_16_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [16][1080p][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583560_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_15_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [15][1080p][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583559_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_15_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [15][1080p][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583556_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_15_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [15][1080p][简繁内封]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583555_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_15_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [15][1080p@60FPS][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583554_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_15_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [15][1080p@60FPS][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `583269_c_c_10_15_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][15][BIG5][1080P][MP4]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582773_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_14_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [14][1080p][简繁内封]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582772_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_14_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [14][1080p@60FPS][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582771_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_14_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [14][1080p@60FPS][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582769_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_14_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [14][1080p][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582768_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_14_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [14][1080p][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582552_c_c_10_14_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][14][BIG5][1080P][MP4]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582076_c_c_10_13_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][13][BIG5][1080P][MP4]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582051_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_13v2_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [13v2][1080p][简繁内封]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582036_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_13_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [13][1080p][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582035_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_13_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [13][1080p][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582033_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_13_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [13][1080p@60FPS][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `582031_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_13_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [13][1080p@60FPS][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581220_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_12_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [12][1080p][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581181_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_12_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [12][1080p@60FPS][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581180_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_12_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [12][1080p@60FPS][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581179_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_12_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [12][1080p][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581178_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_12_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [12][1080p][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `581059_c_c_10_12_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][10月新番][無職轉生～到了異世界就拿出真本事～][12][BIG5][1080P][MP4]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `577124_Mushoku_Tensei_01_11_BDRIP_1920x1080_HEVC_YUV420P10_FLAC`() {
    val r =
        parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][01-11][BDRIP 1920x1080 HEVC-YUV420P10 FLAC][简繁外挂]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `572626__Raws_11th_anniversary_BDRip_1080P_x265_10bit__FLAC_ALL`() {
    val r =
        parse("【肥羊-Raws 11th anniversary】[无职转生～到了异世界就拿出真本事～][無職転生 〜異世界行ったら本気だす〜][BDRip][1080P_x265(10bit)-FLAC][ALL]")
    assertEquals("null", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `566682_Mushoku_Tensei_01_11_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][01-11][x264 1080p][CHT]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `566681_Mushoku_Tensei_01_11_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][01-11][x264 1080p][CHS]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564842_Mushoku_Tensei_11_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][11][x264 1080p][CHT]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564841_Mushoku_Tensei_11_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][11][x264 1080p][CHS]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564512_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][1080p@60FPS][繁體內嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564511_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][1080p@60FPS][简体内嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564510_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][1080p][繁體內嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564509_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][1080p][简体内嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564508_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][720p][繁體內嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `564507_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][720p][简体内嵌]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `564506_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_11_Fin_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01-11 Fin][1080p][简繁内封]")
    assertEquals("01..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564158_Mushoku_Tensei_10_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][10][x264 1080p][CHT]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `564157_Mushoku_Tensei_10_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][10][x264 1080p][CHS]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563676_c_c_1_11_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][11][BIG5][1080P][MP4]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563632_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][11][1080p][MP4][GB][简中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563631_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][11][1080p][MP4][BIG5][繁中]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563438_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][1080p@60FPS][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563431_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][1080p][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563430_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][720p][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `563429_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][1080p][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563428_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][1080p@60FPS][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563427_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][1080p][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563423_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_11_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [11][720p][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `563336_NC_Raws_Mushoku_Tensei___11_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 11 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563334_NC_Raws_Mushoku_Tensei___11_WEB_DL_1080p_AVC_AAC_CHS_CHT_TH_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 无职转生 ～到了异世界就拿出真本事～ / Mushoku Tensei - 11 [WEB-DL][1080p][AVC AAC][CHS_CHT_TH_SRT][MKV]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563048_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][10][1080p][MP4][GB][简中]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `563047_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][10][1080p][MP4][BIG5][繁中]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562845_Mushoku_Tensei_09_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][09][x264 1080p][CHT]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562844_Mushoku_Tensei_09_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][09][x264 1080p][CHS]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562831_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][1080p@60FPS][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562830_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][1080p@60FPS][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562827_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][1080p][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562826_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][1080p][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562825_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][720p][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `562782_Mushoku_Tensei_08_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][08][x264 1080p][CHT]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562781_Mushoku_Tensei_08_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][08][x264 1080p][CHS]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562768_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][720p][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `562760_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_10_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [10][1080p][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562725_c_c_1_10_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][10][BIG5][1080P][MP4]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562694_NC_Raws_Mushoku_Tensei___10_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 10 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562693_NC_Raws_Mushoku_Tensei___10_WEB_DL_1080p_AVC_AAC_CHS_CHT_TH_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 无职转生 ～到了异世界就拿出真本事～ / Mushoku Tensei - 10 [WEB-DL][1080p][AVC AAC][CHS_CHT_TH_SRT][MKV]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562653_c_c_1_09_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][09][BIG5][1080P][MP4]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562652_c_c_1_08_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][08][BIG5][1080P][MP4]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562326_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][09][1080p][MP4][GB][简中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562325_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][09][1080p][MP4][BIG5][繁中]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562170_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][1080p@60FPS][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562168_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][1080p@60FPS][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562161_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][1080p][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562160_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][1080p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562157_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][720p][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `562154_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][720p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `562144_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_09_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [09][1080p][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562056_NC_Raws_Mushoku_Tensei___09_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 09 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `562055_NC_Raws_Mushoku_Tensei___09_WEB_DL_1080p_AVC_AAC_CHS_CHT_TH_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 无职转生 ～到了异世界就拿出真本事～ / Mushoku Tensei - 09 [WEB-DL][1080p][AVC AAC][CHS_CHT_TH_SRT][MKV]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561772_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][08][1080p][MP4][GB][简中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561771_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][08][1080p][MP4][BIG5][繁中]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561704_NC_Raws_Mushoku_Tensei___08_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 无职转生 ～到了异世界就拿出真本事～ / Mushoku Tensei - 08 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561508_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][1080p@60FPS][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561491_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][1080p@60FPS][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561485_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][1080p][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561484_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][1080p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561476_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][720p][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `561475_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][720p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `561472_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_08_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [08][1080p][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561350_NC_Raws_Mushoku_Tensei___08_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 08 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561239_Mushoku_Tensei_07_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][07][x264 1080p][CHT]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `561237_Mushoku_Tensei_07_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][07][x264 1080p][CHS]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560944_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][07][1080p][MP4][GB][简中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560943_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][07][1080p][MP4][BIG5][繁中]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560768_c_c_1_07_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][07][BIG5][1080P][MP4]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560720_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][1080p@60FPS][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560711_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][1080p@60FPS][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560710_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][1080p][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560698_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][1080p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560697_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][720p][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `560695_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][720p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `560690_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_07_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [07][1080p][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560618_NC_Raws_Mushoku_Tensei___07_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 07 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560210_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][06][1080p][MP4][GB][简中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560209_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][06][1080p][MP4][BIG5][繁中]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560180_Mushoku_Tensei_06_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][06][x264 1080p][CHT]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560179_Mushoku_Tensei_06_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][06][x264 1080p][CHS]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560026_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][1080p@60FPS][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560024_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][1080p@60FPS][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560019_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][1080p][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560018_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][1080p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `560017_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][720p][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `560015_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][720p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `560013_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_06_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [06][1080p][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559914_c_c_1_06_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][06][BIG5][1080P][MP4]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559913_NC_Raws_Mushoku_Tensei___06_WEB_DL_1080p_AVC_AAC_CHS_CHT_SRT_MKV`() {
    val r =
        parse("[NC-Raws] 無職轉生～到了異世界就拿出真本事～（僅限港澳台地區） / Mushoku Tensei - 06 [WEB-DL][1080p][AVC AAC][CHS_CHT_SRT][MKV]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559518_Mushoku_Tensei_05_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][05][x264 1080p][CHT]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559513_Mushoku_Tensei_05_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][05][x264 1080p][CHS]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559451_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][05][1080p][MP4][GB][简中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559450_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][05][1080p][MP4][BIG5][繁中]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559402_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][1080p@60FPS][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559394_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][1080p@60FPS][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559391_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][1080p][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559390_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][1080p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559358_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][720p][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `559357_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][720p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `559349_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_05_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [05][1080p][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559346_Mushoku_Tensei_04_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][04][x264 1080p][CHS]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559345_Mushoku_Tensei_04_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][04][x264 1080p][CHT]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559282_c_c_1_05_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][05][BIG5][1080P][MP4]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559077_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][04][1080p][MP4][GB][简中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `559076_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][04][1080p][MP4][BIG5][繁中]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558799_Mushoku_Tensei_03_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][03][x264 1080p][CHT]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558798_Mushoku_Tensei_03_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][03][x264 1080p][CHS]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558797_Mushoku_Tensei_02_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][02][x264 1080p][CHT]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558796_Mushoku_Tensei_02_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][02][x264 1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558741_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][1080p@60FPS][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558731_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][1080p@60FPS][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558730_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][1080p][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558729_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][1080p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558728_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][720p][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `558725_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][720p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `558724_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_04_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [04][1080p][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558624_c_c_1_04_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][04][BIG5][1080P][MP4]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558421_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][03][1080p][MP4][GB][简中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558420_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][03][1080p][MP4][BIG5][繁中]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558396_Mushoku_Tensei_01_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[無職轉生～到了異世界就拿出真本事～_Mushoku Tensei][01][x264 1080p][CHT]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558394_Mushoku_Tensei_01_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[无职转生～到了异世界就拿出真本事～_Mushoku Tensei][01][x264 1080p][CHS]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558071_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][1080p@60FPS][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558069_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][1080p@60FPS][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558067_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][1080p][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558045_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][1080p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558044_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][720p][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `558043_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][720p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `558041_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_03_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [03][1080p][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `558007_c_c_1_03_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][03][BIG5][1080P][MP4]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557642_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p_MP4_GB`() {
    val r =
        parse("[神枫字幕组] [1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][02][1080p][MP4][GB][简体中文]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557640_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_720p_MP4_GB`() {
    val r =
        parse("[神枫字幕组] [1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][02][720p][MP4][GB][简体中文]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `557592_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p_MP4_GB`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][02][1080p][MP4][GB][简中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557591_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p_MP4_BIG5`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][02][1080p][MP4][BIG5][繁中]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557384_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][1080p][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557383_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][1080p@60FPS][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557382_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][1080p@60FPS][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557381_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][1080p][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557380_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][1080p][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557379_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][720p][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `557378_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_02_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [02][720p][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `557306_c_c_1_02_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][02][BIG5][1080P][MP4]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557073_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p_AVC`() {
    val r =
        parse("[爱恋&漫猫字幕组][1月新番][无职转生～到了异世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][01][1080p][AVC][简中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `557071_1_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p_AVC`() {
    val r =
        parse("[愛戀&漫貓字幕組][1月新番][無職轉生～到了異世界就拿出真本事～][Mushoku Tensei Isekai Ittara Honki Dasu][01][1080p][AVC][繁中]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556703_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][1080p][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556702_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p_60FPS`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][1080p@60FPS][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556701_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p_60FPS`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][1080p@60FPS][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556700_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][1080p][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556699_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_1080p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][1080p][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }

  @Test
  public fun `556698_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_720p`() {
    val r =
        parse("[桜都字幕組] 無職轉生～到了異世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][720p][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `556697_Mushoku_Tensei_Isekai_Ittara_Honki_Dasu_01_720p`() {
    val r =
        parse("[桜都字幕组] 无职转生～到了异世界就拿出真本事～ / Mushoku Tensei Isekai Ittara Honki Dasu [01][720p][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
  }

  @Test
  public fun `556694_c_c_1_01_BIG5_1080P_MP4`() {
    val r = parse("[c.c動漫][1月新番][無職轉生～到了異世界就拿出真本事～][01][BIG5][1080P][MP4]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
  }
}

// @formatter:on
