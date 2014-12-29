#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
#!/bin/bash
#Time=$(date -d now +%Y_%m_%d_%k%M)
#DIR_Result="$PWD/dotest"
#mkdir -p $DIR_Result$Time 
NativeFile=$(readlink -f $0)
NativeDir=$(dirname $NativeFile)


# We need to set the bin directory for adb * monkeyrunner
PPWD=$(dirname $PWD)
BIN_PATH="$PPWD/AirSDK/tools"
case "$PATH" in
		*$BIN_PATH*) echo "$ADB_PATH present" 
				;;
		*)     echo "$PATH add $BIN_PATH "
				export PATH="$PATH:$BIN_PATH"
				;;
esac

echo $PATH
#=======================In Developing======================
function KillProcess(){
#Usage: KillProcess $PID
#kill $1 && (sleep 2;kill -1 $1)&& (sleep 2; kill -9 $1)
adb shell kill -9 $1
}

function CheckTimeout(){
#Usage: CheckTimout $PID $TIME(s)
times=0
echo $times

result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
while [ $? != "0" ]
do
	times=$(($times+1))
	echo $times
	sleep 5     #single check interval
	if [ $times -gt $2 ];then
			echo "over $2 check, per check is 5s interval"
			adb shell setprop persist.sys.test ShellKillWifiIsdone
			KillProcess $1
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================


# sensor testing 
cd $NativeDir
adb install -r WifiTest.apk
PKGName="com.compal.wifitest"
TargetPKGName="com.android.settings"
# ActivityName="GpsTestCaseActivity"
Domain="wifi"
adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/TestItemConfig.xml
adb shell setprop persist.sys.test ShellInstallWifi
# adb shell am start -n $PKGName/.$ActivityName
adb shell am instrument -w $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
sleep 5
HangPid=$(adb shell ps | grep $TargetPKGName |awk '{print $2}' )
TimeOut=204
echo $HangPid
echo $TimeOut
CheckTimeout $HangPid $TimeOut

mkdir -p $NativeDir/$Domain
cd $NativeDir

adb pull /data/data/$PKGName/$Domain $Domain

adb uninstall $PKGName
exit 0
