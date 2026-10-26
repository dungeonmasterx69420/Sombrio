import 'package:audio_service/audio_service.dart';
import 'package:finamp/main.dart' as sombramp show startInSombrio;
import 'package:finamp/sombra/sombrio_host.dart';
import 'package:flutter/widgets.dart';
import 'package:get_it/get_it.dart';
import 'package:server_core/server_core.dart';

import '../auth/repositories/server_repository.dart';
import '../auth/repositories/session_repository.dart';
import '../auth/repositories/user_repository.dart';
import 'sombrio_audio.dart';

/// The Music tab: sombrAMP, Sombra's music player, which is the music/sombrAMP package.
///
/// sombrAMP runs in the same engine as Moonfin's screens, next to them in [SombrioShell], which shows one side at a
/// time. It's started the first time the tab is opened, and keeps running (and playing) when the films and shows are
/// shown again. Sombrio is built with `--dart-define=SOMBRIO=true`, which sombrAMP reads as [SombrioHost.isEmbedded].
class SombrioMusic implements SombrioHostDelegate {
  SombrioMusic._();

  static final instance = SombrioMusic._();

  /// Whether the music player is the side shown.
  final shown = ValueNotifier<bool>(false);

  /// sombrAMP's app, once the Music tab has been opened.
  Future<Widget>? get app => _app;
  Future<Widget>? _app;

  /// Shows the music player, starting sombrAMP the first time.
  void open() {
    assert(SombrioHost.isEmbedded, 'Sombrio is built with --dart-define=SOMBRIO=true');
    SombrioHost.delegate ??= this;
    _app ??= sombramp.startInSombrio();
    shown.value = true;
  }

  @override
  void leave() => shown.value = false;

  @override
  Future<SombrioAccount?> account() async {
    final getIt = GetIt.instance;
    if (!getIt.isRegistered<MediaServerClient>()) return null;
    final client = getIt<MediaServerClient>();
    final user = getIt<UserRepository>().currentUser;
    // sombrAMP signs in to Jellyfin servers with Quick Connect
    if (client.serverType != ServerType.jellyfin || client.accessToken == null || user == null) return null;
    final serverId = getIt<SessionRepository>().activeServerId;
    final server = getIt<ServerRepository>().servers.where((server) => server.id == serverId).firstOrNull;
    return SombrioAccount(
      baseUrl: client.baseUrl,
      userName: user.name,
      serverName: server?.name ?? Uri.tryParse(client.baseUrl)?.host ?? client.baseUrl,
    );
  }

  @override
  Future<void> authorizeQuickConnect(String code) async {
    final client = GetIt.instance<MediaServerClient>();
    final userId = GetIt.instance<UserRepository>().currentUser?.id;
    if (!await client.authApi.authorizeQuickConnect(code, userId: userId)) {
      throw StateError("The server didn't authorize sombrAMP's Quick Connect request");
    }
  }

  @override
  Future<void> attachAudioHandler(AudioHandler handler) => SombrioAudio.attach(handler);
}
