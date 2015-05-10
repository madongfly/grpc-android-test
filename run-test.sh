#!/bin/bash -e
cd "$(dirname "$0")"
./gradlew assemble
./gradlew assembleAndroidTest

echo "[INFO] Installing apks"
adb install -r app/build/outputs/apk/app-debug-unaligned.apk
adb install -r app/build/outputs/apk/app-debug-androidTest-unaligned.apk

echo "[INFO] Starting test"
adb shell am instrument -w -e server_host 104.199.154.228 -e server_port 8030 io.grpc.android.integrationtest.test/android.test.InstrumentationTestRunner