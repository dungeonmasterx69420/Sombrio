#!/usr/bin/env bash
# Installs the Sombrio APK given on the running emulator, opens it as the launcher does, then opens the Music tab
# (sombrAMP, on the Flutter engine), and checks that each is still running with its window in front a while later.
# When one isn't, this prints the crash from logcat and fails. Leaves logcat.txt, logcat-music.txt, screen.png and
# screen-music.png.
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

# check <what> <name of its activity> <file suffix>: waits, then checks that the activity is in front and nothing
# crashed since logcat was last cleared
check() {
  local what="$1" activity="$2" suffix="$3"
  sleep 20
  adb logcat -d -v threadtime > "logcat$suffix.txt"
  adb shell screencap -p /sdcard/screen.png && adb pull /sdcard/screen.png "screen$suffix.png" > /dev/null
  local resumed crash
  resumed="$(adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity|ResumedActivity:" | head -n 3)"
  echo "$what, resumed: $resumed"

  # The crash, if any: Java exceptions from AndroidRuntime, native ones from the debuggerd
  crash="$(grep -n -E -A 60 "FATAL EXCEPTION|Fatal signal" "logcat$suffix.txt" | head -n 200)"
  if [ -n "$crash" ]; then
    echo "$crash"
  fi

  if adb shell pidof "$package" > /dev/null && grep -q "$activity" <<< "$resumed" && [ -z "$crash" ]; then
    echo "$what is running, in front."
    return 0
  fi

  echo "::error::$what isn't running in front 20 seconds after opening it."
  echo "--- What Sombrio's process and the activity manager logged:"
  grep -E " (AndroidRuntime|ActivityManager|ActivityTaskManager|DEBUG|libc|flutter|System\.err) *:|$package" \
    "logcat$suffix.txt" | tail -n 150
  return 1
}

adb logcat -c
adb shell monkey -p "$package" -c android.intent.category.LAUNCHER 1 > /dev/null
check Sombrio "$package" "" || exit 1

adb logcat -c
adb shell am start -n "$package/wtf.sombra.sombrio.music.MusicActivity" > /dev/null
check "The Music tab" MusicActivity -music || exit 1
