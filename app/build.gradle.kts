plugins {
    application

    id("org.openjfx.javafxplugin") version "0.1.0" 
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // JCEF Maven for Chromium embedding
    implementation("me.friwi:jcefmaven:141.0.10")
}

// Apply a specific Java toolchain. 
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// JavaFX Configuration
javafx {
    version = "21"
    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.swing")
}

application {
    // Define the main class for the application.
    mainClass = "ingstudios.turtlebrowse.Main"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}