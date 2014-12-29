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
			adb shell setprop persist.sys.test ShellKillSensorIsdone
			KillProcess $1
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================
UnInstallApk () {
	#Usage: UnInstallApk $APKFilename Ex: UnInstallApk $NativeDir/CameraTest.apk
	RmApk=$(aapt d badging $1  | grep package | cut -f2 --delimiter="'")
	echo UnInstall $RmApk -- $1 
	adb uninstall $RmApk
}

# sensor testing 
cd $NativeDir
adb install -r SensorTest.apk
PKGName="com.compal.sensortest"
ActivityName="SensorTest"
Domain="sensors"
adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
#adb shell mkdir /data/data/$PKGName/$Domain
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/TestItemConfig.xml
adb shell setprop persist.sys.test ShellInstallSensor
adb shell am start -n $PKGName/.$ActivityName

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
HangPid=$(adb shell ps | grep $PKGName |awk '{print $2}' )
TimeOut=6
echo $HangPid
echo $TimeOut
CheckTimeout $HangPid $TimeOut

mkdir -p $NativeDir/$Domain
cd $NativeDir

adb pull /data/data/$PKGName/$Domain $Domain
UnInstallApk $NativeDir/SensorTest.apk
