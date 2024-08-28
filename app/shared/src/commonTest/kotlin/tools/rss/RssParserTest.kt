package me.him188.ani.app.tools.rss

import androidx.constraintlayout.compose.platform.annotation.Language
import kotlinx.serialization.json.Json
import me.him188.ani.utils.xml.Xml
import kotlin.test.Test
import kotlin.test.assertEquals

class RssParserTest {
    private val json = Json {
        prettyPrint = true
    }

    private val parser = RssParser

    @Test
    fun breadio() {
        test(
            """
                {
                    "title": "樱trick",
                    "description": "Anime Garden 是動漫花園資源網的第三方镜像站, 動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论.",
                    "link": "https://garden.breadio.wiki/resources?page=1&pageSize=100&search=%5B%22%E6%A8%B1trick%22%5D",
                    "items": [
                        {
                            "title": "[愛戀&漫猫字幕社]櫻Trick Sakura Trick 01-12 avc_flac mkv 繁體內嵌合集(急招時軸)",
                            "pubDate": "2023-11-18T04:54:02",
                            "link": "https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42",
                            "guid": "https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42",
                            "enclosure": {
                                "url": "magnet:?xt=urn:btih:d22868eee2dae4214476ac865e0b6ec533e09e57",
                                "type": "application/x-bittorrent"
                            }
                        }
                    ]
                }
            """.trimIndent(),
            """
                <rss version="2.0">
                <channel>
                <title>樱trick</title>
                <description>Anime Garden 是動漫花園資源網的第三方镜像站, 動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论.</description>
                <link>https://garden.breadio.wiki/resources?page=1&pageSize=100&search=%5B%22%E6%A8%B1trick%22%5D</link>
                <item>
                <title>[愛戀&漫猫字幕社]櫻Trick Sakura Trick 01-12 avc_flac mkv 繁體內嵌合集(急招時軸)</title>
                <link>https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</link>
                <guid isPermaLink="true">https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</guid>
                <pubDate>Sat, 18 Nov 2023 04:54:02 GMT</pubDate>
                <enclosure url="magnet:?xt=urn:btih:d22868eee2dae4214476ac865e0b6ec533e09e57" length="0" type="application/x-bittorrent"/>
                </item>
            """.trimIndent(),
        )
    }

    @Test
    fun acgrip() {
        test(
            """
                {
                    "title": "ACG.RIP",
                    "description": "ACG.RIP has super cow power",
                    "link": "https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3",
                    "ttl": 1800,
                    "items": [
                        {
                            "title": "[Up to 21°C] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 04 (CR 1920x1080 AVC AAC MKV)",
                            "description": "[h3]Katsute Mahou Shoujo to Aku wa Tekitai shitei...",
                            "pubDate": "2024-07-30T07:47:41",
                            "link": "https://acg.rip/t/309582",
                            "guid": "https://acg.rip/t/309582",
                            "enclosure": {
                                "url": "https://acg.rip/t/309582.torrent",
                                "type": "application/x-bittorrent"
                            }
                        },
                        {
                            "title": "[Up to 21°C] 魔法少女与邪恶曾经敌对 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 04 (B-Global 1920x1080 HEVC AAC MKV)",
                            "description": "[h3]Katsute Mahou Shoujo to Aku wa ...",
                            "pubDate": "2024-07-30T07:46:29",
                            "link": "https://acg.rip/t/309581",
                            "guid": "https://acg.rip/t/309581",
                            "enclosure": {
                                "url": "https://acg.rip/t/309581.torrent",
                                "type": "application/x-bittorrent"
                            }
                        },
                        {
                            "title": "[Up to 21°C] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 03 (ABEMA 1920x1080 AVC AAC MP4)",
                            "description": "[h3]Katsute Mahou Shoujo to Aku wa Tekitai shiteita....",
                            "pubDate": "2024-07-25T11:01:13",
                            "link": "https://acg.rip/t/309197",
                            "guid": "https://acg.rip/t/309197",
                            "enclosure": {
                                "url": "https://acg.rip/t/309197.torrent",
                                "type": "application/x-bittorrent"
                            }
                        }
                    ]
                }
            """.trimIndent(),
            """
                <rss version="2.0">
                <channel>
                <title>ACG.RIP</title>
                <description>ACG.RIP has super cow power</description>
                <link>https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3</link>
                <ttl>1800</ttl>
                <item>
                <title>[Up to 21°C] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 04 (CR 1920x1080 AVC AAC MKV)</title>
                <description><img src="https://img1.ak.crunchyroll.com/i/spire4-tmb/f89a681e17239531f343fcf57d59096f1722348663_full.jpg" alt="" /><br /> <br /> [h3]<strong><em><em>Katsute Mahou Shoujo to Aku wa Tekitai shitei...</description>
                <pubDate>Tue, 30 Jul 2024 07:47:41 -0700</pubDate>
                <link>https://acg.rip/t/309582</link>
                <guid>https://acg.rip/t/309582</guid>
                <enclosure url="https://acg.rip/t/309582.torrent" type="application/x-bittorrent"/>
                </item>
                <item>
                <title>[Up to 21°C] 魔法少女与邪恶曾经敌对 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 04 (B-Global 1920x1080 HEVC AAC MKV)</title>
                <description><img src="https://rr1---bg.ouo.si/bfs/intl/management/74b3ee9d28820106cfdc9f4e25005249e8ce1aa6.png@960w_540h_100Q_1c.jpg" alt="" /><br /> <br /> [h3]<strong><em><em>Katsute Mahou Shoujo to Aku wa ...</description>
                <pubDate>Tue, 30 Jul 2024 07:46:29 -0700</pubDate>
                <link>https://acg.rip/t/309581</link>
                <guid>https://acg.rip/t/309581</guid>
                <enclosure url="https://acg.rip/t/309581.torrent" type="application/x-bittorrent"/>
                </item>
                <item>
                <title>[Up to 21°C] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 03 (ABEMA 1920x1080 AVC AAC MP4)</title>
                <description><img src="https://image.p-c2-x.abema-tv.com/image/programs/168-64_s1_p3/thumb001.png" alt="" /><br /> <br /> [h3]<strong><em><em>Katsute Mahou Shoujo to Aku wa Tekitai shiteita.</em></em></strong>...</description>
                <pubDate>Thu, 25 Jul 2024 11:01:13 -0700</pubDate>
                <link>https://acg.rip/t/309197</link>
                <guid>https://acg.rip/t/309197</guid>
                <enclosure url="https://acg.rip/t/309197.torrent" type="application/x-bittorrent"/>
                </item>
            """.trimIndent(),
        )
    }

    @Test
    fun dmhy() {
        test(
            """
                {
                    "title": "動漫花園資源網",
                    "description": "動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论.",
                    "link": "http://share.dmhy.org",
                    "items": [
                        {
                            "title": "[北宇治字幕组] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. [07v2][WebRip][HEVC_AAC][繁日內嵌]",
                            "description": "<p><img src=\"https://p.inari.site/kitauji/pigeon/maho.webp\" style=\"width:1400px;height:1988px\" /></p><br /><p> <strong>为了您的播放能正确显示字幕效果，我们推荐您观看内封源使用 <a href=\"https://github.com/hooke007/MPV_lazy/releases\" target=\"_blank\" rel=\"external nofollow\">MPV 播放器</a>，或者挂载 <a href=\"https://github.com/Masaiki/xy-VSFilter/releases\" target=\"_blank\" rel=\"external nofollow\">XySubFilter with libass字幕滤镜</a>。</strong><br /> </p><p> <strong>本组字幕作品基于 <a href=\"https://creativecommons.org/licenses/by-nc-nd/4.0/\" rel=\"external nofollow\">CC BY-NC-ND 4.0 协议</a> 进行共享。如有需求，可前往北宇治字幕组的<a href=\"https://github.com/Kitauji-Sub\" rel=\"external nofollow\">字幕仓库</a>下载往期制作的外挂字幕。</strong><br /> </p><hr /><h3><strong>当前招募</strong></h3><p> <strong> <strong>翻译</strong> | 日语参考等级n2以上或能够听懂生肉内容并保证正确率，有经验者优先<br /> <strong>校对</strong> | 日语参考等级n1，善于发现翻译的错误<br /> <strong>美工</strong> | 熟练使用PS/AI等软件，能进行图片设计<br /> <strong>时轴</strong> | 熟练使用Aeg打轴/校轴并设置合适的样式、字体或屏幕字<br /> <strong>分流</strong> | 电脑/NAS能长时间开机，有足够的空间和上行带宽且至少具备ipv6公网，为组内资源分流<br /> <strong>有意者加群：招募群（<a href=\"https://jq.qq.com/?k=NWmvNiOQ\" rel=\"external nofollow\">232487445</a>）交流群（<a href=\"https://jq.qq.com/?k=dGxiOqRR\" rel=\"external nofollow\">713548082</a>）TG群（<a href=\"https://t.me/KitaUji\" rel=\"external nofollow\">@KitaUji</a>）</strong><br /> <img src=\"https://p.inari.site/guest/23-02/06/63e0ffef840de.webp\" alt=\"img\" /> </strong> </p>",
                            "pubDate": "2024-08-22T21:15:43",
                            "link": "http://share.dmhy.org/topics/view/677458_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html",
                            "guid": "http://share.dmhy.org/topics/view/677458_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html",
                            "enclosure": {
                                "url": "magnet:?xt=urn:btih:VKQ3KW4C2NT4WYK4QYW2IJKUBP3BPKML&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%2Fannounce&tr=http%3A%2F%2Fopen.nyaatorrents.info%3A6544%2Fannounce&tr=http%3A%2F%2Ft2.popgo.org%3A7456%2Fannonce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=http%3A%2F%2Fopentracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Ftracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftracker.kamigami.org%3A2710%2Fannounce&tr=http%3A%2F%2Fanidex.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=https%3A%2F%2Fopentracker.i2p.rocks%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.org%3A443%2Fannounce&tr=https%3A%2F%2Ftr.burnabyhighstar.com%3A443%2Fannounce&tr=http%3A%2F%2Ftr.nyacat.pw%2Fannounce&tr=https%3A%2F%2Ftr.nyacat.pw%2Fannounce",
                                "length": 1,
                                "type": "application/x-bittorrent"
                            }
                        },
                        {
                            "title": "[北宇治字幕组] 魔法少女与恶曾是敌人。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. [07v2][WebRip][HEVC_AAC][简日内嵌]",
                            "description": "<p><img src=\"https://p.inari.site/kitauji/pigeon/maho.webp\" style=\"width:1400px;height:1988px\" /></p><br /><p> <strong>为了您的播放能正确显示字幕效果，我们推荐您观看内封源使用 <a href=\"https://github.com/hooke007/MPV_lazy/releases\" target=\"_blank\" rel=\"external nofollow\">MPV 播放器</a>，或者挂载 <a href=\"https://github.com/Masaiki/xy-VSFilter/releases\" target=\"_blank\" rel=\"external nofollow\">XySubFilter with libass字幕滤镜</a>。</strong><br /> </p><p> <strong>本组字幕作品基于 <a href=\"https://creativecommons.org/licenses/by-nc-nd/4.0/\" rel=\"external nofollow\">CC BY-NC-ND 4.0 协议</a> 进行共享。如有需求，可前往北宇治字幕组的<a href=\"https://github.com/Kitauji-Sub\" rel=\"external nofollow\">字幕仓库</a>下载往期制作的外挂字幕。</strong><br /> </p><hr /><h3><strong>当前招募</strong></h3><p> <strong> <strong>翻译</strong> | 日语参考等级n2以上或能够听懂生肉内容并保证正确率，有经验者优先<br /> <strong>校对</strong> | 日语参考等级n1，善于发现翻译的错误<br /> <strong>美工</strong> | 熟练使用PS/AI等软件，能进行图片设计<br /> <strong>时轴</strong> | 熟练使用Aeg打轴/校轴并设置合适的样式、字体或屏幕字<br /> <strong>分流</strong> | 电脑/NAS能长时间开机，有足够的空间和上行带宽且至少具备ipv6公网，为组内资源分流<br /> <strong>有意者加群：招募群（<a href=\"https://jq.qq.com/?k=NWmvNiOQ\" rel=\"external nofollow\">232487445</a>）交流群（<a href=\"https://jq.qq.com/?k=dGxiOqRR\" rel=\"external nofollow\">713548082</a>）TG群（<a href=\"https://t.me/KitaUji\" rel=\"external nofollow\">@KitaUji</a>）</strong><br /> <img src=\"https://p.inari.site/guest/23-02/06/63e0ffef840de.webp\" alt=\"img\" /> </strong> </p>",
                            "pubDate": "2024-08-22T21:15:30",
                            "link": "http://share.dmhy.org/topics/view/677457_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html",
                            "guid": "http://share.dmhy.org/topics/view/677457_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html",
                            "enclosure": {
                                "url": "magnet:?xt=urn:btih:LMKXDZSQPSU3GDVNCIDKYLTAESZOQRAN&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%2Fannounce&tr=http%3A%2F%2Fopen.nyaatorrents.info%3A6544%2Fannounce&tr=http%3A%2F%2Ft2.popgo.org%3A7456%2Fannonce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=http%3A%2F%2Fopentracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Ftracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftracker.kamigami.org%3A2710%2Fannounce&tr=http%3A%2F%2Fanidex.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=https%3A%2F%2Fopentracker.i2p.rocks%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.org%3A443%2Fannounce&tr=https%3A%2F%2Ftr.burnabyhighstar.com%3A443%2Fannounce&tr=http%3A%2F%2Ftr.nyacat.pw%2Fannounce&tr=https%3A%2F%2Ftr.nyacat.pw%2Fannounce",
                                "length": 1,
                                "type": "application/x-bittorrent"
                            }
                        },
                        {
                            "title": "[LoliHouse] 魔法少女与邪恶曾经敌对。 / 曾经、魔法少女和邪恶相互为敌。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita - 07 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]",
                            "description": "<p> <img src=\"https://s2.loli.net/2024/07/14/CigHFA6BUOe1lxf.webp\" /><br /> </p> <p> <br /> </p> <p> <strong> 魔法少女与邪恶曾经敌对。 / 曾经、魔法少女和邪恶相互为敌。<br /> Katsute Mahou Shoujo to Aku wa Tekitai shiteita<br /> かつて魔法少女と悪は敵対していた。<br /> </strong> </p> <p> <br /> </p> <p> <strong> 字幕：Bilibili Global<br /> 脚本：S01T004721<br /> 压制：Анастасия<br /> 本片简繁字幕均经过繁化姬处理后生成，请自行判断下载；如有措辞不当，概不负责。<br /> </strong> </p> <p> <br /> </p> <hr /> <p> <br /> </p> <p> <strong> 本组作品首发于： <a href=\"https://acg.rip/?term=LoliHouse\" target=\"_blank\" rel=\"external nofollow\">acg.rip</a> | <a href=\"https://share.dmhy.org/topics/list?keyword=lolihouse\" target=\"_blank\" rel=\"external nofollow\">dmhy.org</a> | <a href=\"https://bangumi.moe/search/581be821ee98e9ca20730eae\" target=\"_blank\" rel=\"external nofollow\">bangumi.moe</a> | <a href=\"https://nyaa.si/?f=0&c=0_0&q=lolihouse\" target=\"_blank\" rel=\"external nofollow\">nyaa.si</a> </strong> </p> <p> <strong>各站发布情况取决于站点可用性，如有缺失烦请移步其他站点下载。</strong><br /> </p> <p> <br /> </p> <hr /> <p> <br /> </p> <p> <strong>为了顺利地观看我们的作品，推荐大家使用以下播放器：</strong> </p> <p> <strong>Windows：<a href=\"https://mpv.io/\" target=\"_blank\" rel=\"external nofollow\">mpv</a>（<a href=\"https://vcb-s.com/archives/7594\" target=\"_blank\" rel=\"external nofollow\">教程</a>）</strong> </p> <p> <strong>macOS：<a href=\"https://iina.io/\" target=\"_blank\" rel=\"external nofollow\">IINA</a></strong> </p> <p> <strong>iOS/Android：<a href=\"https://www.videolan.org/vlc/\" target=\"_blank\" rel=\"external nofollow\">VLC media player</a></strong> </p> <p> <br /> </p> <hr /> <p> <br /> </p> ...",
                            "pubDate": "2024-08-21T10:53:28",
                            "link": "http://share.dmhy.org/topics/view/677370_LoliHouse_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_-_07_WebRip_1080p_HEVC-10bit_AAC.html",
                            "guid": "http://share.dmhy.org/topics/view/677370_LoliHouse_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_-_07_WebRip_1080p_HEVC-10bit_AAC.html",
                            "enclosure": {
                                "url": "magnet:?xt=urn:btih:7M3N7SECHCATCTXDMWS5TGVE3ISHAEIT&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%3A80%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%2Fannounce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%2Fannounce",
                                "length": 1,
                                "type": "application/x-bittorrent"
                            }
                        }
                    ]
                }
            """.trimIndent(),
            """
                <rss xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:wfw="http://wellformedweb.org/CommentAPI/" version="2.0">
                <channel>
                <title>
                <![CDATA[ 動漫花園資源網 ]]>
                </title>
                <link>http://share.dmhy.org</link>
                <description>
                <![CDATA[ 動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论. ]]>
                </description>
                <language>zh-cn</language>
                <pubDate>Mon, 26 Aug 2024 22:17:18 +0800</pubDate>
                <item>
                <title>
                <![CDATA[ [北宇治字幕组] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. [07v2][WebRip][HEVC_AAC][繁日內嵌] ]]>
                </title>
                <link>http://share.dmhy.org/topics/view/677458_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html</link>
                <pubDate>Thu, 22 Aug 2024 21:15:43 +0800</pubDate>
                <description>
                <![CDATA[ <p><img src="https://p.inari.site/kitauji/pigeon/maho.webp" style="width:1400px;height:1988px" /></p><br /><p> <strong>为了您的播放能正确显示字幕效果，我们推荐您观看内封源使用 <a href="https://github.com/hooke007/MPV_lazy/releases" target="_blank" rel="external nofollow">MPV 播放器</a>，或者挂载 <a href="https://github.com/Masaiki/xy-VSFilter/releases" target="_blank" rel="external nofollow">XySubFilter with libass字幕滤镜</a>。</strong><br /> </p><p> <strong>本组字幕作品基于 <a href="https://creativecommons.org/licenses/by-nc-nd/4.0/" rel="external nofollow">CC BY-NC-ND 4.0 协议</a> 进行共享。如有需求，可前往北宇治字幕组的<a href="https://github.com/Kitauji-Sub" rel="external nofollow">字幕仓库</a>下载往期制作的外挂字幕。</strong><br /> </p><hr /><h3><strong>当前招募</strong></h3><p> <strong> <strong>翻译</strong> | 日语参考等级n2以上或能够听懂生肉内容并保证正确率，有经验者优先<br /> <strong>校对</strong> | 日语参考等级n1，善于发现翻译的错误<br /> <strong>美工</strong> | 熟练使用PS/AI等软件，能进行图片设计<br /> <strong>时轴</strong> | 熟练使用Aeg打轴/校轴并设置合适的样式、字体或屏幕字<br /> <strong>分流</strong> | 电脑/NAS能长时间开机，有足够的空间和上行带宽且至少具备ipv6公网，为组内资源分流<br /> <strong>有意者加群：招募群（<a href="https://jq.qq.com/?k=NWmvNiOQ" rel="external nofollow">232487445</a>）交流群（<a href="https://jq.qq.com/?k=dGxiOqRR" rel="external nofollow">713548082</a>）TG群（<a href="https://t.me/KitaUji" rel="external nofollow">@KitaUji</a>）</strong><br /> <img src="https://p.inari.site/guest/23-02/06/63e0ffef840de.webp" alt="img" /> </strong> </p> ]]>
                </description>
                <enclosure url="magnet:?xt=urn:btih:VKQ3KW4C2NT4WYK4QYW2IJKUBP3BPKML&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%2Fannounce&tr=http%3A%2F%2Fopen.nyaatorrents.info%3A6544%2Fannounce&tr=http%3A%2F%2Ft2.popgo.org%3A7456%2Fannonce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=http%3A%2F%2Fopentracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Ftracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftracker.kamigami.org%3A2710%2Fannounce&tr=http%3A%2F%2Fanidex.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=https%3A%2F%2Fopentracker.i2p.rocks%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.org%3A443%2Fannounce&tr=https%3A%2F%2Ftr.burnabyhighstar.com%3A443%2Fannounce&tr=http%3A%2F%2Ftr.nyacat.pw%2Fannounce&tr=https%3A%2F%2Ftr.nyacat.pw%2Fannounce" length="1" type="application/x-bittorrent"/>
                <author>
                <![CDATA[ Kitauji ]]>
                </author>
                <guid isPermaLink="true">http://share.dmhy.org/topics/view/677458_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html</guid>
                <category domain="http://share.dmhy.org/topics/list/sort_id/2">
                <![CDATA[ 動畫 ]]>
                </category>
                </item>
                <item>
                <title>
                <![CDATA[ [北宇治字幕组] 魔法少女与恶曾是敌人。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. [07v2][WebRip][HEVC_AAC][简日内嵌] ]]>
                </title>
                <link>http://share.dmhy.org/topics/view/677457_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html</link>
                <pubDate>Thu, 22 Aug 2024 21:15:30 +0800</pubDate>
                <description>
                <![CDATA[ <p><img src="https://p.inari.site/kitauji/pigeon/maho.webp" style="width:1400px;height:1988px" /></p><br /><p> <strong>为了您的播放能正确显示字幕效果，我们推荐您观看内封源使用 <a href="https://github.com/hooke007/MPV_lazy/releases" target="_blank" rel="external nofollow">MPV 播放器</a>，或者挂载 <a href="https://github.com/Masaiki/xy-VSFilter/releases" target="_blank" rel="external nofollow">XySubFilter with libass字幕滤镜</a>。</strong><br /> </p><p> <strong>本组字幕作品基于 <a href="https://creativecommons.org/licenses/by-nc-nd/4.0/" rel="external nofollow">CC BY-NC-ND 4.0 协议</a> 进行共享。如有需求，可前往北宇治字幕组的<a href="https://github.com/Kitauji-Sub" rel="external nofollow">字幕仓库</a>下载往期制作的外挂字幕。</strong><br /> </p><hr /><h3><strong>当前招募</strong></h3><p> <strong> <strong>翻译</strong> | 日语参考等级n2以上或能够听懂生肉内容并保证正确率，有经验者优先<br /> <strong>校对</strong> | 日语参考等级n1，善于发现翻译的错误<br /> <strong>美工</strong> | 熟练使用PS/AI等软件，能进行图片设计<br /> <strong>时轴</strong> | 熟练使用Aeg打轴/校轴并设置合适的样式、字体或屏幕字<br /> <strong>分流</strong> | 电脑/NAS能长时间开机，有足够的空间和上行带宽且至少具备ipv6公网，为组内资源分流<br /> <strong>有意者加群：招募群（<a href="https://jq.qq.com/?k=NWmvNiOQ" rel="external nofollow">232487445</a>）交流群（<a href="https://jq.qq.com/?k=dGxiOqRR" rel="external nofollow">713548082</a>）TG群（<a href="https://t.me/KitaUji" rel="external nofollow">@KitaUji</a>）</strong><br /> <img src="https://p.inari.site/guest/23-02/06/63e0ffef840de.webp" alt="img" /> </strong> </p> ]]>
                </description>
                <enclosure url="magnet:?xt=urn:btih:LMKXDZSQPSU3GDVNCIDKYLTAESZOQRAN&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%2Fannounce&tr=http%3A%2F%2Fopen.nyaatorrents.info%3A6544%2Fannounce&tr=http%3A%2F%2Ft2.popgo.org%3A7456%2Fannonce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=http%3A%2F%2Fopentracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Ftracker.acgnx.se%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftracker.kamigami.org%3A2710%2Fannounce&tr=http%3A%2F%2Fanidex.moe%3A6969%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=https%3A%2F%2Fopentracker.i2p.rocks%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%3A443%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.org%3A443%2Fannounce&tr=https%3A%2F%2Ftr.burnabyhighstar.com%3A443%2Fannounce&tr=http%3A%2F%2Ftr.nyacat.pw%2Fannounce&tr=https%3A%2F%2Ftr.nyacat.pw%2Fannounce" length="1" type="application/x-bittorrent"/>
                <author>
                <![CDATA[ Kitauji ]]>
                </author>
                <guid isPermaLink="true">http://share.dmhy.org/topics/view/677457_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_07v2_WebRip_HEVC_AAC.html</guid>
                <category domain="http://share.dmhy.org/topics/list/sort_id/2">
                <![CDATA[ 動畫 ]]>
                </category>
                </item>
                <item>
                <title>
                <![CDATA[ [LoliHouse] 魔法少女与邪恶曾经敌对。 / 曾经、魔法少女和邪恶相互为敌。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita - 07 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕] ]]>
                </title>
                <link>http://share.dmhy.org/topics/view/677370_LoliHouse_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_-_07_WebRip_1080p_HEVC-10bit_AAC.html</link>
                <pubDate>Wed, 21 Aug 2024 10:53:28 +0800</pubDate>
                <description>
                <![CDATA[ <p> <img src="https://s2.loli.net/2024/07/14/CigHFA6BUOe1lxf.webp" /><br /> </p> <p> <br /> </p> <p> <strong> 魔法少女与邪恶曾经敌对。 / 曾经、魔法少女和邪恶相互为敌。<br /> Katsute Mahou Shoujo to Aku wa Tekitai shiteita<br /> かつて魔法少女と悪は敵対していた。<br /> </strong> </p> <p> <br /> </p> <p> <strong> 字幕：Bilibili Global<br /> 脚本：S01T004721<br /> 压制：Анастасия<br /> 本片简繁字幕均经过繁化姬处理后生成，请自行判断下载；如有措辞不当，概不负责。<br /> </strong> </p> <p> <br /> </p> <hr /> <p> <br /> </p> <p> <strong> 本组作品首发于： <a href="https://acg.rip/?term=LoliHouse" target="_blank" rel="external nofollow">acg.rip</a> | <a href="https://share.dmhy.org/topics/list?keyword=lolihouse" target="_blank" rel="external nofollow">dmhy.org</a> | <a href="https://bangumi.moe/search/581be821ee98e9ca20730eae" target="_blank" rel="external nofollow">bangumi.moe</a> | <a href="https://nyaa.si/?f=0&c=0_0&q=lolihouse" target="_blank" rel="external nofollow">nyaa.si</a> </strong> </p> <p> <strong>各站发布情况取决于站点可用性，如有缺失烦请移步其他站点下载。</strong><br /> </p> <p> <br /> </p> <hr /> <p> <br /> </p> <p> <strong>为了顺利地观看我们的作品，推荐大家使用以下播放器：</strong> </p> <p> <strong>Windows：<a href="https://mpv.io/" target="_blank" rel="external nofollow">mpv</a>（<a href="https://vcb-s.com/archives/7594" target="_blank" rel="external nofollow">教程</a>）</strong> </p> <p> <strong>macOS：<a href="https://iina.io/" target="_blank" rel="external nofollow">IINA</a></strong> </p> <p> <strong>iOS/Android：<a href="https://www.videolan.org/vlc/" target="_blank" rel="external nofollow">VLC media player</a></strong> </p> <p> <br /> </p> <hr /> <p> <br /> </p> ... ]]>
                </description>
                <enclosure url="magnet:?xt=urn:btih:7M3N7SECHCATCTXDMWS5TGVE3ISHAEIT&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ft.nyaatracker.com%3A80%2Fannounce&tr=http%3A%2F%2Fnyaa.tracker.wf%3A7777%2Fannounce&tr=http%3A%2F%2Ftr.bangumi.moe%3A6969%2Fannounce&tr=https%3A%2F%2Ftr.bangumi.moe%3A9696%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%2Fannounce&tr=http%3A%2F%2Fshare.camoe.cn%3A8080%2Fannounce&tr=https%3A%2F%2Ftracker.nanoha.org%2Fannounce" length="1" type="application/x-bittorrent"/>
                <author>
                <![CDATA[ LoliHouse ]]>
                </author>
                <guid isPermaLink="true">http://share.dmhy.org/topics/view/677370_LoliHouse_Katsute_Mahou_Shoujo_to_Aku_wa_Tekitai_shiteita_-_07_WebRip_1080p_HEVC-10bit_AAC.html</guid>
                <category domain="http://share.dmhy.org/topics/list/sort_id/2">
                <![CDATA[ 動畫 ]]>
                </category>
                </item>
            """.trimIndent(),
        )
    }

    private fun test(
        @Language("json")
        expected: String,
        @Language("xml")
        xml: String
    ) {
        assertEquals(
            expected,
            json.encodeToString(
                RssChannel.serializer(),
                parser.parse(
                    Xml.parse(xml),
                ),
            ),
        )
    }
}