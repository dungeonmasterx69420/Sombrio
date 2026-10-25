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

# Flutter generates the module's Android project (.android) with its own Gradle and Android Gradle plugin, newer than
# the ones sombrAMP's Android app is built with, and some of sombrAMP's plugins don't build with those. So the module is
# built with the app's, and with its fix for plugins that compile against old Android SDKs.
android="$module/.android"
cp "$app/android/gradle/wrapper/gradle-wrapper.properties" "$android/gradle/wrapper/gradle-wrapper.properties"
agp="$(sed -n -E 's/.*id "com\.android\.application" version .([0-9.]+).*/\1/p' "$app/android/settings.gradle")"
kotlin="$(sed -n -E 's/.*id "org\.jetbrains\.kotlin\.android" version .([0-9.]+).*/\1/p' "$app/android/settings.gradle")"
sed -i -E "s/(id \"com\.android\.library\" version )\"[^\"]*\"/\1\"$agp\"/" "$android/settings.gradle"
# The Kotlin Gradle plugin goes on the root project's classpath, which every plugin's build script inherits, so that
# plugins asking for an old one in their own build script (like dynamic_color's 1.5) are compiled with the app's, as in
# the app.
sed -i '/id "org\.jetbrains\.kotlin\.android" version/d' "$android/settings.gradle"
{
  cat <<GRADLE
// Added by Sombrio's music/build-module.sh, see there
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin"
    }
}

GRADLE
  cat "$android/build.gradle"
} > "$android/build.gradle.new"
mv "$android/build.gradle.new" "$android/build.gradle"
# Its Gradle properties too: some plugins use their dependencies' resources through their own R class, which needs R
# classes to be transitive.
cp "$app/android/gradle.properties" "$android/gradle.properties"
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
grep -q "version \"$agp\"" "$android/settings.gradle" || { echo "Couldn't set the Android Gradle plugin version" >&2; exit 1; }
# Newer than pubspec.yaml, so that Flutter doesn't generate it again
touch "$android"

# SOMBRIO tells sombrAMP it runs inside Sombrio (see lib/sombra/sombrio_host.dart). Only the release AAR is built: the
# debug builds of Sombrio use it too. When Gradle fails, Flutter only says so, so the output is kept and its errors
# shown.
log="$module/build-aar.log"
if ! flutter build aar --verbose --no-pub --no-debug --no-profile --dart-define=SOMBRIO=true \
  --target-platform android-arm,android-arm64,android-x64 > "$log" 2>&1; then
  grep -E -A 25 "What went wrong|^e: |error:|FAILURE:" "$log" | head -400 >&2 || true
  tail -n 60 "$log" >&2
  echo "Building sombrAMP failed, see $log" >&2
  exit 1
fi
tail -n 20 "$log"
