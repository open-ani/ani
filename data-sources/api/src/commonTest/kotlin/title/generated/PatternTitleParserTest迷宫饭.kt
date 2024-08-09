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
 * 原名: `迷宫饭`
 * 数据源: `dmhy`
 *
 * 由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
 * 如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
 * 如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
 */
public class PatternTitleParserTest迷宫饭 : PatternBasedTitleParserTestSuite() {
  @Test
  public fun `670480_1_Dungeon_Meshi_21_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【21】【BIG5_MP4】【1920X1080】")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670479_1_Dungeon_Meshi_21_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【21】【GB_MP4】【1920X1080】")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670475_1_Dungeon_Meshi_21_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【21】【BIG5_MP4】【1280X720】")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670474_1_Dungeon_Meshi_21_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【21】【GB_MP4】【1280X720】")
    assertEquals("21..21", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670438_Dungeon_Meshi_20_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [20][简体双语][1080p]招募翻译")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670199_Dungeon_Meshi_20_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [20][1080p][简繁内封]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `670198_Dungeon_Meshi_20_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [20][1080p][繁體內嵌]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `670197_Dungeon_Meshi_20_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [20][1080p][简体内嵌]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `670092_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___20_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 20 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `670077_01_Dungeon_Meshi_Delicious_in_Dungeon_20_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][20][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `670076_01_Dungeon_Meshi_Delicious_in_Dungeon_20_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][20][1080p][简日双语][招募翻译时轴]")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669908_1_Dungeon_Meshi_20_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【20】【BIG5_MP4】【1920X1080】")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669907_1_Dungeon_Meshi_20_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【20】【GB_MP4】【1920X1080】")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669906_1_Dungeon_Meshi_20_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【20】【BIG5_MP4】【1280X720】")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669905_1_Dungeon_Meshi_20_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【20】【GB_MP4】【1280X720】")
    assertEquals("20..20", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669845_Dungeon_Meshi_19_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [19][简体双语][1080p]招募翻译")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669844_Dungeon_Meshi_18_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [18][简体双语][1080p]招募翻译")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `669364_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___19v2_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 19v2 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `669296_01_Dungeon_Meshi_Delicious_in_Dungeon_19_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][19][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669295_01_Dungeon_Meshi_Delicious_in_Dungeon_19_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][19][1080p][简日双语][招募翻译时轴]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669290_Dungeon_Meshi_19_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [19][1080p][简繁内封]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `669289_Dungeon_Meshi_19_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [19][1080p][繁體內嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `669288_Dungeon_Meshi_19_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [19][1080p][简体内嵌]")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `669202_1_Dungeon_Meshi_19_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【19】【BIG5_MP4】【1920X1080】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669201_1_Dungeon_Meshi_19_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【19】【GB_MP4】【1920X1080】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669200_1_Dungeon_Meshi_19_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【19】【BIG5_MP4】【1280X720】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `669199_1_Dungeon_Meshi_19_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【19】【GB_MP4】【1280X720】")
    assertEquals("19..19", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668901_Dungeon_Meshi_18_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [18][1080p][简繁内封]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668900_Dungeon_Meshi_18_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [18][1080p][繁體內嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668899_Dungeon_Meshi_18_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [18][1080p][简体内嵌]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668763_Dungeon_Meshi_17_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][17][x264 1080p][CHT]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668762_Dungeon_Meshi_17_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][17][x264 1080p][CHS]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `668761_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___18_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 18 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668722_01_Dungeon_Meshi_Delicious_in_Dungeon_18_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][18][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668721_01_Dungeon_Meshi_Delicious_in_Dungeon_18_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][18][1080p][简日双语][招募翻译时轴]")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668697_Dungeon_Meshi_16_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [17][简体双语][1080p]招募翻译")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668638_1_Dungeon_Meshi_18_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【18】【BIG5_MP4】【1920X1080】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668637_1_Dungeon_Meshi_18_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【18】【GB_MP4】【1920X1080】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668636_1_Dungeon_Meshi_18_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【18】【BIG5_MP4】【1280X720】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668635_1_Dungeon_Meshi_18_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【18】【GB_MP4】【1280X720】")
    assertEquals("18..18", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668567_Dungeon_Meshi_17v2_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [17v2][1080p][简繁内封]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668566_Dungeon_Meshi_17v2_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [17v2][1080p][繁體內嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668565_Dungeon_Meshi_17v2_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [17v2][1080p][简体内嵌]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `668293_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___17_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 17 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668279_01_Dungeon_Meshi_Delicious_in_Dungeon_17_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][17][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668278_01_Dungeon_Meshi_Delicious_in_Dungeon_17_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][17][1080p][简日双语][招募翻译时轴]")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668196_1_Dungeon_Meshi_17_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【17】【BIG5_MP4】【1920X1080】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668195_1_Dungeon_Meshi_17_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【17】【GB_MP4】【1920X1080】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668191_1_Dungeon_Meshi_17_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【17】【BIG5_MP4】【1280X720】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668190_1_Dungeon_Meshi_17_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【17】【GB_MP4】【1280X720】")
    assertEquals("17..17", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668183_Dungeon_Meshi_16_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [16][简体双语][1080p]招募翻译")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668060_DBD_Raws_Dungeon_Meshi_01_06TV_BOX1_1080P_BDRip_HEVC_10bit_FLAC_MKV`() {
    val r =
        parse("[DBD-Raws][迷宫饭/Dungeon Meshi/ダンジョン飯][01-06TV+特典映像][BOX1][1080P][BDRip][HEVC-10bit][FLAC][MKV]")
    assertEquals("01..06+特典映像", r.episodeRange.toString())
    assertEquals("", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668029_Dungeon_Meshi_16_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][16][x264 1080p][CHT]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `668028_Dungeon_Meshi_16_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][16][x264 1080p][CHS]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667774_Dungeon_Meshi_16_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [16][1080p][简繁内封]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `667772_Dungeon_Meshi_16_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [16][1080p][繁體內嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667771_Dungeon_Meshi_16_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [16][1080p][简体内嵌]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `667728_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___16_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 16 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `667704_01_Dungeon_Meshi_Delicious_in_Dungeon_16_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][16][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667703_01_Dungeon_Meshi_Delicious_in_Dungeon_16_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][16][1080p][简日双语][招募翻译时轴]")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667604_1_Dungeon_Meshi_16_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【16】【GB_MP4】【1920X1080】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667602_1_Dungeon_Meshi_16_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【16】【BIG5_MP4】【1920X1080】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667597_1_Dungeon_Meshi_16_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【16】【BIG5_MP4】【1280X720】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667595_1_Dungeon_Meshi_16_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【16】【GB_MP4】【1280X720】")
    assertEquals("16..16", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667535_Dungeon_Meshi_15_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [15][简体双语][1080p]招募翻译")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667474_Dungeon_Meshi_15_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][15][x264 1080p][CHT]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667473_Dungeon_Meshi_15_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][15][x264 1080p][CHS]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667445_Dungeon_Meshi_14_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [14][简体双语][1080p]招募翻译")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `667367_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___15_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 15 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `667249_Dungeon_Meshi_15_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [15][1080p][简繁内封]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `667250_Dungeon_Meshi_15_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [15][1080p][繁體內嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667248_Dungeon_Meshi_15_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [15][1080p][简体内嵌]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667126_1_Dungeon_Meshi_06_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][06][1080P][MP4][繁日雙語]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667125_1_Dungeon_Meshi_06_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][06][1080P][MP4][简日双语]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667028_01_Dungeon_Meshi_Delicious_in_Dungeon_15_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][15][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `667027_01_Dungeon_Meshi_Delicious_in_Dungeon_15_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][15][1080p][简日双语][招募翻译时轴]")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666938_1_Dungeon_Meshi_15_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【15】【BIG5_MP4】【1920X1080】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666937_1_Dungeon_Meshi_15_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【15】【GB_MP4】【1920X1080】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666936_1_Dungeon_Meshi_15_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【15】【BIG5_MP4】【1280X720】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666935_1_Dungeon_Meshi_15_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【15】【GB_MP4】【1280X720】")
    assertEquals("15..15", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666874_Dungeon_Meshi_14_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][14][x264 1080p][CHT]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666873_Dungeon_Meshi_14_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][14][x264 1080p][CHS]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666871_Dungeon_Meshi_13_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][13][x264 1080p][CHT]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666870_Dungeon_Meshi_13_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][13][x264 1080p][CHS]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666365_Dungeon_Meshi_14_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [14][1080p][简繁内封]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `666364_Dungeon_Meshi_14_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [14][1080p][繁體內嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666363_Dungeon_Meshi_14_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [14][1080p][简体内嵌]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666360_Dungeon_Meshi_13_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [13][简体双语][1080p]招募翻译")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `666283_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___14_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 14 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `666243_01_Dungeon_Meshi_Delicious_in_Dungeon_14_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][14][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666242_01_Dungeon_Meshi_Delicious_in_Dungeon_14_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][14][1080p][简日双语][招募翻译时轴]")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666136_1_Dungeon_Meshi_14_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【14】【BIG5_MP4】【1920X1080】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666135_1_Dungeon_Meshi_14_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【14】【GB_MP4】【1920X1080】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666134_1_Dungeon_Meshi_14_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【14】【BIG5_MP4】【1280X720】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `666133_1_Dungeon_Meshi_14_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【14】【GB_MP4】【1280X720】")
    assertEquals("14..14", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665797_Dungeon_Meshi_12_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [12][简体双语][1080p]招募翻译")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665785_Dungeon_Meshi_12_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][12][x264 1080p][CHT]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665784_Dungeon_Meshi_12_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][12][x264 1080p][CHS]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665781_Dungeon_Meshi_13_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [13][1080p][简繁内封]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `665780_Dungeon_Meshi_13_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [13][1080p][繁體內嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `665779_Dungeon_Meshi_13_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [13][1080p][简体内嵌]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `665756_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___13_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 13 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `665727_01_Dungeon_Meshi_Delicious_in_Dungeon_13_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][13][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665726_01_Dungeon_Meshi_Delicious_in_Dungeon_13_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][13][1080p][简日双语][招募翻译时轴]")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665686_1_Dungeon_Meshi_13_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【13】【BIG5_MP4】【1920X1080】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665684_1_Dungeon_Meshi_13_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【13】【GB_MP4】【1920X1080】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665683_1_Dungeon_Meshi_13_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【13】【BIG5_MP4】【1280X720】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665682_1_Dungeon_Meshi_13_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【13】【GB_MP4】【1280X720】")
    assertEquals("13..13", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665621_Dungeon_Meshi_11_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [11][简体双语][1080p]招募翻译")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665571_1_Dungeon_Meshi_05_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][05][1080P][MP4][繁日雙語]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665570_1_Dungeon_Meshi_05_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][05][1080P][MP4][简日双语]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665463_Dungeon_Meshi_12_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [12][1080p][简繁内封]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `665461_Dungeon_Meshi_12_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [12][1080p][繁體內嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `665460_Dungeon_Meshi_12_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [12][1080p][简体内嵌]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `665346_1_Dungeon_Meshi_04_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][04][1080P][MP4][繁日雙語]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665345_1_Dungeon_Meshi_04_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][04][1080P][MP4][简日双语]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `665318_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___12_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 12 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `665311_01_Dungeon_Meshi_Delicious_in_Dungeon_12_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][12][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665310_01_Dungeon_Meshi_Delicious_in_Dungeon_12_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][12][1080p][简日双语][招募翻译时轴]")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665239_1_Dungeon_Meshi_12_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【12】【BIG5_MP4】【1920X1080】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665238_1_Dungeon_Meshi_12_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【12】【GB_MP4】【1280X720】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665237_1_Dungeon_Meshi_12_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【12】【GB_MP4】【1920X1080】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665236_1_Dungeon_Meshi_12_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【12】【BIG5_MP4】【1280X720】")
    assertEquals("12..12", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665136_Dungeon_Meshi_11_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][11][x264 1080p][CHT]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `665135_Dungeon_Meshi_11_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][11][x264 1080p][CHS]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664998_Dungeon_Meshi_11_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [11][1080p][简繁内封]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `664997_Dungeon_Meshi_11_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [11][1080p][繁體內嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `664996_Dungeon_Meshi_11_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [11][1080p][简体内嵌]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `664887_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___11_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 11 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `664886_01_Dungeon_Meshi_Delicious_in_Dungeon_11_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][11][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664885_01_Dungeon_Meshi_Delicious_in_Dungeon_11_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][11][1080p][简日双语][招募翻译时轴]")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664850_1_Dungeon_Meshi_11_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【11】【BIG5_MP4】【1920X1080】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664849_1_Dungeon_Meshi_11_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【11】【GB_MP4】【1920X1080】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664848_1_Dungeon_Meshi_11_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【11】【BIG5_MP4】【1280X720】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664847_1_Dungeon_Meshi_11_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【11】【GB_MP4】【1280X720】")
    assertEquals("11..11", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664840_Dungeon_Meshi_10_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [10][简体双语][1080p]招募翻译")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `664782_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___10_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 10 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `664728_Dungeon_Meshi_10_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [10][1080p][简繁内封]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `664727_Dungeon_Meshi_10_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [10][1080p][繁體內嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `664726_Dungeon_Meshi_10_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [10][1080p][简体内嵌]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `664723_1_Dungeon_Meshi_03_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][03][1080P][MP4][繁日雙語]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664722_1_Dungeon_Meshi_03_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][03][1080P][MP4][简日双语]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664505_Dungeon_Meshi_10_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][10][x264 1080p][CHT]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664504_Dungeon_Meshi_10_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][10][x264 1080p][CHS]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664469_01_Dungeon_Meshi_Delicious_in_Dungeon_10_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][10][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664468_01_Dungeon_Meshi_Delicious_in_Dungeon_10_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][10][1080p][简日双语][招募翻译时轴]")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664427_1_Dungeon_Meshi_10_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【10】【BIG5_MP4】【1920X1080】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664426_1_Dungeon_Meshi_10_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【10】【GB_MP4】【1920X1080】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664424_1_Dungeon_Meshi_10_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【10】【BIG5_MP4】【1280X720】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664423_1_Dungeon_Meshi_10_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【10】【GB_MP4】【1280X720】")
    assertEquals("10..10", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664299_Dungeon_Meshi_09_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [09][简体双语][1080p]招募翻译")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664298_Dungeon_Meshi_08_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [08][简体双语][1080p]招募翻译")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664289_1_Dungeon_Meshi_02_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][02][1080P][MP4][繁日雙語]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664288_1_Dungeon_Meshi_02_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][02][1080P][MP4][简日双语]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `664165_Dungeon_Meshi_07_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [07][简体双语][1080p]招募翻译")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `664146_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___09_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 09 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `663992_Dungeon_Meshi_09_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [09][1080p][简繁内封]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `663991_Dungeon_Meshi_09_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [09][1080p][繁體內嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `663990_Dungeon_Meshi_09_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [09][1080p][简体内嵌]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `663949_Dungeon_Meshi_09_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][09][x264 1080p][CHT]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663948_Dungeon_Meshi_09_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][09][x264 1080p][CHS]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663878_01_Dungeon_Meshi_Delicious_in_Dungeon_09_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][09][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663877_01_Dungeon_Meshi_Delicious_in_Dungeon_09_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][09][1080p][简日双语][招募翻译时轴]")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663857_1_Dungeon_Meshi_09_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【09】【BIG5_MP4】【1920X1080】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663856_1_Dungeon_Meshi_09_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【09】【GB_MP4】【1920X1080】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663855_1_Dungeon_Meshi_09_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【09】【BIG5_MP4】【1280X720】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663854_1_Dungeon_Meshi_09_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【09】【GB_MP4】【1280X720】")
    assertEquals("09..09", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663851_Dungeon_Meshi_06_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [06][简体双语][1080p]招募翻译")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663602_Dungeon_Meshi_08_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][08][x264 1080p][CHT]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663601_Dungeon_Meshi_08_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][08][x264 1080p][CHS]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663572_Dungeon_Meshi_08_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [08][1080p][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `663571_Dungeon_Meshi_08_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [08][1080p][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `663570_Dungeon_Meshi_08_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [08][1080p][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `663455_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___08_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 08 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `663375_01_Dungeon_Meshi_Delicious_in_Dungeon_08_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][08][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663374_01_Dungeon_Meshi_Delicious_in_Dungeon_08_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][08][1080p][简日双语][招募翻译时轴]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663350_1_Dungeon_Meshi_08_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【08】【BIG5_MP4】【1920X1080】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663349_1_Dungeon_Meshi_08_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【08】【GB_MP4】【1920X1080】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663348_1_Dungeon_Meshi_08_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【08】【BIG5_MP4】【1280X720】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663347_1_Dungeon_Meshi_08_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【08】【GB_MP4】【1280X720】")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663133_Dungeon_Meshi_07_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][07][x264 1080p][CHS]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663132_Dungeon_Meshi_07_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][07][x264 1080p][CHT]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `663106_Dungeon_Meshi_07_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [07][1080p][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `663105_Dungeon_Meshi_07_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [07][1080p][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `663104_Dungeon_Meshi_07_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [07][1080p][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662978_1_Dungeon_Meshi_01_1080P_MP4`() {
    val r = parse("[愛戀字幕社][1月新番][迷宮飯][Dungeon Meshi][01][1080P][MP4][繁日雙語]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662977_1_Dungeon_Meshi_01_1080P_MP4`() {
    val r = parse("[爱恋字幕社][1月新番][迷宫饭][Dungeon Meshi][01][1080P][MP4][简日双语]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `662912_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___07_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 07 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662907_01_Dungeon_Meshi_Delicious_in_Dungeon_07_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][07][1080p][繁日雙語][招募翻譯時軸]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662906_01_Dungeon_Meshi_Delicious_in_Dungeon_07_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][07][1080p][简日双语][招募翻译时轴]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662862_1_Dungeon_Meshi_07_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【07】【BIG5_MP4】【1920X1080】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662861_1_Dungeon_Meshi_07_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【07】【GB_MP4】【1920X1080】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662859_1_Dungeon_Meshi_07_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【07】【BIG5_MP4】【1280X720】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662856_1_Dungeon_Meshi_07_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【07】【GB_MP4】【1280X720】")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662800_Dungeon_Meshi_06_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [06][1080p][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662799_Dungeon_Meshi_06_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [06][1080p][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662798_Dungeon_Meshi_06_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [06][1080p][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662690_Dungeon_Meshi_06_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][06][x264 1080p][CHT]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662689_Dungeon_Meshi_06_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][06][x264 1080p][CHS]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `662517_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___06_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 06 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662499_01_Dungeon_Meshi_Delicious_in_Dungeon_06_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][06][1080p][繁日雙語][招募翻譯]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662498_01_Dungeon_Meshi_Delicious_in_Dungeon_06_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][06][1080p][简日双语][招募翻译]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662374_1_Dungeon_Meshi_06_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【06】【BIG5_MP4】【1920X1080】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662373_1_Dungeon_Meshi_06_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【06】【GB_MP4】【1920X1080】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662371_1_Dungeon_Meshi_06_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【06】【BIG5_MP4】【1280X720】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662370_1_Dungeon_Meshi_06_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【06】【GB_MP4】【1280X720】")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662317_Dungeon_Meshi_05_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [05][简体双语][1080p]招募翻译")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662096_Dungeon_Meshi_05_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [05][1080p][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `662095_Dungeon_Meshi_05_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [05][1080p][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662094_Dungeon_Meshi_05_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [05][1080p][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `662083_Dungeon_Meshi_05_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][05][x264 1080p][CHT]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `662082_Dungeon_Meshi_05_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][05][x264 1080p][CHS]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `661962_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___05_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 05 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `661959_01_Dungeon_Meshi_Delicious_in_Dungeon_05_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][05][1080p][繁日雙語][招募翻譯]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661958_01_Dungeon_Meshi_Delicious_in_Dungeon_05_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][05][1080p][简日双语][招募翻译]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661907_1_Dungeon_Meshi_05_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【05】【BIG5_MP4】【1920X1080】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661906_1_Dungeon_Meshi_05_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【05】【BIG5_MP4】【1280X720】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661904_1_Dungeon_Meshi_05_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【05】【GB_MP4】【1920X1080】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661903_1_Dungeon_Meshi_05_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【05】【GB_MP4】【1280X720】")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661841_Dungeon_Meshi_04_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [04][简体双语][1080p]招募翻译")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661672_Dungeon_Meshi_04_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [04][1080p][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `661671_Dungeon_Meshi_04_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [04][1080p][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `661670_Dungeon_Meshi_04_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [04][1080p][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `661648_Dungeon_Meshi_04_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][04][x264 1080p][CHS]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661647_Dungeon_Meshi_04_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][04][x264 1080p][CHT]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `661638_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___04v2_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 04v2 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public
      fun `661607_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___04_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 04 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `661470_01_Dungeon_Meshi_Delicious_in_Dungeon_04_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][04][1080p][繁日雙語][招募翻譯]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661469_01_Dungeon_Meshi_Delicious_in_Dungeon_04_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][04][1080p][简日双语][招募翻译]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661438_1_Dungeon_Meshi_04_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【04】【BIG5_MP4】【1920X1080】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661436_1_Dungeon_Meshi_04_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【04】【GB_MP4】【1920X1080】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661434_1_Dungeon_Meshi_04_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【04】【BIG5_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661433_1_Dungeon_Meshi_04_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【04】【GB_MP4】【1280X720】")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661307_Dungeon_Meshi_03_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [03][简体双语][1080p]招募翻译")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661184_Dungeon_Meshi_03_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][03][x264 1080p][CHT]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661183_Dungeon_Meshi_03_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][03][x264 1080p][CHS]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661182_Dungeon_Meshi_02_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][02][x264 1080p][CHT]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661181_Dungeon_Meshi_02_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][02][x264 1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661164_Dungeon_Meshi_03_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][03][x264 1080p][CHT]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661163_Dungeon_Meshi_03_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][03][x264 1080p][CHS]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661162_Dungeon_Meshi_02_x264_1080p_CHT`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][02][x264 1080p][CHT]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661161_Dungeon_Meshi_02_x264_1080p_CHS`() {
    val r = parse("【悠哈璃羽字幕社】[迷宫饭/Dungeon Meshi][02][x264 1080p][CHS]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `661103_Dungeon_Meshi_03_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [03][1080p][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `661102_Dungeon_Meshi_03_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [03][1080p][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `661101_Dungeon_Meshi_03_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [03][1080p][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `660921_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___03_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 03 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660889_01_Dungeon_Meshi_Delicious_in_Dungeon_03_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][03][1080p][繁日雙語][招募翻譯]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660888_01_Dungeon_Meshi_Delicious_in_Dungeon_03_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][03][1080p][简日双语][招募翻译]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660877_1_Dungeon_Meshi_03_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【03】【BIG5_MP4】【1920X1080】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660876_1_Dungeon_Meshi_03_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【03】【GB_MP4】【1920X1080】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660875_1_Dungeon_Meshi_03_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【03】【BIG5_MP4】【1280X720】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660874_1_Dungeon_Meshi_03_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【03】【GB_MP4】【1280X720】")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660861_Dungeon_Meshi_02_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [02][简体双语][1080p]招募翻译")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660760_Dungeon_Meshi_02_1080p_2024_1`() {
    val r = parse("[摆烂字幕组＆猎户译制部] 迷宫饭 / Dungeon Meshi [02] [1080p] [繁日内嵌] [2024年1月番]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `660759_Dungeon_Meshi_02_1080p_2024_1`() {
    val r = parse("[摆烂字幕组＆猎户译制部] 迷宫饭 / Dungeon Meshi [02] [1080p] [简日内嵌] [2024年1月番]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `660594_Delicious_in_Dungeon_03_MKV_1080P_NETFLIX`() {
    val r = parse("[天月搬運組][迷宮飯 Delicious in Dungeon][02][日語中字][MKV][1080P][NETFLIX][高畫質版]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660593_Delicious_in_Dungeon_02_MKV_1080P_NETFLIX`() {
    val r = parse("[天月搬運組][迷宮飯 Delicious in Dungeon][02][日語中字][MKV][1080P][NETFLIX][高壓版]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660539_Dungeon_Meshi_02_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [02][1080p][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660538_Dungeon_Meshi_02_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [02][1080p][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `660537_Dungeon_Meshi_02_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [02][1080p][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `660374_Dungeon_Meshi_01_1080p`() {
    val r = parse("[云光字幕组] 迷宫饭 Dungeon Meshi [01][简体双语][1080p]招募翻译")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public
      fun `660359_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___02_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 02 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `660340_01_Dungeon_Meshi_Delicious_in_Dungeon_02_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][02][1080p][繁日雙語][招募翻譯]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660339_01_Dungeon_Meshi_Delicious_in_Dungeon_02_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][02][1080p][简日双语][招募翻译]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660326_1_Dungeon_Meshi_02_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【02】【BIG5_MP4】【1920X1080】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660325_1_Dungeon_Meshi_02_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【02】【GB_MP4】【1920X1080】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660324_1_Dungeon_Meshi_02_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【02】【BIG5_MP4】【1280X720】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660323_1_Dungeon_Meshi_02_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【02】【GB_MP4】【1280X720】")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660128_Dungeon_Meshi_01_x264_1080p_CHS`() {
    val r = parse("[悠哈璃羽字幕社][迷宫饭/Dungeon Meshi][01][x264 1080p][CHS]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660127_Dungeon_Meshi_01_x264_1080p_CHT`() {
    val r = parse("[悠哈璃羽字幕社][迷宫饭/Dungeon Meshi][01][x264 1080p][CHT]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `660105_Dungeon_Meshi_01_1080p_2024_1`() {
    val r = parse("[摆烂字幕组＆猎户译制部] 迷宫饭 / Dungeon Meshi [01] [1080p] [简日内嵌] [2024年1月番]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public
      fun `660004_LoliHouse_Dungeon_Meshi_Delicious_in_Dungeon___01_WebRip_1080p_HEVC_10bit_AAC_EAC3`() {
    val r =
        parse("[喵萌奶茶屋&LoliHouse] 迷宫饭 / Dungeon Meshi / Delicious in Dungeon - 01 [WebRip 1080p HEVC-10bit AAC EAC3][简繁日内封字幕]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659971_Dungeon_Meshi_01_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [01][1080p][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `659970_Dungeon_Meshi_01_1080p`() {
    val r = parse("[桜都字幕組] 迷宮飯 / Dungeon Meshi [01][1080p][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659969_Dungeon_Meshi_01_1080p`() {
    val r = parse("[桜都字幕组] 迷宫饭 / Dungeon Meshi [01][1080p][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `659933_1_Dungeon_Meshi_01v2_BIG5_MP4_1920X1080`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【01v2】【BIG5_MP4】【1920X1080】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659932_1_Dungeon_Meshi_01v2_GB_MP4_1920X1080`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【01v2】【GB_MP4】【1920X1080】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659931_1_Dungeon_Meshi_01v2_BIG5_MP4_1280X720`() {
    val r = parse("【幻櫻字幕組】【1月新番】【迷宮飯 Dungeon Meshi】【01v2】【BIG5_MP4】【1280X720】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659930_1_Dungeon_Meshi_01v2_GB_MP4_1280X720`() {
    val r = parse("【幻樱字幕组】【1月新番】【迷宫饭 Dungeon Meshi】【01v2】【GB_MP4】【1280X720】")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("720P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659916_01_Dungeon_Meshi_Delicious_in_Dungeon_01_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宮飯 / Dungeon Meshi / Delicious in Dungeon][01][1080p][繁日雙語][招募翻譯]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659915_01_Dungeon_Meshi_Delicious_in_Dungeon_01_1080p`() {
    val r =
        parse("【喵萌奶茶屋】★01月新番★[迷宫饭 / Dungeon Meshi / Delicious in Dungeon][01][1080p][简日双语][招募翻译]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659896_Delicious_in_Dungeon_01_MKV_1080P_NETFLIX_Multi_VeryG`() {
    val r = parse("[天月搬運組][迷宮飯 Delicious in Dungeon][01][日語中字][MKV][1080P][NETFLIX][高畫質版]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }

  @Test
  public fun `659895_Delicious_in_Dungeon_01_MKV_1080P_NETFLIX_VeryG`() {
    val r = parse("[天月搬運組][迷宮飯 Delicious in Dungeon][01][日語中字][MKV][1080P][NETFLIX][高壓版]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, JPN", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("1080P", r.resolution.toString())
    assertEquals(null, r.subtitleKind)
  }
}

// @formatter:on
