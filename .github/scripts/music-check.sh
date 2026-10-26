#!/usr/bin/env bash
# Installs the APK built from integration_test/sombrio/music_drive.dart on the running emulator and opens it, which
# signs in to the test server (jellyfin-test-server.sh), opens the Music tab, plays a track and goes back to films and
# shows. Follows its log until it passes or fails, and fails as well when sombrAMP's services went missing on the way.
# Leaves music-logcat.txt, and music-<step>.png for the steps the driver asks screenshots of.
#
#     .github/scripts/music-check.sh music-drive.apk
set -uo pipefail

apk="$1"
package=wtf.sombra.sombrio

echo "Android $(adb shell getprop ro.build.version.release) (API $(adb shell getprop ro.build.version.sdk))"
adb shell cmd uimode night yes
# With its runtime permissions granted, so that Android 13 and up don't ask for notifications in front of it
adb install -r -g "$apk"
adb logcat -c
adb shell monkey -p "$package" -c android.intent.category.LAUNCHER 1 > /dev/null

verdict=""
for _ in $(seq 1 360); do
  sleep 1
  adb logcat -d -s flutter:I > drive.txt
  for shot in $(grep -o "DRIVE: SHOT [a-z-]*" drive.txt | awk '{print $3}'); do
    [ -f "music-$shot.png" ] || adb exec-out screencap -p > "music-$shot.png"
  done
  verdict="$(grep -o "DRIVE: \(PASS\|FAIL\).*" drive.txt | head -n 1)"
  [ -n "$verdict" ] && break
  if ! adb shell pidof "$package" > /dev/null; then
    verdict="Sombrio stopped"
    break
  fi
done
adb exec-out screencap -p > music-last.png
adb logcat -d -v threadtime > music-logcat.txt

grep -o "DRIVE: .*" drive.txt
missing="$(grep -c "is not registered inside GetIt" music-logcat.txt)"
if [ "$missing" != 0 ]; then
  echo "::error::sombrAMP's services were missing $missing times:"
  grep -m 5 -A 12 "is not registered inside GetIt" music-logcat.txt
  exit 1
fi
if [ "$verdict" != "DRIVE: PASS" ]; then
  echo "::error::The music check didn't pass: ${verdict:-no verdict after 6 minutes}"
  grep -E " (AndroidRuntime|flutter) *:" music-logcat.txt | tail -n 100
  exit 1
fi
echo "The Music tab signs in, plays and goes back."
