#!/usr/bin/env bash
# Builds sombrAMP, the music player in music/sombrAMP, as a Flutter module for Sombrio's Music tab.
#
# sombrAMP is a Flutter app. Sombrio needs it as an Android library, so this copies its Dart side into
# music/module, declares that a Flutter module, and builds it as AARs: a Maven repository in
# music/module/build/host/outputs/repo, which Gradle picks up (see settings.gradle.kts). Without it, Sombrio
# builds without its Music tab.
#
#     git submodule update --init
#     music/build-module.sh
#     ./gradlew assembleRelease
#
# Needs the Flutter version sombrAMP is built with (FLUTTER_VERSION in music/sombrAMP/.github/workflows/sombramp.yml)
# and the Android SDK.
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
app="$here/sombrAMP"
module="$here/module"

if [ ! -f "$app/pubspec.yaml" ]; then
  echo "music/sombrAMP is empty: run git submodule update --init" >&2
  exit 1
fi

rm -rf "$module"
mkdir -p "$module"

# The Dart code, its assets and the files `flutter pub get` reads. Flutter generates the module's own Android project.
for path in lib assets images pubspec.yaml pubspec.lock l10n.yaml build.yaml analysis_options.yaml; do
  cp -R "$app/$path" "$module/"
done

# A module is an app whose pubspec says so.
awk '
  { print }
  /^flutter:[[:space:]]*$/ && !done {
    print "  module:"
    print "    androidX: true"
    print "    androidPackage: wtf.sombra.sombramp"
    print "    iosBundleIdentifier: wtf.sombra.sombramp"
    done = 1
  }
' "$app/pubspec.yaml" > "$module/pubspec.yaml"

cd "$module"
flutter pub get
# SOMBRIO tells sombrAMP it runs inside Sombrio (see lib/sombra/sombrio_host.dart). Only the release AAR is built: the
# debug builds of Sombrio use it too.
flutter build aar --no-debug --no-profile --dart-define=SOMBRIO=true \
  --target-platform android-arm,android-arm64,android-x64
