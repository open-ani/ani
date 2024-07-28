import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/*
 * 为 Compose 配置 JVM + Android 的 HMPP 源集结构
 */

(kotlinExtension as? KotlinMultiplatformExtension)?.run {
    androidTarget {
        attributes.attribute(AniTarget, "android")
    }
    jvm("desktop")

    sourceSets {
        val commonMain by getting {

        }

        val jvmMain by creating {
            dependsOn(commonMain)
        }

        androidMain {
            dependsOn(jvmMain)
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val appleMain by creating {
            dependsOn(nativeMain)
        }

        val iosMain by creating {
            dependsOn(appleMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}
