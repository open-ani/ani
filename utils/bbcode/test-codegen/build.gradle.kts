plugins {
    kotlin("jvm")
    `flatten-source-sets`
}

dependencies {
    implementation(projects.utils.bbcode)
    implementation(projects.utils.testing)
    implementation(libs.kotlinpoet)
}
