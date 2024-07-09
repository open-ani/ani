import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    idea
    `flatten-source-sets`
    `ani-lib-targets`
    id("org.openapi.generator") version "7.6.0"
}

val generatedRoot = "generated/openapi"

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.dataSources.api)
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.coroutines.core)

        implementation(projects.utils.serialization)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
    }
    sourceSets.commonMain {
        kotlin.srcDirs(file("src/commonMain/gen"))
    }
}

idea {
    module {
        generatedSourceDirs.add(file("src/commonMain/gen"))
    }
}

// https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
val generateApiV0 = tasks.register("generateApiV0", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/v0.yaml")
    outputDir.set(layout.buildDirectory.file(generatedRoot).get().asFile.absolutePath)
    packageName.set("me.him188.ani.datasources.bangumi")
    modelNamePrefix.set("Bangumi")
    apiNameSuffix.set("BangumiApi")
    // https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/kotlin.md
    additionalProperties.set(
        mapOf(
            "apiSuffix" to "BangumiApi",
            "library" to "multiplatform",
            "dateLibrary" to "kotlinx-datetime",
//            "serializationLibrary" to "kotlinx_serialization", // 加了这个他会生成两个 `@Serializable`
            "enumPropertyNaming" to "UPPERCASE",
//            "generateOneOfAnyOfWrappers" to "true",
            "omitGradleWrapper" to "true",
        ),
    )
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)

//    typeMappings.put("BangumiValue", "kotlinx.serialization.json.JsonElement")
//    schemaMappings.put("WikiV0", "kotlinx.serialization.json.JsonElement") // works
//    schemaMappings.put("Item", "kotlinx.serialization.json.JsonElement")
//    schemaMappings.put("Value", "kotlinx.serialization.json.JsonElement")
    typeMappings.put(
        "kotlin.Double",
        "@Serializable(me.him188.ani.utils.serialization.BigNumAsDoubleStringSerializer::class) me.him188.ani.utils.serialization.BigNum",
    )
//    typeMappings.put("BangumiEpisodeCollectionType", "/*- `0`: 未收藏 - `1`: 想看 - `2`: 看过 - `3`: 抛弃*/ Int")
}

val generateApiP1 = tasks.register("generateApiP1", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set(stripP1Api("$projectDir/p1.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.file(generatedRoot).get().asFile.absolutePath)
    packageName.set("me.him188.ani.datasources.bangumi.next")
    modelNamePrefix.set("BangumiNext")
    apiNameSuffix.set("BangumiNextApi")
    additionalProperties.set(
        mapOf(
            "apiSuffix" to "BangumiNextApi",
            "library" to "multiplatform",
            "dateLibrary" to "kotlinx-datetime",
            "enumPropertyNaming" to "UPPERCASE",
            "omitGradleWrapper" to "true",
        ),
    )
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    validateSpec.set(false)

    typeMappings.put(
        "kotlin.Double",
        "@Serializable(me.him188.ani.utils.serialization.BigNumAsDoubleStringSerializer::class) me.him188.ani.utils.serialization.BigNum",
    )
}

private fun stripP1Api(path: String): File {
    val yaml = org.yaml.snakeyaml.Yaml()
    val p1ApiObject: Map<String, Any> = File(path).inputStream().use { yaml.load(it) }

    // keep subjects only
    val paths = p1ApiObject["paths"].cast<Map<String, *>>().toMutableMap()
    val subjectPaths = paths.filter { (path, _) -> path.startsWith("/p1/subjects") }

    // keep components referred by subjects only
    val components = p1ApiObject["components"].cast<Map<String, *>>().toMutableMap()
    components.remove("securitySchemes")
    val keepSchemaKeys = listOf(
        "ErrorResponse",
        "Topic",
        "SubjectInterestComment",
        "TopicCreation",
        "TopicDetail",
        "GroupReply",
        "BaseEpisodeComment",
        "Group",
        "Subject",
        "Reaction",
        "Reply",
    )
    val schemas = components["schemas"].cast<Map<String, *>>().toMutableMap()
    val keepSchemas = schemas.filter { (component, _) -> component in keepSchemaKeys }

    val strippedApiObject = mutableMapOf<String, Any>().apply {
        put("openapi", p1ApiObject["openapi"].cast())
        put("info", p1ApiObject["info"].cast())
        put("paths", subjectPaths)
        put("components", mapOf("schemas" to keepSchemas))
    }

    return File.createTempFile("ani-build-fixGeneratedOpenApi-next-p1-stripped", ".yaml").apply {
        deleteOnExit()
        writeText(yaml.dump(strippedApiObject))
    }
}

val fixGeneratedOpenApi = tasks.register("fixGeneratedOpenApi") {
    dependsOn(generateApiV0, generateApiP1)

    val modelsV0 = layout
        .buildDirectory
        .file("$generatedRoot/src/commonMain/kotlin/me/him188/ani/datasources/bangumi/models/")
        .get()
        .asFile
    val modelsP1 = layout
        .buildDirectory
        .file("$generatedRoot/src/commonMain/kotlin/me/him188/ani/datasources/bangumi/next/models/")
        .get()
        .asFile
    

//    inputs.file(file)
//    outputs.file(file)
    //    outputs.upToDateWhen {
//        models.resolve("BangumiValue.kt").readText() == expected
//    }
    doLast {
        modelsV0.resolve("BangumiValue.kt").writeText(
            """
                package me.him188.ani.datasources.bangumi.models
                
                typealias BangumiValue = kotlinx.serialization.json.JsonElement
            """.trimIndent(),
        )
        modelsV0.resolve("BangumiEpisodeCollectionType.kt").delete()
        modelsV0.resolve("BangumiSubjectCollectionType.kt").delete()

        modelsP1.resolve("BangumiNextListGroupMembersByNameTypeParameter.kt").delete()
        modelsP1.resolve("BangumiNextPatchEpisodeWikiInfoRequestEpisodeType.kt").delete()
    }
}

val copyGeneratedToSrc = tasks.register("copyGeneratedToSrc", Copy::class) {
    dependsOn(fixGeneratedOpenApi)
    from(layout.buildDirectory.file("$generatedRoot/src/commonMain/kotlin"))
    into("src/commonMain/gen")
}

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    dependsOn(fixGeneratedOpenApi)
//}

tasks.register("generateOpenApi") {
    dependsOn(copyGeneratedToSrc)
}
