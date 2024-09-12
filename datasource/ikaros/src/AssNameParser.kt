package me.him188.ani.datasources.ikaros

import io.ktor.util.toLowerCasePreservingASCIIRules

internal class AssNameParser {

    private val scCnLowerCaseList = listOf("sc", "chs", "gb")
    private val tcCnLowerCaseList = listOf("tc", "cht", "big5")

    /**
     * parse ass name to language.
     *
     * such as `[DBD-Raws][XXX！][01][1080P][BDRip][HEVC-10bit][FLAC].sc.ass` to `sc`.
     */
    fun parseAssName2Language(name: String): String {
        if (name.isBlank() || !name.endsWith("ass")) return name
        // remove `.ass` postfix 
        val removeSuffix = name.removeSuffix(".ass")
        val language = removeSuffix.substringAfterLast('.', "")
        if (language.isEmpty()) return removeSuffix
        if (scCnLowerCaseList.contains(language.toLowerCasePreservingASCIIRules())) return "简中"
        if (tcCnLowerCaseList.contains(language.toLowerCasePreservingASCIIRules())) return "繁中"
        return language;
    }

    companion object {
        val default by lazy(LazyThreadSafetyMode.PUBLICATION) {
            AssNameParser()
        }

        val mineTypeTextXssa = "text/x-ssa"
        val mineTypeApplicationXssa = "application/x-ssa"

        /**
         * default is `text/x-ssa`.
         *
         * @see AssNameParser.mineTypeTextXssa
         * @see AssNameParser.mineTypeApplicationXssa
         */
        val httpMineType = mineTypeTextXssa;
    }

}
