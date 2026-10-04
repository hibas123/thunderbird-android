plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "net.thunderbird.feature.mail.message.tags.internal"
}

dependencies {
    implementation(projects.feature.mail.message.tags.api)
    implementation(projects.feature.mail.message.list.api)

    implementation(projects.core.preference.api)
    implementation(projects.core.ui.contract)
    implementation(projects.core.ui.navigation)
    implementation(projects.core.ui.compose.common)

    testImplementation(projects.core.ui.compose.testing)
}

codeCoverage {
    branchCoverage = 0
    lineCoverage = 0
}
