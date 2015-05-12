#!/bin/bash

# First argument is the AVD name, the rest arguments will be given to the adb for running the test App.
# Usage example: ./run-test.sh testDevice -e server_host 104.199.155.211 -e server_port 8030 -e server_host_override foo.test.google.fr -e use_tls true -e use_test_ca true -e test_case empty_unary
cd "$(dirname "$0")"

echo "[INFO] Starting emulator $1"
emulator64-arm -avd $1 -wipe-data -no-skin -no-audio -no-window -port 5554 &
./wait-for-emulator.sh
shift

echo "[INFO] Installing apks"
adb start-server
adb install -r app/build/outputs/apk/app-debug-unaligned.apk
adb install -r app/build/outputs/apk/app-debug-androidTest-unaligned.apk

echo "[INFO] Starting test"
adb shell am instrument -w "$*" io.grpc.android.integrationtest.test/android.test.InstrumentationTestRunner

adb -s emulator-5554 emu kill
