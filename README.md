gRPC Android test App

=======================

Implements gRPC integration tests in an Android App.

Connect your Android device or start the emulator:
```
$ ./start-emulator.sh <AVD name> & ./wait-for-emulator.sh
```

Manually test
-------------

Install the test App by:
```
$ ./gradlew installDebug
```
Then manually test it with the UI.


Commandline test
----------------

Build and installs the apk:
```
$ ./update-apk.sh
```

Run the test with arguments:
```
$ ./run-test.sh -e server_host <hostname or ip address> -e server_port 8030 -e server_host_override foo.test.google.fr -e use_tls true -e use_test_ca true
```