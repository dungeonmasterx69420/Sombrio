/// Sombrio, the Sombra app for films, shows, Live TV and music, built on Moonfin.
///
/// What Sombrio adds to Moonfin lives in this folder: the Music tab (sombrAMP, the music/sombrAMP package, in
/// [SombrioShell] next to Moonfin's screens), the audio service both share, System S, and updates from Sombrio's
/// releases. Moonfin's own files only call into it, so that newer Moonfin versions merge in easily.
library;

abstract final class Sombrio {
  /// The app's name, as the user sees it.
  static const name = 'Sombrio';

  /// The GitHub repository Sombrio is built and released from.
  static const repository = 'dungeonmasterx69420/Sombrio';

  /// The number of this build: the versionCode of the APKs the Sombrio builds workflow publishes, as `build-<number>`.
  /// Zero in builds made elsewhere, which don't look for updates.
  static const buildNumber = int.fromEnvironment('SOMBRIO_BUILD');
}
