package me.him188.ani.datasources.api.test.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.test.codegen.main.TestData
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.titles.ParsedTopicTitle
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse

val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

class TopicSet(
    val topics: List<Topic>,
)

class TestSuite(
    val originalName: String,
    val dataSource: String,
    val name: String,
    val cases: List<TestCase>
)

class TestCase(
    val name: String,
    val title: String,
    val parsed: ParsedTopicTitle,
)

class TestGenerator(
    private val parser: RawTitleParser
) {
    fun createSuite(testData: TestData): TestSuite {
        fun String.sanitize() = replace("%", "_")
            .replace(".", "_")
            .replace("-", "_")

        return TestSuite(
            originalName = testData.originalName,
            name = testData.kotlinClassName.sanitize(),
            dataSource = testData.dataSource,
            cases = testData.topics.map {
                TestCase(
                    name = it.id.removeSuffix(".html").sanitize(),
                    title = it.rawTitle,
                    parsed = parser.parse(it.rawTitle)
                )
            }
        )
    }

    // 这库真是各种难用
    fun generateSuite(suite: TestSuite): FileSpec = FileSpec.builder(
        "", // 他会创建目录层级
        "PatternTitleParserTest${suite.name}"
    ).apply {
        addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("Suppress")).apply {
            addMember("\"FunctionName\"")
            addMember("\"ClassName\"")
            addMember("\"RedundantVisibilityModifier\"")
            addMember("\"PackageDirectoryMismatch\"")
            addMember("\"NonAsciiCharacters\"")
            addMember("\"SpellCheckingInspection\"")
        }.build())
        addType(
            TypeSpec.classBuilder(
                ClassName(
                    "me.him188.ani.datasources.api.title.generated", // 这库并不会写出 package
                    "PatternTitleParserTest${suite.name}"
                )
            ).apply {
                addKdoc(
                    """
                        原名: `${suite.originalName}`
                        数据源: `${suite.dataSource}`
                        
                        由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
                        如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
                        如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
                """.trimIndent()
                )
                addImport("kotlin.test", "assertEquals") // 它不允许 "*"
                superclass(ClassName("me.him188.ani.datasources.api.title", "PatternBasedTitleParserTestSuite"))
                for (case in suite.cases) case.parsed.run {
                    addFunction(
                        FunSpec.builder(case.name)
                            .addAnnotation(ClassName.bestGuess("kotlin.test.Test"))
                            // 这库会自动 wrap code, 如果不写 %S 就可能出问题
                            // 他不会自动换行, 必须要有 + "\n"
                            .addCode(
                                """val r = parse(%S)""" + "\n", case.title
                            )
                            .addCode("assertEquals(%S, r.episodeRange.toString())" + "\n", episodeRange.toString())
                            .addCode(
                                "assertEquals(%S, r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })" + "\n",
                                subtitleLanguages.sortedBy { it.id }.joinToString { it.id }
                            )
                            .addCode(
                                "assertEquals(%S, r.resolution.toString())" + "\n",
                                resolution.toString()
                            )
                            .build()
                    )
                }
            }.build()
        )
    }.build()
}

private const val Q = "\"\"\""
