#!/bin/bash

# All arguments will be given to the adb for running the test App.
# Usage example: ./run-test.sh -e server_host <hostname or ip address> -e server_port 8030 -e server_host_override foo.test.google.fr -e use_tls true -e use_test_ca true -e test_case empty_unary
echo "[INFO]: Running gRpc Android test"
adb -e shell am instrument -w $* io.grpc.android.integrationtest.test/android.test.InstrumentationTestRunner
