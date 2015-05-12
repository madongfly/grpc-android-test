#!/bin/bash
echo "Waiting for emulator to start..."

bootanim=""
failcounter=0
until [[ "$bootanim" =~ "stopped" ]]; do
   bootanim=`adb -e shell getprop init.svc.bootanim 2>&1`
   if [[ "$bootanim" =~ "not found" ]]; then
      let "failcounter += 1"
      if [[ $failcounter -gt 3 ]]; then
        echo "Can not find device"
        exit 1
      fi
      echo "Device was not found, try again..."
   fi
   sleep 1
done

