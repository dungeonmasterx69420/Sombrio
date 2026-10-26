#!/usr/bin/env bash
# Installs the Sombrio APK given on the running emulator, opens it as the launcher does, and checks that it's still
# running a while later. When it isn't, this prints the crash from logcat and fails. Leaves logcat.txt and screen.png.
#
#     .github/scripts/smoke-test.sh sombrio.apk
set -uo pipefail

apk="$1"
package=wtf.sombra.sombrio

adb install -r "$apk"
adb logcat -c
adb shell monkey -p "$package" -c android.intent.category.LAUNCHER 1
sleep 20

adb logcat -d -v threadtime > logcat.txt
adb shell screencap -p /sdcard/screen.png && adb pull /sdcard/screen.png screen.png

if adb shell pidof "$package" > /dev/null; then
  echo "Sombrio is running."
  exit 0
fi

echo "::error::Sombrio isn't running 20 seconds after opening it."
# The crash: Java exceptions from AndroidRuntime, native ones from the debuggerd
grep -n -E -A 60 "FATAL EXCEPTION|Fatal signal" logcat.txt | head -n 200
echo "--- Everything Sombrio's process and the activity manager logged:"
grep -E " (AndroidRuntime|ActivityManager|ActivityTaskManager|DEBUG|libc|flutter|System\.err) *:|$package" logcat.txt |
  tail -n 150
exit 1
