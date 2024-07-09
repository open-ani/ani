package me.him188.ani.app.data.models.subject

class RelatedPersonInfo(
    val personInfo: PersonInfo,
    val relation: String,
) {
    companion object {
        private val SORT_ORDER = listOf(
            "动画制作",
            "原作",
            "监督", "导演",
            "脚本", "编剧",
            "音乐",
            "人设", "人物设定",
            "系列构成",
            "美术设计",
            "动作作画监督",
        )

        fun sortList(personList: List<RelatedPersonInfo>): List<RelatedPersonInfo> {
            /*
           中文名="葬送的芙莉莲"
           别名=[{"v":"Frieren: Beyond Journey's End"},{"v":"Sousou no Frieren"},{"v":"葬送的芙莉蓮"}]
           话数="28"
           放送开始="2023年9月29日"
           放送星期="星期五"
           官方网站="https://frieren-anime.jp/"
           播放电视台="日本テレビ系"
           其他电视台="BS日テレ / AT-X"
           Copyright="© 山田鐘人・アベツカサ／小学館／「葬送のフリーレン」製作委員会"
           原作="山田鐘人・アベツカサ（小学館「週刊少年サンデー」連載中）"
           导演="斎藤圭一郎"
           音乐="Evan Call"
           人物设定="長澤礼子"
           系列构成="鈴木智尋"
           动画制作="MADHOUSE"
           美术设计="杉山晋史"
           动作作画监督="岩澤亨(动作导演)"
           概念艺术="吉岡誠子"
           设定="原科大樹(魔物设计)"
           主题歌编曲="Ayase（OP1） / Evan Call（ED1,SPED）/ n-buna（OP2）"
           主题歌作曲="Ayase（OP1） / milet、野村陽一郎、中村泰輔（ED1） / Evan Call（SPED）/ n-buna（OP2）"
           主题歌作词="Ayase（OP1） / milet（ED1,SPED）/ n-buna（OP2）"
           主题歌演出="YOASOBI（OP1） / milet（ED1,SPED）/ ヨルシカ（OP2）"
           製作="「葬送のフリーレン」製作委員会【東宝（藤田雅規、齋藤雅哉、山田祥子、佐野航）、小学館（島村英司）、日本テレビ（稲毛弘之、中谷敏夫、吉田和生、今井蘭泉）、MADHOUSE、小学館集英社プロダクション、Aniplex、電通】"
           企画="大田圭二、沢辺伸政；共同企划：佐藤貴博、田代早苗、佐藤龍伸、三宅将典、東山敦"
           执行制片人="山中一孝、備前島幹人"
           总制片人="高橋敦司、武井克弘"
           制片人="田口翔一朗、四竈泰介、岩佐直樹、田口亜有理、伊藤悠公、青木遥、菊池瑠梨子"
           副制片人="竹田晃洋、野呂瀬友里、平原唯灯"
           原作协力="大嶋一範、小倉功雅、芳仲宏暢"
           音乐制作="東宝ミュージック、ミラクル・バス"
           音乐制作人="有馬由衣"
           OP・ED 分镜="斎藤圭一郎（OP1）、hohobun（ED1）、吉成鋼（SPED）"
           原画="こはく(中村豊)"
           动画制片人="福士裕一郎＆中目貴史"
            */
            return personList.sortedBy { info ->
                val index = SORT_ORDER.indexOfFirst { position ->
                    info.relation == position
                }
                if (index == -1) {
                    Int.MAX_VALUE
                } else {
                    index
                }
            }
        }
    }
}