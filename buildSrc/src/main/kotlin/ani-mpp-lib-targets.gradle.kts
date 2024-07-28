import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/*
 * 配置 JVM + Android 的 compose 项目. 默认不会配置 resources. 
 * 
 * 该插件必须在 kotlin, compose, android 之后引入.
 * 
 * 如果开了 android, 就会配置 desktop + android, 否则只配置 jvm.
 */

extra.set("ani.jvm.target", 17)

val android = extensions.findByType(LibraryExtension::class)
val composeExtension = extensions.findByType(ComposeExtension::class)

configure<KotlinMultiplatformExtension> {
    /**
     * 平台架构:
     * ```
     * common
     *   - jvm (可访问 JDK, 但不能使用 Android SDK 没有的 API)
     *     - android (可访问 Android SDK)
     *     - desktop (可访问 JDK)
     *   - native
     *     - apple
     *       - ios
     *         - iosArm64
     *         - iosSimulatorArm64 TODO
     * ```
     *
     * `native - apple - ios` 的架构是为了契合 Kotlin 官方推荐的默认架构. 以后如果万一要添加其他平台, 可方便添加.
     */
    if (android != null) {
        jvm("desktop")
        androidTarget {
            attributes.attribute(AniTarget, "android")
        }
    } else {
        jvm()
    }
    iosArm64()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets.commonMain.dependencies {
        // 添加常用依赖
        if (composeExtension != null) {
            val compose = ComposePlugin.Dependencies(project)
            // Compose
            api(compose.foundation)
            api(compose.animation)
            api(compose.ui)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.runtime)
        }

        if (project.path != ":utils:platform") {
            implementation(project(":utils:platform"))
        }
    }
}

extensions.findByType(org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension::class)?.apply {
    enableStrongSkippingMode = true
}

if (android != null) {
    configure<KotlinMultiplatformExtension> {
        sourceSets {
            // Workaround for MPP compose bug, don't change
            removeIf { it.name == "androidAndroidTestRelease" }
            removeIf { it.name == "androidTestFixtures" }
            removeIf { it.name == "androidTestFixturesDebug" }
            removeIf { it.name == "androidTestFixturesRelease" }
        }
    }
    tasks.named("generateComposeResClass") {
        dependsOn("generateResourceAccessorsForAndroidUnitTest")
    }
    tasks.withType(KotlinCompilationTask::class) {
        dependsOn("generateComposeResClass")
        dependsOn("generateResourceAccessorsForAndroidRelease")
        dependsOn("generateResourceAccessorsForAndroidUnitTest")
        dependsOn("generateResourceAccessorsForAndroidUnitTestRelease")
        dependsOn("generateResourceAccessorsForAndroidUnitTestDebug")
        dependsOn("generateResourceAccessorsForAndroidDebug")
    }

    android.apply {
        compileSdk = getIntProperty("android.compile.sdk")
        defaultConfig {
            minSdk = getIntProperty("android.min.sdk")
        }
        buildTypes.getByName("release") {
            isMinifyEnabled = false // shared 不能 minify, 否则构建 app 会失败
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                *sharedAndroidProguardRules(),
            )
        }
        buildFeatures {
            compose = true
        }
    }
}
