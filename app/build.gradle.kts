plugins {
    application

    id("org.openjfx.javafxplugin") version "0.1.0"

    id("com.gradleup.shadow") version "9.3.2"

    id("org.panteleyev.jpackageplugin") version "2.0.1"
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
    implementation("me.friwi:jcefmaven:146.0.10")

    // Material icons from Ikonli
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-material2-pack:12.4.0")

    // Monet theme builder for JavaFX
    implementation("org.glavo:MonetFX:0.4.0")

    // Ollama for Java
    implementation("io.github.ollama4j:ollama4j:1.1.7")

    // JFoenix library
    implementation("com.jfoenix:jfoenix:9.0.10")

    // HTML to Markdwon
    implementation("dev.kreuzberg:html-to-markdown:2.29.0")

    // jsoup
    implementation("org.jsoup:jsoup:1.22.2")

    // ControlsFX
    implementation("org.controlsfx:controlsfx:11.2.1")

    // Nitrite DB
    implementation(platform("org.dizitart:nitrite-bom:4.3.2"))
    implementation("org.dizitart:nitrite")
    implementation("org.dizitart:nitrite-mvstore-adapter:4.3.2")

    // Google Guava
    implementation("com.google.guava:guava:33.6.0-jre")

    // Google Gson
    implementation("com.google.code.gson:gson:2.14.0")

    // Discord Rich Presence
    implementation("io.github.CDAGaming:DiscordIPC:0.10.2")
}

// Apply a specific Java toolchain. 
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// JavaFX Configuration
javafx {
    version = "24"
    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.swing")
}

val osName = System.getProperty("os.name").lowercase()
val isLinux = osName.contains("linux")
val isMac = osName.contains("darwin") || osName.contains("mac") || osName.contains("osx")   

val baseJvmArgs = listOf(
    "--enable-native-access=ALL-UNNAMED,javafx.graphics",
    "--add-modules=jdk.incubator.vector",
    "-Djava.library.path=build/natives",
    "-Dsun.java2d.opengl=false",
    "-Dsun.java2d.xrender=false",
    "-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel",
    "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
    "--add-opens=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "-Dapp.dir=\$APPDIR",
    "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
    "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
    "--add-exports=java.desktop/sun.lwawt=ALL-UNNAMED",
    "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
    "--add-exports=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
    "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
)

val jvmArgs = baseJvmArgs + 
    if (isLinux) listOf("-Dglass.platform=gtk") 
    else if (isMac) listOf(
        "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-exports=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-exports=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    ) 
    else emptyList()

application {
    mainClass.set("dev.ingstudios.turtlebrowse.Main")
    applicationDefaultJvmArgs = jvmArgs
}

tasks.jpackage {
    verbose = true

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
    }

    runtimeImage = javaLauncher.get().metadata.installationPath.asFile
    
    appName = "Turtlebrowse"
    vendor = "(ing) Studios"
    appVersion = "1.4.6"
    copyright = "2026 (ing) Studios and Ethan Lee"

    input = layout.buildDirectory.dir("libs")
    
    mainJar = "app-all.jar"
    dependsOn(tasks.shadowJar)
    mainClass = "dev.ingstudios.turtlebrowse.Main"

    destination = layout.buildDirectory.dir("dist")

    icon = when {
        System.getProperty("os.name").lowercase().contains("win") -> 
            layout.projectDirectory.file("src/main/resources/icon.ico")
        System.getProperty("os.name").lowercase().contains("mac") -> 
            layout.projectDirectory.file("src/main/resources/icon.icns")
        else -> 
            layout.projectDirectory.file("src/main/resources/logo_full_trans.png")
    }

    javaOptions = jvmArgs

    windows {
        type = org.panteleyev.jpackage.ImageType.EXE
        winDirChooser = true
        winMenu = true
        winShortcut = true
        winShortcutPrompt = true
        installDir = "ingStudios\\Turtlebrowse"
        winUpgradeUuid = "6f701d42-0c33-443a-98fa-6543c3e7b3df"
    }

    mac {
        type = org.panteleyev.jpackage.ImageType.PKG
        macPackageName = "Turtlebrowse"
        macPackageIdentifier = "dev.ingstudios.turtlebrowse"
    }

    linux {
        val pkgType = project.property("targetPkgType").toString().lowercase()
        if (pkgType != null) {
            type = if (pkgType == "rpm") {
                org.panteleyev.jpackage.ImageType.RPM
            } else {
                org.panteleyev.jpackage.ImageType.DEB
            }
        } else {
            type = org.panteleyev.jpackage.ImageType.RPM
        }
        linuxShortcut = true
        linuxMenuGroup = "Network;WebBrowser;"
        linuxAppCategory = "web" 
        linuxPackageName = "turtlebrowse"
        linuxDebMaintainer = "contact@ingstudios.dev"
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    environment("_JAVA_AWT_WM_NONREPARENTING", "0")
    environment("GDK_BACKEND", "x11")
    environment("WAYLAND_DISPLAY", "")
}
