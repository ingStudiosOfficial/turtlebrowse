# Turtlebrowse Changelog

All major and minor changes, alongside with bug fixes go here. More details available with each releases' full changelog.

## v1.11.4

**8/10/2026**

- Added downloads directory picker
- Unified `yt-dlp` downloads directory and default downloads directory with `FileSystemManager`
- Fixed newtab page `turtlebrowse://` URLs

## v1.11.3

**8/10/2026**

- Added custom newtab URLs
- Fixed local file handling scheme
- Refactored settings dialogs with global class

## v1.11.2

**7/10/2026**

- Added adblock toggle
- Added restart browser snackbar for AI model settings
- Added zoom level guard if is 0

## v1.11.1

**7/10/2026**

- Fixed native library loading for `adblock-coffee`

## v1.11.0

**6/10/2026**

- Added ad-blocking with EasyList rules via CEF resource request handler
- Added `adblock-coffee` into `app/libs/` containing the jar and shared libraries
- Fixed version in downloads page

## v1.10.1

**5/10/2026**

- Added copy address link to clipboard in context menu
- Added open link in new tab in context menu
- Added page zoom controls

## v1.10.0

**2/10/2026**

- Bumped JCEF from 146.0.10 to 152.0.6
- Fixed `ikonli-materialdesign2-pack` icons not showing

## v1.9.0

**23/9/2026**

- Added an integrated terminal in the sidebar using pty4j and JediTerm

## v1.8.2

**22/9/2026**

- Added fullscreen mode
- Added keybindings for fullscreen mode
- Added additional keybindings for toggling DevTools

## v1.8.1

**22/9/2026**

- Fixed tool sidebars not adapting to theme
- Fixed AI sidebar not opening via context menu for summarization and rewrite
- Added empty cache and hard reload keybind

## v1.8.0

**20/9/2026**

- Added history autosuggest alongside DuckDuckGo autocomplete
- Browser system/light/dark modes
- Refactored UI components with helper functions to adapt to the theme

## v1.7.1

**19/9/2026**

- Added an update proxy microservice in Go
- Migrated browser update manager to settings page
- Additional keybinds

## v1.7.0

**19/9/2026**

- Find tool
- Tools sidebar
- Migrated yt-dlp video downloader to tools sidebar

## v1.6.1

**19/9/2026**

- Migrated more context menu to sidebar menu

## v1.6.0

**18/9/2026**

- Browsing history
- History page
- History deletion
- Migrate from border layout and browser add and delete to card layout hide and show
- Undo commit `475ee01b2da346df1e435cacb67e4306fdbc42c8`

## v1.5.4

**17/9/2026**

- JCEF download handler for better onboarding
- Fixed grey screen issue when switching tabs by making micro adjustments to width/height

## Earlier versions

Earlier versions were not documented here. Check each releases' changelog for further details.

© 2026 (ing) Studios and Ethan Lee
