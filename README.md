<div align="center">
  <img src="images/sombrio_icon.png" alt="The sombra S mark" width="128">

  <h1>Sombrio</h1>

  Films, shows and music from your own Jellyfin server, in one app, by <a href="https://sombra.wtf">sombra</a>.
  Built on <a href="https://github.com/jarnedemeulemeester/findroid">Findroid</a> and
  <a href="https://github.com/dungeonmasterx69420/sombrAMP">sombrAMP</a>.
</div>

## What it is

Sombrio is two Sombra apps in one Android app:

- **Films and shows**: Findroid, the native Jellyfin client, redesigned in **System S**, the design system behind
  [sombra.wtf](https://sombra.wtf). Browse your libraries, play with ExoPlayer or mpv, download for offline, with
  trickplay, chapters, media segments (skip intro) and picture-in-picture.
- **Music**: [sombrAMP](https://github.com/dungeonmasterx69420/sombrAMP), sombra's music player for Jellyfin and
  Navidrome, behind the **Music** tab. It's the whole of sombrAMP: gapless playback, downloads, lyrics, Android Auto,
  and its own Layout & Theme settings.

Both sides look the same: ink on black (dark is the default) or on paper white, hard edges, extruded window shadows,
Pixelify Sans and IBM Plex Mono, and one phosphor colour that marks whatever is live.

> [!IMPORTANT]
> You **need** access to a [Jellyfin](https://jellyfin.org) server, version 10.11 or newer. Music can also come from a
> [Navidrome](https://www.navidrome.org) or other Subsonic server. Sombrio plays your own media and doesn't connect to
> any streaming service.

### One sign-in

Sign in to your Jellyfin server once, in Sombrio. The first time you open the Music tab, sombrAMP offers to **continue
as** that account: Sombrio approves a Quick Connect request for it, so the music player gets its own session without
asking for your password (Quick Connect has to be enabled on the server, which it is by default). Signing out of one
doesn't sign out the other. To use another server for music, like Navidrome, choose **Use another server** instead.

## Downloads

The **Sombrio builds** workflow (`.github/workflows/sombrio.yaml`) builds the app on every push and attaches one APK
per processor type to the run, on the repository's Actions tab. Most phones want **arm64-v8a**; older ones
armeabi-v7a, and emulators x86_64.

Builds of `main` are also published on the [Releases](https://github.com/dungeonmasterx69420/Sombrio/releases) page,
which needs no GitHub account. Once installed, Sombrio offers newer releases itself, on startup and in Settings → About
→ Check for updates: it downloads the right APK and opens Android's installer, which updates the app in place.

Releases need the `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
`ANDROID_KEY_PASSWORD` repository secrets, so that every APK is signed with the same key; without them, each build gets
a throwaway key (uninstall before installing another build) and nothing is released. The same key as sombrAMP's works
too.

Sombrio's app ID is `wtf.sombra.sombrio`, so it installs next to Findroid and sombrAMP.

## The design

Everything comes from System S, as in sombrAMP; nothing in the look is new to this app.

- **Colour.** Ink, pane and desk greys for everything, plus one phosphor (amber by default; green, cyan and pink in
  Settings → Appearance → Phosphor) for what is live: playback progress, downloads, what you've watched and liked,
  loading and focus. The names under posters are drawn in it too. System S has no red: problems are shown inverted.
- **Type.** Pixelify Sans for titles, buttons and labels; IBM Plex Mono for text and figures. Both are bundled under
  the SIL Open Font License (`core/src/main/res/font`, licences in `core/src/main/assets/fonts`).
- **Posters.** Artwork keeps its full colour, framed square on a pane with a 1dp ink edge.
- **Windows.** Home's suggestions, settings groups and the sign-in are System S windows: a pinstriped title bar and
  the extruded shadow. Home's header is a menu bar with the S mark; dialogs have the alert's deep shadow and inner
  ring.
- **Buttons** invert while pressed; the default one of a group, like Play, has the outer ring.

The System S port lives in `core/src/main/java/dev/jdtech/jellyfin/core/presentation/theme/SystemS.kt` (tokens) and
`app/phone/src/main/java/dev/jdtech/jellyfin/presentation/components/SystemS.kt` and `presentation/theme`
(Compose theme and widgets). The music side's is sombrAMP's own `lib/sombra`.

## How it's put together

Findroid is a Kotlin app and sombrAMP a Flutter app, so sombrAMP is built as a Flutter module, an Android library:

- `music/sombrAMP` is sombrAMP, as a git submodule.
- `music/build-module.sh` builds it as a Flutter module, with `--dart-define=SOMBRIO=true`, into a Maven repository in
  `music/module`.
- The `:music` module (`music/src`) holds `MusicActivity`, which the Music tab opens and sombrAMP runs in, and what
  sombrAMP's own Android app declares around it: the audio service (playback and Android Auto), the cover art
  provider and the notification icons. It also answers sombrAMP's questions about Sombrio's account.

Without the built module, Gradle leaves `:music` out and Sombrio builds without the Music tab;
`-Psombrio.music=required` makes that an error, as in CI.

## Building

You need the Android SDK and Flutter (the version in `FLUTTER_VERSION` of `.github/workflows/sombrio.yaml`):

```sh
git submodule update --init
music/build-module.sh
./gradlew :app:phone:assembleLibreDebug
```

To update sombrAMP, check out a newer commit in `music/sombrAMP` and commit the submodule.

## What hasn't changed

To keep merging upstream changes painless, the internals still carry Findroid's names: the Kotlin packages are
`dev.jdtech.jellyfin` and the theme is `FindroidTheme`. Inside, sombrAMP still carries Finamp's. The Android TV app
(`app/tv`) is Findroid's and isn't part of Sombrio's builds.

## Credits and licence

Sombrio is built on [Findroid](https://github.com/jarnedemeulemeester/findroid), the native Jellyfin app by Jarne
Demeulemeester and its contributors, licensed under the [GNU GPL v3](LICENSE), which Sombrio is too. Its music player
is [sombrAMP](https://github.com/dungeonmasterx69420/sombrAMP), built on [Finamp](https://github.com/finamp-app/finamp)
by the Finamp contributors, under the Mozilla Public License 2.0. Both play from [Jellyfin](https://jellyfin.org).
