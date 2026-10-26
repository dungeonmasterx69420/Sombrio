import 'dart:async';

import 'package:finamp/components/global_snackbar.dart';
import 'package:finamp/sombra/system_s.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../ui/navigation/app_router.dart';
import 'sombrio_music.dart';

/// The whole app: Moonfin's screens ([films]) and, once the Music tab has been opened, sombrAMP's, each its own app
/// with its own navigation, of which one is shown at a time. Both stay running, so each keeps its place and the music
/// keeps playing while films and shows are shown.
///
/// Both apps listen for the back button, and Flutter asks them in the order they started listening, so the shell
/// listens first and sends it to the side that is shown: sombrAMP goes back through its own pages, then to films and
/// shows; Moonfin handles it as it would on its own. Neither side's pages follow Android's predictive back gesture
/// (Moonfin's page transitions don't, and sombrAMP turns them off inside Sombrio), since a hidden page would follow
/// it too, so the gesture comes here as a back press as well.
class SombrioShell extends StatefulWidget {
  const SombrioShell({super.key, required this.films});

  final Widget films;

  @override
  State<SombrioShell> createState() => _SombrioShellState();
}

class _SombrioShellState extends State<SombrioShell> with WidgetsBindingObserver {
  final _music = SombrioMusic.instance;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _music.shown.addListener(_onSideChanged);
  }

  @override
  void dispose() {
    _music.shown.removeListener(_onSideChanged);
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  void _onSideChanged() => setState(() {});

  @override
  Future<bool> didPopRoute() async {
    // Until the music player has been opened, Moonfin is the only app listening
    if (_music.app == null) return false;

    if (_music.shown.value) {
      final musicNavigator = GlobalSnackbar.navigatorState;
      if (musicNavigator != null && await musicNavigator.maybePop()) return true;
      _music.leave();
      return true;
    }

    // Moonfin's own handling, which sombrAMP's navigator, listening after it, mustn't get as well
    final moonfin = appRouter.backButtonDispatcher;
    if (moonfin is RootBackButtonDispatcher && await moonfin.didPopRoute()) return true;
    unawaited(SystemNavigator.pop());
    return true;
  }

  @override
  Widget build(BuildContext context) {
    final musicShown = _music.shown.value;
    final musicApp = _music.app;
    return Directionality(
      textDirection: TextDirection.ltr,
      child: IndexedStack(
        index: musicShown ? 1 : 0,
        sizing: StackFit.expand,
        children: [
          _Side(shown: !musicShown, child: widget.films),
          _Side(
            shown: musicShown,
            child: musicApp == null
                ? const SizedBox.shrink()
                : FutureBuilder<Widget>(
                    future: musicApp,
                    builder: (context, snapshot) => snapshot.data ?? const _MusicStarting(),
                  ),
          ),
        ],
      ),
    );
  }
}

/// One of the two apps. The hidden one doesn't animate or take the focus, which a keyboard or remote moves.
class _Side extends StatelessWidget {
  const _Side({required this.shown, required this.child});

  final bool shown;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return TickerMode(
      enabled: shown,
      child: ExcludeFocus(excluding: !shown, child: child),
    );
  }
}

/// What the Music tab shows while sombrAMP starts, which takes a moment the first time: System S's ink, and the
/// phosphor spinning.
class _MusicStarting extends StatelessWidget {
  const _MusicStarting();

  @override
  Widget build(BuildContext context) {
    return ColoredBox(
      color: SystemSPalette.dark.desk,
      child: Center(
        child: SizedBox.square(
          dimension: 28,
          child: CircularProgressIndicator(strokeWidth: 2, color: SystemSPhosphor.amber.glow),
        ),
      ),
    );
  }
}
