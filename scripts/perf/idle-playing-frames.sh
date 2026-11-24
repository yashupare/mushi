#!/usr/bin/env bash
# Regression check: frames the app renders while music plays and nothing on screen changes.
#
# Plays from the mini player, leaves Home idle for 10 s with the full player collapsed, and prints
# the frames rendered. Expected: about 40 (the mini player's progress line, 4 position ticks a
# second). Far more means something animates or recomposes continuously in the background, as
# when the playback position was part of PlayerUiState or the progress bar sprang to every tick
# (159 frames before the fix).
#
# Needs: adb on PATH, one device or emulator, the app installed, something in the queue. The tap
# coordinates assume a 1080 x 2400 screen (the mini player's play button). On Git Bash for
# Windows, export MSYS_NO_PATHCONV=1 first.
set -euo pipefail
PKG="${PKG:-com.mushi.app}"
adb shell am force-stop "$PKG"
adb shell am start -W -n "$PKG/com.mushi.app.MainActivity" >/dev/null
sleep 8
adb shell input tap 970 2078   # play
sleep 4
adb shell dumpsys gfxinfo "$PKG" reset >/dev/null
sleep 10
adb shell dumpsys gfxinfo "$PKG" | grep -E "Total frames rendered"
adb shell input tap 970 2078   # pause
