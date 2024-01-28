import org.gradle.api.Project

fun Project.configureCompose() {
    composeOrNull?.apply {
        kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:1.5.8-dev-k2.0.0-Beta2-99ed868a0f8")
//        kotlinCompilerPlugin.set(dependencies.compiler.forKotlin("2.0.0-Beta2"))
//        kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=2.0.0-Beta2")
    }
}

val Project.composeOrNull
    get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.findByName("compose") as org.jetbrains.compose.ComposeExtension?
