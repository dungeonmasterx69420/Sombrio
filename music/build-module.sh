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

# A module is an app whose pubspec says so. It leaves out integration_test, which the module's plugin registrant would
# register although release builds don't include it, so they wouldn't compile.
awk '
  /^  integration_test:/ { skip = 1; next }
  skip && /^    / { next }
  { skip = 0; print }
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

# Flutter generates the module's Android project (.android) with its own Gradle and Android Gradle plugin, newer than
# the ones sombrAMP's Android app is built with, and some of sombrAMP's plugins don't build with those. So the module is
# built with the app's, and with its fix for plugins that compile against old Android SDKs.
android="$module/.android"
cp "$app/android/gradle/wrapper/gradle-wrapper.properties" "$android/gradle/wrapper/gradle-wrapper.properties"
agp="$(sed -n -E 's/.*id "com\.android\.application" version .([0-9.]+).*/\1/p' "$app/android/settings.gradle")"
kotlin="$(sed -n -E 's/.*id "org\.jetbrains\.kotlin\.android" version .([0-9.]+).*/\1/p' "$app/android/settings.gradle")"
# Kotlin goes in the same plugins block as Flutter's plugin loader, which brings its own, older Kotlin Gradle plugin:
# there Gradle picks the newer of the two, and every plugin is compiled with it, also those asking for an older one in
# their own build script. Declared anywhere else, Flutter's would win.
sed -i -E \
  -e "s/(id \"com\.android\.library\" version )\"[^\"]*\"/\1\"$agp\"/" \
  -e "s/(id \"org\.jetbrains\.kotlin\.android\" version )\"[^\"]*\"/\1\"$kotlin\"/" \
  "$android/settings.gradle"
# Its Gradle properties too: some plugins use their dependencies' resources through their own R class, which needs R
# classes to be transitive.
cp "$app/android/gradle.properties" "$android/gradle.properties"
# Nothing of this build stays running afterwards: its Gradle daemon (with the app's 8 GB heap) and Kotlin daemon would
# keep that memory from the Sombrio build that follows.
cat >> "$android/gradle.properties" <<'PROPERTIES'
org.gradle.daemon=false
kotlin.compiler.execution.strategy=in-process
PROPERTIES
cat >> "$android/build.gradle" <<'GRADLE'

// Added by Sombrio's music/build-module.sh, as in sombrAMP's android/build.gradle: some plugins compile against Android
// SDKs older than their dependencies need.
subprojects {
    afterEvaluate { project ->
        if (project.plugins.hasPlugin("com.android.application") ||
                project.plugins.hasPlugin("com.android.library")) {
            project.android {
                compileSdkVersion 36
            }
        }
    }
}
GRADLE
grep -q "id \"com\.android\.library\" version \"$agp\"" "$android/settings.gradle" ||
  { echo "Couldn't set the Android Gradle plugin version" >&2; exit 1; }
grep -q "id \"org\.jetbrains\.kotlin\.android\" version \"$kotlin\"" "$android/settings.gradle" ||
  { echo "Couldn't set the Kotlin version" >&2; exit 1; }
# Newer than pubspec.yaml, so that Flutter doesn't generate it again
touch "$android"

# SOMBRIO tells sombrAMP it runs inside Sombrio (see lib/sombra/sombrio_host.dart). Only the release AAR is built: the
# debug builds of Sombrio use it too. When Gradle fails, Flutter only says so, so the output is kept and its errors
# shown.
log="$module/build-aar.log"
if ! flutter build aar --verbose --no-pub --no-debug --no-profile --dart-define=SOMBRIO=true \
  --target-platform android-arm,android-arm64,android-x64 > "$log" 2>&1; then
  # The Kotlin errors, and what Gradle says went wrong. Dokka, which generates the javadoc, complains about every
  # library compiled with a newer Kotlin than its own, harmlessly, so those lines are left out.
  grep -v "expected version is 1\.4\.2" "$log" | grep -E -B 2 -A 12 "^\s*e: |What went wrong|compileReleaseKotlin FAILED" |
    head -n 250 >&2 || true
  echo "Building sombrAMP failed, see $log" >&2
  exit 1
fi
tail -n 20 "$log"
