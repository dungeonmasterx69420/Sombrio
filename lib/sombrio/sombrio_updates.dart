import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import '../l10n/app_localizations.dart';
import 'sombrio.dart';

/// Sombrio's updates: the releases the Sombrio builds workflow publishes for each build of main, tagged
/// `build-<number>`, with one APK per ABI, all signed with the same key, so Android installs them over the running
/// app. The Sombrio builds before Moonfin read the same releases.
///
/// Moonfin's update check (lib/data/services/app_update_service.dart) finds them through this, and its banner and
/// dialog install them with [install].
abstract final class SombrioUpdates {
  /// The GitHub API path of the latest release.
  static const latestReleasePath = '/repos/${Sombrio.repository}/releases/latest';

  static final _releaseTag = RegExp(r'^build-(\d+)$');
  static const _channel = MethodChannel('wtf.sombra.sombrio/updates');

  /// The build number in a release's tag, or null when it isn't a build's.
  static int? buildOf(String tag) => int.tryParse(_releaseTag.firstMatch(tag.trim())?.group(1) ?? '');

  /// Whether the release tagged [tag] is newer than this build. Builds made outside the workflow have no number and
  /// are never updated.
  static bool isNewer(String tag) {
    final build = buildOf(tag);
    return build != null && Sombrio.buildNumber > 0 && build > Sombrio.buildNumber;
  }

  /// The ABI this build is for, which its update has to be for too: the Dart VM names it in [Platform.version], as in
  /// `on "android_arm64"`.
  static String? get abi {
    final version = Platform.version;
    if (version.contains('"android_arm64"')) return 'arm64-v8a';
    if (version.contains('"android_arm"')) return 'armeabi-v7a';
    if (version.contains('"android_x64"')) return 'x86_64';
    return null;
  }

  /// Whether the release asset [name] is the APK for this device, as in `sombrio-build-130-arm64-v8a.apk`.
  static bool isApkForThisDevice(String name) {
    final abi = SombrioUpdates.abi;
    return abi != null && name.endsWith('-$abi.apk');
  }

  /// Downloads the APK at [apk], showing how far it got, then opens Android's installer for it, which first asks to
  /// allow installing apps from Sombrio if that hasn't been allowed yet.
  static Future<void> install(BuildContext context, Uri apk) async {
    final progress = ValueNotifier<double?>(null);
    final failed = ValueNotifier<bool>(false);
    final navigator = Navigator.of(context, rootNavigator: true);
    unawaited(
      showDialog<void>(
        context: context,
        useRootNavigator: true,
        barrierDismissible: false,
        builder: (context) => _InstallDialog(progress: progress, failed: failed),
      ),
    );
    try {
      final file = await _download(apk, (fraction) => progress.value = fraction);
      if (navigator.mounted) navigator.pop();
      await _channel.invokeMethod<void>('installApk', {'path': file.path});
    } catch (e) {
      debugPrint('Sombrio update failed: $e');
      failed.value = true;
    }
  }

  static Future<File> _download(Uri url, void Function(double? fraction) onProgress) async {
    // The installer reads it through the app's file provider, which shares the updates folder of the cache
    // (android/app/src/main/res/xml/update_paths.xml).
    final directory = Directory(p.join((await getTemporaryDirectory()).path, 'updates'));
    if (directory.existsSync()) directory.deleteSync(recursive: true);
    directory.createSync(recursive: true);
    final file = File(p.join(directory.path, p.basename(url.path)));

    final client = HttpClient()..connectionTimeout = const Duration(seconds: 15);
    try {
      final response = await (await client.getUrl(url)).close();
      if (response.statusCode != HttpStatus.ok) {
        throw HttpException('GitHub answered with status ${response.statusCode}', uri: url);
      }
      final total = response.contentLength;
      var received = 0;
      final sink = file.openWrite();
      try {
        await for (final chunk in response) {
          sink.add(chunk);
          received += chunk.length;
          onProgress(total > 0 ? received / total : null);
        }
      } finally {
        await sink.close();
      }
    } finally {
      client.close();
    }
    return file;
  }
}

class _InstallDialog extends StatelessWidget {
  const _InstallDialog({required this.progress, required this.failed});

  final ValueNotifier<double?> progress;
  final ValueNotifier<bool> failed;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return ValueListenableBuilder<bool>(
      valueListenable: failed,
      builder: (context, failed, _) => PopScope(
        canPop: failed,
        child: AlertDialog(
          title: Text(l10n.updating),
          content: failed
              ? Text(l10n.updateDownloadFailed)
              : ValueListenableBuilder<double?>(
                  valueListenable: progress,
                  builder: (context, progress, _) => Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      LinearProgressIndicator(value: progress),
                      if (progress != null) ...[
                        const SizedBox(height: 12),
                        Text('${(progress * 100).toStringAsFixed(0)}%'),
                      ],
                    ],
                  ),
                ),
          actions: [
            if (failed) TextButton(onPressed: () => Navigator.of(context).pop(), child: Text(l10n.close)),
          ],
        ),
      ),
    );
  }
}
