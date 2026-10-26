// Drives the Music tab against a real Jellyfin server, as someone opening it for the first time would: starts Sombrio,
// signs in to the server on the films and shows side, opens the Music tab, continues there as the same account, plays
// a track from the Tracks tab, then goes back to films and shows. It prints what it does to the log as "DRIVE: ..."
// lines, "DRIVE: SHOT <name>" where a screenshot tells the most, and last "DRIVE: PASS" or "DRIVE: FAIL <why>". Errors
// it doesn't see, like sombrAMP's services missing, are in the rest of the log.
//
// The server needs a user "tester" with the password "test" and a music library whose tracks include one named
// "Track 2" (.github/scripts/jellyfin-test-server.sh sets one up). Built as the app's entry point:
//
//     flutter build apk --flavor mobile --target-platform android-x64 -t integration_test/sombrio/music_drive.dart \
//       --dart-define=SOMBRIO=true --dart-define=DRIVE_URL=http://10.0.2.2:8096
//
// on an emulator (.github/scripts/music-check.sh), or `flutter build linux` the same way and run under Xvfb.
import 'dart:async';

import 'package:finamp/components/global_snackbar.dart';
import 'package:finamp/menus/components/icon_button_with_semantics.dart';
import 'package:finamp/services/finamp_user_helper.dart';
import 'package:finamp/services/queue_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:moonfin/auth/models/login_state.dart';
import 'package:moonfin/auth/repositories/auth_repository.dart';
import 'package:moonfin/auth/repositories/server_repository.dart';
import 'package:moonfin/auth/repositories/session_repository.dart';
import 'package:moonfin/data/services/media_server_client_factory.dart';
import 'package:moonfin/main.dart' as app;
import 'package:moonfin/sombrio/sombrio_music.dart';
import 'package:moonfin/sombrio/sombrio_shell.dart';
import 'package:moonfin/ui/navigation/app_router.dart';
import 'package:moonfin/ui/navigation/destinations.dart';

const _serverUrl = String.fromEnvironment('DRIVE_URL', defaultValue: 'http://127.0.0.1:8096');
const _username = 'tester';
const _password = 'test';

void main() {
  app.main();
  unawaited(_drive().catchError((Object error, StackTrace trace) => _say('FAIL $error\n$trace')));
}

void _say(String message) => debugPrint('DRIVE: $message');

/// Asks for a screenshot, and waits while it's taken.
Future<void> _shot(String name) async {
  _say('SHOT $name');
  await _pause(3);
}

Future<void> _drive() async {
  final controller = LiveWidgetController(WidgetsBinding.instance);
  await _waitFor('the app', () => WidgetsBinding.instance.rootElement != null && _isShown(find.byType(SombrioShell)));
  await _pause(5);

  // Films and shows: signs in as the sign-in screen does
  final server = await GetIt.instance<ServerRepository>().addServer(_serverUrl);
  if (server == null) return _say('FAIL no server at $_serverUrl');
  final client = GetIt.instance<MediaServerClientFactory>().getClient(
    serverId: server.id,
    serverType: server.serverType,
    baseUrl: server.address,
  );
  final result = await GetIt.instance<AuthRepository>().authenticate(
    client: client,
    serverId: server.id,
    username: _username,
    password: _password,
  );
  if (result is! Authenticated) return _say('FAIL signing in to films and shows: $result');
  await GetIt.instance<SessionRepository>().switchCurrentSession(
    serverId: result.serverId,
    userId: result.userId,
    username: _username,
    password: _password,
  );
  appRouter.go(Destinations.home);
  await _pause(5);
  await _shot('films');

  // The Music tab, the first time: sombrAMP starts, and offers to continue as the account films and shows use
  _say('opening the Music tab');
  SombrioMusic.instance.open();
  await _waitFor('the music player', () => GlobalSnackbar.navigatorState != null);
  _say('queue service: ${GetIt.instance.isRegistered<QueueService>()}');
  final continueAs = find.textContaining('Continue as');
  await _waitFor('"Continue as"', () => _isShown(continueAs));
  await _shot('music-sign-in');
  await controller.tap(continueAs.first);
  await _waitFor('the music library', () => GetIt.instance<FinampUserHelper>().currentUser?.currentView != null);
  final tracksTab = find.text('Tracks');
  await _waitFor('the Tracks tab', () => _isShown(tracksTab));
  await _pause(3);
  await _shot('music');

  await controller.tap(tracksTab.first);
  final track = find.text('Track 2');
  await _waitFor('"Track 2"', () => _isShown(track));
  await _pause(2);
  await controller.tap(track.first);
  final queue = GetIt.instance<QueueService>();
  await _waitFor('a track playing', () => queue.getCurrentTrack() != null);
  await _pause(5);
  await _shot('playing');
  final playing = queue.getCurrentTrack()?.item.title;
  _say('playing: $playing');

  // The way back
  final back = find.byWidgetPredicate((widget) => widget is IconButtonWithSemantics && widget.label == 'Films and shows');
  if (_isShown(back)) {
    await controller.tap(back.first);
    await _pause(3);
    await _shot('back');
  }
  final wentBack = !SombrioMusic.instance.shown.value;
  _say('back on films and shows: $wentBack');

  if (playing == null) return _say('FAIL nothing played');
  if (!wentBack) return _say('FAIL "Films and shows" didn\'t go back');
  _say('PASS');
}

bool _isShown(Finder finder) => finder.evaluate().isNotEmpty;

Future<void> _pause(int seconds) => Future<void>.delayed(Duration(seconds: seconds));

Future<void> _waitFor(String what, bool Function() done, {int seconds = 90}) async {
  final end = DateTime.now().add(Duration(seconds: seconds));
  while (!done()) {
    if (DateTime.now().isAfter(end)) throw TimeoutException('No $what after $seconds seconds');
    await _pause(1);
  }
  _say('got $what');
}
