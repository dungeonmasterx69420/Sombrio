<div align="center">
  <img src="images/sombrio_icon.png" alt="The sombra S mark" width="128">

  <h1>Sombrio</h1>

  Films, shows, Live TV and music from your own Jellyfin server, in one app, by <a href="https://sombra.wtf">sombra</a>.
  Built on <a href="https://github.com/Moonfin-Client/Moonfin-Core">Moonfin</a> and
  <a href="https://github.com/dungeonmasterx69420/sombrAMP">sombrAMP</a>.
</div>

## What it is

Sombrio is two apps in one Android app:

- **Films, shows and Live TV**: [Moonfin](https://github.com/Moonfin-Client/Moonfin-Core), the Flutter client for
  Jellyfin and Emby, in **System S**, the design system behind [sombra.wtf](https://sombra.wtf). With it come
  Moonfin's player (direct play of almost anything, trickplay, segment skipping, picture-in-picture), Live TV with a
  program guide, recordings and series timers, its admin area, Seerr requests, SyncPlay and casting.
- **Music**: [sombrAMP](https://github.com/dungeonmasterx69420/sombrAMP), sombra's music player for Jellyfin and
  Navidrome, behind the **Music** tab. It's the whole of sombrAMP: gapless playback, lyrics, Android Auto, and its own
  Layout & Theme settings.

> [!IMPORTANT]
> You **need** access to a [Jellyfin](https://jellyfin.org) server, version 10.8 or newer. Music can also come from a
> [Navidrome](https://www.navidrome.org) or other Subsonic server. Sombrio plays your own media and doesn't connect to
> any streaming service.

### The tabs

At the bottom: **Home**, **Libraries**, **Live TV** (the program guide), **Music** and **You**. Downloads have no tab;
Moonfin can still download, from an item's menu, and Settings → Appearance → Navigation puts its button back. The tabs
between Home and You can be changed there too, and the bar can be moved to the top or the side; Music has a button
there as well.

- **Live TV** opens the guide, which needs a Live TV library on the server (a tuner or an M3U list and a guide source,
  set up in the admin area below). Programs can be recorded from the guide, and the recordings, with what's scheduled,
  are a button away from it.
- **Music** shows sombrAMP in place of the films and shows, and keeps both running: each keeps its place, and music
  keeps playing while you browse films. **Films and shows**, at the top of the music screen and in its menu, goes back,
  and so does back on the music screen.
- **You** has the account, Settings, Quick Connect and the remote control.

### The admin dashboard

If your account administers the server, Settings starts with **Administration**: Moonfin's admin area, with the
dashboard (server, active sessions, running tasks, restart and shutdown), analytics, activity, scheduled tasks,
libraries and their metadata, Live TV (tuners and guide providers), users, devices, API keys, the server's
settings (playback and transcoding, streaming, trickplay, networking, branding), plugins and repositories, backups and
logs.

### One sign-in

Sign in to your Jellyfin server once, on the films and shows side. The first time you open the Music tab, sombrAMP
offers to **continue as** that account: Sombrio approves a Quick Connect request for it, so the music player gets its
own session without asking for your password (Quick Connect has to be enabled on the server, which it is by default).
To use another server for music, like Navidrome, choose **Use another server** instead.

## Downloads

The **Sombrio builds** workflow (`.github/workflows/sombrio.yaml`) builds the app on every push and attaches one APK
per processor type to the run, on the repository's Actions tab. Most phones want **arm64-v8a**; older ones
armeabi-v7a, and emulators x86_64.

Builds of `main` that open on Android emulators, and whose Music tab signs in to a test Jellyfin server, plays a
track and goes back to films and shows there (`integration_test/sombrio/music_drive.dart`), are also published on the
[Releases](https://github.com/dungeonmasterx69420/Sombrio/releases) page, which needs no GitHub account, tagged
`build-<number>`. Once installed, Sombrio offers newer releases itself, on startup and in Settings → About → Check for
updates: it downloads the right APK and opens Android's installer, which updates the app in place. The Sombrio builds
from before Moonfin read the same releases, so they update to this one too; you sign in again once, since it keeps its
settings differently.

Releases need the `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
`ANDROID_KEY_PASSWORD` repository secrets, so that every APK is signed with the same key; without them, each build gets
a throwaway key (uninstall before installing another build) and nothing is released.

Sombrio's app ID is `wtf.sombra.sombrio`, so it installs next to Moonfin and sombrAMP.

## The design

Everything comes from System S, as in sombrAMP: ink on black, hard edges and square corners, and one phosphor, amber,
for what is live (playback progress, what you've watched, focus, the current tab). System S has no red: problems are
shown in ink. Titles, buttons and labels are set in Pixelify Sans, and text and figures in IBM Plex Mono, sombrAMP's
fonts (SIL Open Font License). It's Moonfin's default theme, **System S** in Settings → Appearance, next to Moonfin's
Neon Pulse, Glass and 8-bit Hero.

## How it's put together

Moonfin and sombrAMP are both Flutter apps, and Sombrio is one Flutter app made of both:

- The repository is Moonfin-Core, merged with its history, with Sombrio's changes on top. What Sombrio adds lives in
  `lib/sombrio`, and Moonfin's own files only call into it, each such place marked `Sombrio:`.
- `music/sombrAMP` is sombrAMP, as a git submodule, and a package of the app (its package is still named `finamp`).
  The app is built with `--dart-define=SOMBRIO=true`, which tells sombrAMP it runs inside Sombrio.
- `lib/sombrio/sombrio_shell.dart` holds Moonfin's app and sombrAMP's side by side, shows one at a time, and sends the
  back button to the one shown. `sombrio_music.dart` starts sombrAMP when the Music tab is first opened and answers
  its questions about the account. `sombrio_audio.dart` is the one audio service both players play through: the
  notification, lock screen and Android Auto follow whichever played last.
- `lib/sombrio/sombrio_updates.dart` installs Sombrio's releases. The Android side of sombrAMP (its channels, cover art
  provider and notification icons) is in `android/app/src/main/kotlin/wtf/sombra/sombrio` and the app's resources.

To bring in a newer Moonfin:

```sh
git fetch https://github.com/Moonfin-Client/Moonfin-Core main
git merge FETCH_HEAD
```

## Building

You need Flutter (the version in `FLUTTER_VERSION` of `.github/workflows/sombrio.yaml`) and the Android SDK and NDK:

```sh
git submodule update --init
(cd music/sombrAMP && flutter gen-l10n)   # sombrAMP's translations, which pub doesn't generate for a package
flutter build apk --flavor mobile --split-per-abi --dart-define=SOMBRIO=true
```

To update sombrAMP, check out a newer commit in `music/sombrAMP` and commit the submodule.

## What hasn't changed

To keep merging Moonfin painless, the internals still carry its names: the Dart package is `moonfin`, the Kotlin
package `org.moonfin.androidtv`, and System S is the theme with Moonfin's id. Inside, sombrAMP still carries Finamp's.
Only the phone app (the `mobile` flavor) is built; Moonfin's TV, iOS and desktop apps are still in the repository but
aren't Sombrio's.

## Credits and licence

Sombrio is built on [Moonfin](https://github.com/Moonfin-Client/Moonfin-Core) by the Moonfin contributors, which is
free software under the GNU General Public License, version 2 or (at your option) any later version, as Sombrio is (see
[LICENSE](LICENSE) for the version 2 text). Its music player is [sombrAMP](https://github.com/dungeonmasterx69420/sombrAMP),
built on [Finamp](https://github.com/finamp-app/finamp) by the Finamp contributors, under the Mozilla Public License
2.0. Both play from [Jellyfin](https://jellyfin.org). Sombrio is an independent project and isn't affiliated with the
Moonfin, Jellyfin or Emby projects.
