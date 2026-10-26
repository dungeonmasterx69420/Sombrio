import 'package:flutter_test/flutter_test.dart';
import 'package:moonfin/data/models/aggregated_library.dart';
import 'package:moonfin/preference/user_preferences.dart';
import 'package:moonfin/util/live_tv_library.dart';

AggregatedLibrary _lib(String id, String type) =>
    AggregatedLibrary(id: id, name: id, collectionType: type, serverId: 's1');

void main() {
  test('the live tv button starts on', () {
    // Sombrio: Live TV has a tab of its own, which only shows when the server
    // has a Live TV library
    expect(UserPreferences.showLiveTvButton.defaultValue, isTrue);
  });

  test('the guide has one way in, whichever way the button is', () {
    // Turning the button off puts the library tile back rather than taking
    // Live TV out of the nav, so there is always exactly one way in.
    final libraries = [_lib('movies', 'movies'), _lib('guide', 'livetv')];

    expect(
      librariesForNav(
        libraries,
        UserPreferences.showLiveTvButton.defaultValue,
      ).map((lib) => lib.id),
      ['movies'],
    );
    expect(
      librariesForNav(libraries, false).map((lib) => lib.id),
      ['movies', 'guide'],
    );
  });
}
