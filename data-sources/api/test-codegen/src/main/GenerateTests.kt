package me.him188.ani.datasources.api.test.codegen.main

import me.him188.ani.datasources.api.test.codegen.TestGenerator
import me.him188.ani.datasources.api.test.codegen.json
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import java.io.File

/**
 * 从 `testData` 目录中生成单元测试
 */
fun main(args: Array<String>) { // 直接 run 就行
    val inputDir = File(args.getOrNull(0) ?: "testData")
    val outputDir = File(args.getOrNull(1) ?: "../src/commonTest/kotlin/title/generated")

    val suites = inputDir.walk().filter { it.extension == "json" }
        .map {
            try {
                json.decodeFromString<TestData>(it.readText())
            } catch (e: Exception) {
                throw IllegalStateException("Failed to parse ${it.name}", e)
            }
        }
        .toList()

    println("Found ${suites.size} test data, total ${suites.sumOf { it.topics.size }} topics")

    TestGenerator(RawTitleParser.getDefault()).run {
        for (data in suites) {
//            val out = outputDir.resolve(data.kotlinClassName + ".kt")
            println("Generating suite '${data.kotlinClassName}'")
            val suite = createSuite(data)
            val generated = generateSuite(suite)
            generated.writeTo(outputDir)
            outputDir.resolve(generated.name + ".kt").run {
                writeText("// @formatter:off\n" + readText() + "\n// @formatter:on\n")
            }
        }
    }
}