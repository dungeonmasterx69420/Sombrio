#!/usr/bin/env bash
# Installs the Sombrio APK given on the running emulator, opens it as the launcher does, and checks that a while later
# it's still running with its window in front. When it isn't, this prints the crash from logcat and fails. Leaves
# logcat.txt and screen.png.
#
#     .github/scripts/smoke-test.sh sombrio.apk [night]
#
# With "night", the emulator is in dark mode first, as most phones are.
set -uo pipefail

apk="$1"
night="${2:-}"
package=wtf.sombra.sombrio

echo "Android $(adb shell getprop ro.build.version.release) (API $(adb shell getprop ro.build.version.sdk))"
if [ "$night" = night ]; then
  adb shell cmd uimode night yes
fi
adb install -r "$apk"
adb logcat -c
adb shell monkey -p "$package" -c android.intent.category.LAUNCHER 1 > /dev/null
sleep 20

adb logcat -d -v threadtime > logcat.txt
adb shell screencap -p /sdcard/screen.png && adb pull -q /sdcard/screen.png screen.png
resumed="$(adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity|ResumedActivity:" | head -n 3)"
echo "Resumed: $resumed"

# The crash, if any: Java exceptions from AndroidRuntime, native ones from the debuggerd
crash="$(grep -n -E -A 60 "FATAL EXCEPTION|Fatal signal" logcat.txt | head -n 200)"
if [ -n "$crash" ]; then
  echo "$crash"
fi

if adb shell pidof "$package" > /dev/null && grep -q "$package" <<< "$resumed" && [ -z "$crash" ]; then
  echo "Sombrio is running, in front."
  exit 0
fi

echo "::error::Sombrio isn't running in front 20 seconds after opening it."
echo "--- What Sombrio's process and the activity manager logged:"
pid="$(grep -m 1 -oE "Start proc [0-9]+:$package" logcat.txt | grep -oE "[0-9]+" | head -n 1)"
grep -E " (AndroidRuntime|ActivityManager|ActivityTaskManager|DEBUG|libc|System\.err) *:|$package${pid:+| $pid }" logcat.txt |
  tail -n 150
exit 1
