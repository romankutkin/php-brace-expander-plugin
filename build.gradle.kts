import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit)

    intellijPlatform {
        phpstorm("2026.2")
        bundledPlugin("com.jetbrains.php")
        testFrameworks(
            TestFrameworkType.Platform,
            TestFrameworkType.JUnit5,
            TestFrameworkType.JUnit5CodeInsight,
        )
    }
}

tasks.test {
    useJUnitPlatform()
}
