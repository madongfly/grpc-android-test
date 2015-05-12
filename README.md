gRPC Android test App

=======================

Implements gRPC integration tests in an Android App.

Manually test
-------------

Install the test App by:
```
$ ./gradlew installDebug
```
Then manually test it with the UI.

Commandline test
----------------

Build the apk and test App apk:
```
$ ./gradlew assemble
$ ./gradlew assembleAndroidTest
```

Install the apks:
```
$ adb install -r app/build/outputs/apk/app-debug-unaligned.apk
$ adb install -r app/build/outputs/apk/app-debug-androidTest-unaligned.apk
```

Run the test with arguments:
```
$ adb shell am instrument -w -e server_host 104.199.155.211 -e server_port 8030 -e server_host_override foo.test.google.fr -e use_tls true -e use_test_ca true io.grpc.android.integrationtest.test/android.test.InstrumentationTestRunner
```