import com.android.build.api.dsl.CommonExtension

/**
 * 扁平化源集目录结构, 减少文件树层级 by 2
 *
 * 变化:
 * ```
 * src/${targetName}Main/kotlin -> ${targetName}Main
 * src/${targetName}Main/resources -> ${targetName}Resources
 * src/${targetName}Test/kotlin -> ${targetName}Test
 * src/${targetName}Test/resources -> ${targetName}TestResources
 * ```
 *
 * `${targetName}` 可以是 `common`, `android` `desktop` 等.
 */
fun configureFlattenSourceSets() {
    val flatten = extra.runCatching { get("flatten.sourceset") }.getOrNull()?.toString()?.toBoolean() ?: true
    if (!flatten) return
    sourceSets {
        findByName("main")?.apply {
            resources.srcDirs(listOf(projectDir.resolve("resources")))
            java.srcDirs(listOf(projectDir.resolve("src")))
        }
        findByName("test")?.apply {
            resources.srcDirs(listOf(projectDir.resolve("testResources")))
            java.srcDirs(listOf(projectDir.resolve("test")))
        }
    }
}

/**
 * 扁平化多平台项目的源集目录结构, 减少文件树层级 by 2
 *
 * 变化:
 * ```
 * src/androidMain/res -> androidRes
 * src/androidMain/assets -> androidAssets
 * src/androidMain/aidl -> androidAidl
 * src/${targetName}Main/kotlin -> ${targetName}Main
 * src/${targetName}Main/resources -> ${targetName}Resources
 * src/${targetName}Test/kotlin -> ${targetName}Test
 * src/${targetName}Test/resources -> ${targetName}TestResources
 * ```
 *
 * `${targetName}` 可以是 `common`, `android` `desktop` 等.
 */
fun Project.configureFlattenMppSourceSets() {
    kotlinSourceSets?.invoke {
        fun setForTarget(
            targetName: String,
        ) {
            findByName("${targetName}Main")?.apply {
                resources.srcDirs(listOf(projectDir.resolve("${targetName}Resources")))
                kotlin.srcDirs(listOf(projectDir.resolve("${targetName}Main"), projectDir.resolve(targetName)))
            }
            findByName("${targetName}Test")?.apply {
                resources.srcDirs(listOf(projectDir.resolve("${targetName}TestResources")))
                kotlin.srcDirs(listOf(projectDir.resolve("${targetName}Test")))
            }
        }

        setForTarget("common")

        allKotlinTargets().all {
            val targetName = name
            setForTarget(targetName)
        }
    }

    extensions.findByType(CommonExtension::class)?.run {
        this.sourceSets["main"].res.srcDirs(projectDir.resolve("androidRes"))
        this.sourceSets["main"].assets.srcDirs(projectDir.resolve("androidAssets"))
        this.sourceSets["main"].aidl.srcDirs(projectDir.resolve("androidAidl"))
    }
}

configureFlattenSourceSets()
configureFlattenMppSourceSets()