import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

(kotlinExtension as? KotlinMultiplatformExtension)?.run {
    jvm()
}
