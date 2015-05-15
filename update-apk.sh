#!/bin/bash -e

# This script updates the gRPC Android test app with the current gRpc-java HEAD.
cd "$(dirname "$0")"
echo "[INFO] Building gRpc HEAD"
git clone --depth 1 https://github.com/grpc/grpc-java.git
cd grpc-java/core && ../gradlew install
cd ../stub && ../gradlew install
cd ../okhttp && ../gradlew install
cd ../protobuf-nano && ../gradlew install
cd ../.. && rm -rf grpc-java

echo "[INFO] Building Apk"
git pull origin master
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest

echo "[INFO] Installing Apk"
adb -e install -r app/build/outputs/apk/app-debug-unaligned.apk
adb -e install -r app/build/outputs/apk/app-debug-androidTest-unaligned.apk
