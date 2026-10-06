<div align="center">
<img src=".github/branding/logo_full_trans.png" align="center" width="200px" />

<h1 align="center">EvilBrowse</h1>

<p align="center">
A polished, private, local-first Chromium desktop browser with <b>vertical tabs</b>, built-in <b>uBlock-Origin filter blocking</b>, and <b>opt-in local AI</b> — stabilized from Turtlebrowse.
</p>
<p><a href="/CHANGELOG.md">Changelog</a> • <a href="/LICENSE">License</a> • Forked from <a href="https://github.com/ingStudiosOfficial/turtlebrowse">ingStudiosOfficial/turtlebrowse</a></p>
<hr />
</div>

> **Why "Evil"?** The name is playful: the browser is "evil" because it fights bugs and refuses to behave badly. It is a legitimate web browser, not malware.

## Download

EvilBrowse packages are built from this repo with Gradle `jpackage` (see `app/build.gradle.kts`). The upstream Turtlebrowse downloads live at [turtlebrowse.ingstudios.dev](https://turtlebrowse.ingstudios.dev).

## Features

- Vertical tabs with collapsible sidebar, plus optional classic horizontal strip
- uBlock Origin filter lists + EasyList ad/tracker blocking (toggle in Settings → Privacy)
- Fast cold startup: AI is opt-in and never initializes unless enabled
- Powered by the Java Chromium Embedded Framework
- 100% local opt-in agentic AI via Ollama
- Multiple isolated user profiles + guest mode
- Free and open-source (FOSS, Apache 2.0)

<div align="center">
	<h2>YouTube Video</h2>
	<a href="https://youtu.be/WzU8Cnl3kE0?si=k_mfjx7AwVNErvR7" target="_blank"><img src="./.github/branding/yt_thumbnail.png" width="300" /></a>
	<p>Watch the full video on <a href="https://youtu.be/WzU8Cnl3kE0?si=k_mfjx7AwVNErvR7" target="_blank">YouTube</a>.</p>
</div>

## Download

The latest version of Turtlebrowse is available for download at [turtlebrowse.ingstudios.dev](https://turtlebrowse.ingstudios.dev).

If you want to download a previous version of Turtlebrowse, it can be found in the [releases](https://github.com/ingStudiosOfficial/turtlebrowse/releases) section.

## Features

- Powered by the Java Chromium Embedded Framework
- Follows Chromium standards
- Built-in Chrome DevTools and Chrome DevTools Protocol
- 100% local agentic AI
- Dynamic Material You theme
- Multiple isolated user profiles
- Discord Presence toggle
- YouTube and other platforms video downloader with yt-dlp
- Always private, no data leaves your device
- Free and open-source (FOSS)
- Guest/private mode where all data from disk is deleted when closed
- An integrated terminal in the sidebar
- Available for Linux, Windows, and macOS on different architectures

## Development

EvilBrowse is a stabilization fork of Turtlebrowse and welcomes contributions. We welcome any sort of contributions that are **human made**. Here's how to build EvilBrowse.

### Prerequisites

- **Git** - Source control for EvilBrowse

- **JDK 25** - EvilBrowse is powered by Java 25 and uses the latest features

- **Node.js** - Used to build the internal pages and website

### Building and Running

#### Browser

1. **Clone the repository**
```bash
git clone https://github.com/ingStudiosOfficial/turtlebrowse.git
cd turtlebrowse
```

2. **Build the Gradle project**
```bash
# Relative to the root of the project
cd app
./gradlew build # or ./gradlew.bat build on Windows
```

3. **Run the Gradle project**
```bash
./gradlew run # or ./gradlew.bat run on Windows
```

#### Internal Pages

1. **Install dependencies**
```bash
# Relative to the root of the project
cd frontend/pages
npm install
```

2. **Run the pages in development mode**
```bash
npm run dev
```

3. **Build the pages**
```bash
npm run build
```

#### Internal Games

##### Dino

1. **Install dependencies**
```bash
# Relative to the root of the project
cd frontend/games/dino
npm install
```

2. **Run the dino game in development mode**
```bash
npm run dev
```

3. **Build the dino game**
```bash
npm run build
```

#### Website

1. **Install dependencies**
```bash
# Relative to the root of the project
cd website
npm install
```

2. **Run the website in development mode**
```bash
npm run dev
```

3. **Build the website**
```bash
npm run build
```

### Updates proxy

1. **Run the Go project**
```bash
# Relative to the root of the project
cd updates
go run .
```

2. **Build the binary**
```bash
go build -o ./build/evilbrowseupdates .
```

## Credits

Huge thank you to the [Java Chromium Embedded Framework](https://github.com/chromiumembedded/java-cef) project for providing Java bindings to CEF and [jcefmaven](https://github.com/jcefmaven/jcefmaven) for providing pre-built binaries for Gradle. This project would not have been possible without them.

**Notable mentions**

- [wayou/t-rex-runner](https://github.com/wayou/t-rex-runner) for decompiling the Chromium Dino game from the [Chromium](https://github.com/chromium/chromium) source code (I tried doing it myself but it was a pain, huge thanks!)
- [JetBrains/jediterm](https://github.com/JetBrains/jediterm) for providing integrated terminal support
- [sshahine/JFoenix](https://github.com/sshahine/JFoenix) for beautiful Material 3 JavaFX components
- [ollama4j/ollama4j](https://github.com/ollama4j/ollama4j) for the Java API wrapper for Ollama

The other dependencies that can be found in [build.gradle.kts](app/build.gradle.kts) are also very much appredicated!

## License

EvilBrowse is licensed under the Apache 2.0 License. Check [LICENSE](./LICENSE) for more details.

© 2026 (ing) Studios and Ethan Lee
