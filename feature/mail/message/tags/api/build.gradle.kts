plugins {
    id(ThunderbirdPlugins.Library.kmp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "net.thunderbird.feature.mail.message.tags.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.core.ui.navigation)
        }
    }
}

codeCoverage {
    branchCoverage = 0
    lineCoverage = 0
}
