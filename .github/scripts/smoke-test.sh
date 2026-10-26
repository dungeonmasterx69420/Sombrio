#!/usr/bin/env bash
# Installs the Sombrio APK given on the running emulator, opens it as the launcher does, and checks that it's still
# running with its window in front a while later. When it isn't, this prints the crash from logcat and fails. Leaves
# logcat.txt and screen.png.
#
#     .github/scripts/smoke-test.sh sombrio.apk [night]
#
# With "night", the emulator is in dark mode first, as most phones are.
#
# The Music tab, sombrAMP, runs in the same Flutter engine as the rest of the app and only opens once signed in, so
# opening the app is as far as this gets without a server.
set -uo pipefail

apk="$1"
night="${2:-}"
package=wtf.sombra.sombrio
activity=MainActivity

echo "Android $(adb shell getprop ro.build.version.release) (API $(adb shell getprop ro.build.version.sdk))"
if [ "$night" = night ]; then
  adb shell cmd uimode night yes
fi
adb install -r "$apk"

adb logcat -c
adb shell monkey -p "$package" -c android.intent.category.LAUNCHER 1 > /dev/null

sleep 30
adb logcat -d -v threadtime > logcat.txt
adb shell screencap -p /sdcard/screen.png && adb pull /sdcard/screen.png screen.png > /dev/null
resumed="$(adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity|ResumedActivity:" | head -n 3)"
echo "Resumed: $resumed"

# The crash, if any: Java exceptions from AndroidRuntime, native ones from the debuggerd, and Dart ones Flutter logs
crash="$(grep -n -E -A 60 "FATAL EXCEPTION|Fatal signal" logcat.txt | head -n 200)"
if [ -n "$crash" ]; then
  echo "$crash"
fi
dart_errors="$(grep -E " flutter *:.*(Unhandled Exception|Error:|Exception:)" logcat.txt | head -n 40)"
if [ -n "$dart_errors" ]; then
  echo "--- Errors the Dart side logged (not failing the test):"
  echo "$dart_errors"
fi

if adb shell pidof "$package" > /dev/null && grep -q "$package/.*$activity" <<< "$resumed" && [ -z "$crash" ]; then
  echo "Sombrio is running, in front."
  exit 0
fi

echo "::error::Sombrio isn't running in front 30 seconds after opening it."
echo "--- What Sombrio's process and the activity manager logged:"
grep -E " (AndroidRuntime|ActivityManager|ActivityTaskManager|DEBUG|libc|flutter|System\.err) *:|$package" logcat.txt |
  tail -n 150
exit 1
