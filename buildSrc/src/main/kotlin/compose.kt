import org.gradle.api.Project

val Project.composeOrNull
    get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.findByName("compose") as org.jetbrains.compose.ComposeExtension?
