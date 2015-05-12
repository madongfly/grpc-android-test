#!/bin/bash -e
cd "$(dirname "$0")"

echo "[INFO] Starting emulator $1"
emulator64-arm -avd $1 -no-skin -no-audio -no-window -port 5554 &
./wait-for-emulator.sh

echo "[INFO] Installing apks"
adb install -r app/build/outputs/apk/app-debug-unaligned.apk
adb install -r app/build/outputs/apk/app-debug-androidTest-unaligned.apk

echo "[INFO] Starting test"
adb shell am instrument -w -e server_host 104.199.155.211 -e server_port 8030 -e server_host_override foo.test.google.fr -e use_tls true -e use_test_ca true io.grpc.android.integrationtest.test/android.test.InstrumentationTestRunner

adb -s emulator-5554 emu kill
