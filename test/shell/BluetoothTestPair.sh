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
adb shell kill -9 $1
}

function CheckTimeout(){
#Usage: CheckTimout $PID $TIME(s)
times=0
echo $times
NeedPair=$(cat $NativeDir/TestItemConfig.xml | grep -n1 "pair and discovery" | grep True )
echo $NeedPair | grep -q "True"
NeedToPair=$?
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
while [ $? != "0" ]
do
	times=$(($times+1))
	echo $times
	sleep 5     #single check interval
	
	TempScanResult=$(adb shell getprop persist.sys.test_scannedresult)
	ScanResult=$(echo $TempScanResult | tr '\r ' ' ')
	TempScanFlag=$(adb shell getprop persist.sys.test_scanflag)
	ScanFlag=$(echo $TempScanFlag | tr '\r' ' ')
	if [ "$NeedToPair" == "0" ] && [ "$ScanFlag" == "true " ] && [ "$ScanResult" == "false " ];then
		echo "scan device under test"
		DutScanned=$(hcitool scan | grep $DutBtAddr)
		if [ $? == "0" ];then
			echo "device is be scanned"
			adb shell setprop persist.sys.test_scannedresult DutBeScanned
		fi
	fi
	
	TempPairFlag=$(adb shell getprop persist.sys.test_pairflag)
	PairFlag=$(echo $TempPairFlag | tr '\r' ' ')
	if [ "$NeedToPair" == "0" ] && [ "$PairFlag" == "true " ];then
		echo "pair request"
		/etc/init.d/bluetooth restart
		sleep 3
		echo $DutBtAddr
		/usr/bin/bluetooth-agent hci0 $DutBtAddr
		sleep 3
#		adb shell setprop persist.sys.test_pairflag false
	fi

	if [ $times -gt $2 ];then
			echo "over $2 check, per check is 5s interval"
			adb shell setprop persist.sys.test ShellKillBluetoothPairIsdone
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
adb install -r BluetoothTestPair.apk
PKGName="com.compal.bluetoothtestpair"
TargetPKGName="com.android.settings"
# ActivityName="GpsTestCaseActivity"
Domain="bluetooth"
NbBtAddr=$(hcitool dev | grep hci0 |awk '{print $2}' )
adb shell setprop persist.sys.test_nbaddr $NbBtAddr
adb shell setprop persist.sys.test_dutaddr false
adb shell setprop persist.sys.test_pairflag false 
adb shell setprop persist.sys.test_scanflag false
adb shell setprop persist.sys.test_scannedresult false 
adb shell setprop persist.sys.test ShellInstallBluetoothPair
adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/TestItemConfig.xml
# adb shell am start -n $PKGName/.$ActivityName
adb shell am instrument -w $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
sleep 5
startcount=0
count=30
DutAddr=$(adb shell getprop persist.sys.test_dutaddr)
echo $DutAddr | grep -q "false"
#while [ $? == "0"  ] && [ $s -lt $c ]
while [ $startcount -lt $count ]
do
	startcount=$(($startcount+1))
	sleep 1
	DutAddr=$(adb shell getprop persist.sys.test_dutaddr)
	echo $DutAddr | grep -q ":"
	if [ $? == "0" ];then
		echo $DutAddr
		break
	fi
done
DutBtAddr=$(echo $DutAddr | tr '\r' ' ')
echo $DutBtAddr

HangPid=$(adb shell ps | grep $TargetPKGName |awk '{print $2}' )
TimeOut=30
echo $HangPid
echo $TimeOut

CheckTimeout $HangPid $TimeOut

mkdir -p $NativeDir/$Domain
cd $NativeDir

adb shell mv /data/data/$PKGName/$Domain/TestItemResult.xml /data/data/$PKGName/$Domain/TestItemResultPair.xml
adb pull /data/data/$PKGName/$Domain $Domain

adb uninstall $PKGName
#exit 0
