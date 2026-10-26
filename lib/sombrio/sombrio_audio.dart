import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/painting.dart';

/// The app's one audio service, which Moonfin's player and sombrAMP's both play through.
///
/// audio_service can only be started once per app, so both players are handlers behind a [SwitchAudioHandler]: the
/// media notification, the lock screen, headset buttons and Android Auto control whichever of them started playing
/// last.
abstract final class SombrioAudio {
  static final _switch = SwitchAudioHandler();
  static final _attached = <AudioHandler>{};
  static Future<void>? _started;

  // Moonfin's configuration (lib/playback/audio_handler.dart), with the S mark in the notification.
  static final _config = AudioServiceConfig(
    androidNotificationChannelId: 'wtf.sombra.sombrio.audio',
    androidNotificationChannelName: 'Playback',
    androidNotificationOngoing: false,
    // Must stay true. When false, audio_service holds its partial wake lock and the media playback foreground service
    // forever after a pause, which is what makes Android warn about the app draining the battery.
    androidStopForegroundOnPause: true,
    notificationColor: const Color(0xFF0A0A0A),
    androidNotificationIcon: 'mipmap/white',
    androidBrowsableRootExtras: {
      AndroidContentStyle.supportedKey: true,
      AndroidContentStyle.browsableHintKey: AndroidContentStyle.categoryListItemHintValue,
      AndroidContentStyle.playableHintKey: AndroidContentStyle.listItemHintValue,
    },
  );

  static Future<void> _start() => _started ??= AudioService.init(builder: () => _switch, config: _config);

  /// Starts the service if it isn't yet, then plays [handler] through it. Until one of them plays, the service shows
  /// the first handler attached.
  static Future<void> attach(AudioHandler handler) async {
    await _start();
    if (!_attached.add(handler)) return;
    if (_attached.length == 1) _switch.inner = handler;
    handler.playbackState.map((state) => state.playing).distinct().listen((playing) {
      if (playing && !identical(_switch.inner, handler)) _switch.inner = handler;
    });
  }

  /// In place of [AudioService.init], for Moonfin's player: builds its handler and attaches it. [config] is
  /// Moonfin's, which the service already has the equivalent of.
  static Future<T> init<T extends AudioHandler>({required T Function() builder, AudioServiceConfig? config}) async {
    await _start();
    final handler = builder();
    await attach(handler);
    return handler;
  }
}
