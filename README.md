<div align="center">
<img src=".github/branding/logo_full_trans.png" align="center" width="200px" />

<h1 align="center">Turtlebrowse</h1>

<p align="center">
A <b>Material You</b> themed JVM based <b>agentic</b> browser made in JCEF (Java Chromium Embedded Framework), Swing, and JavaFX.
</p>
<p><a href="https://www.youtube.com/watch?v=WzU8Cnl3kE0">YouTube Video</a> • <a href="https://turtlebrowse.ingstudios.dev">Download</a> • <a href="/LICENSE">License</a></p>
<hr />
</div>

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
- 100% local agentic AI
- Dynamic Material You theme
- Multiple isolated user profiles
- Always private, no data leaves your device
- Free and open-source (FOSS)

## Development

Turtlebrowse has reached a stable stage, but we are always looking for contributors. We welcome any sort of contributions that are **human made**. Here's how to build Turtlebrowse.

### Prerequisites

- **Git** - Source control for Turtlebrowse

- **JDK 25** - Turtlebrowse is powered by Java 25 and uses the latest features

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

## Credits

Huge thank you to the [Java Chromium Embedded Framework](https://github.com/chromiumembedded/java-cef) project for providing Java bindings to CEF and [jcefmaven](https://github.com/jcefmaven/jcefmaven) for providing pre-built binaries for Gradle. This project would not have been possible without them.

**Notable mentions**

- [wayou/t-rex-runner](https://github.com/wayou/t-rex-runner) for decompiling the Chromium Dino game from the [Chromium](https://github.com/chromium/chromium) source code (I tried doing it myself but it was a pain, huge thanks!)

## License

Turtlebrowse is licensed under the Apache 2.0 License. Check [LICENSE](./LICENSE) for more details.

© 2026 (ing) Studios and Ethan Lee
