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
#Usage: CheckTimout $TIME(s) $TotalTest(n) $SuspendDelayTime(s) $NativeDir $Domain
WaitTime=$((($1+$3)*$2+10))
echo $WaitTime

sleep $WaitTime

sudo adb kill-server
sudo adb start-server

#Check adb state
AdbState=$(adb shell getprop persist.sys.adbstate)
echo $AdbState | grep -q "0"
if [ $? -eq "0" ];then
	adb shell setprop persist.sys.adbstate "1"
fi

#enabled=$(adb shell "cat /sys/module/otg_wakelock/parameters/enabled")
#echo $enabled | grep -q "enabled"
#if [ $? != "0" ];then
#	echo $enabled | tee /$4/$5/enabled
#else
#	echo "N" > /$4/$5/enabled
#fi
}

#=======================In Developing======================
Suspend="1"

LoadConfig () {
configFile=$1
less $configFile | grep -A 1 suspend | grep True 
Result=$?
Suspend=$Result
echo Suspend is $Suspend --  0 is True, 1 is False
}

#=======================In Developing======================


# Suspend testing 
cd $NativeDir
adb install -r SuspendTest.apk
adb install -r ../checkAdb/CheckAdb.apk
PKGName="com.compal.suspendtest"
CheckAdbPKGName="com.compal.checkadb"
ActivityName="SuspendTestActivity"
Domain="suspend"
DelayTriggerTime="30"
SuspendDelayTime="0"
TotalTest="1"
EnableByShell="True"

#Disable kerguard
#adb shell sqlite3 /data/data/com.android.providers.settings/databases/settings.db "UPDATE secure set value=0 where name='device_provisioned'"

adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName

LoadConfig $NativeDir/TestItemConfig.xml

if [ $Suspend -eq "0" ];then
	adb shell setprop persist.sys.suspendtestcount "1"
	adb shell setprop persist.sys.suspendtotaltest $TotalTest
	adb shell setprop persist.sys.suspenddelaytime $SuspendDelayTime
	adb shell setprop persist.sys.suspendtriggertime $DelayTriggerTime
	adb shell setprop persist.sys.enablebyshell $EnableByShell

	adb shell setprop persist.sys.adbstate "0"
	AdbCheckTime=$((($DelayTriggerTime+$SuspendDelayTime)*$TotalTest+20))
	adb shell am startservice -n com.compal.checkadb/.CheckAdbService --ei android.intent.extras.TEST_TIME $AdbCheckTime

	adb shell setprop persist.sys.test ShellInstallSuspend
	adb shell am start -n $PKGName/.$ActivityName
	HangPid=$(adb shell ps | grep $PKGName |awk '{print $2}' )
	echo $HangPid
	echo $DelayTriggerTime

	enabled=$(adb shell "cat /sys/module/otg_wakelock/parameters/enabled")
	echo $enabled | grep -q "enabled"
	if [ $? != "0" ];then
		adb shell "echo N > /sys/module/otg_wakelock/parameters/enabled"
	fi
fi

mkdir -p $NativeDir/$Domain
result="0"

if [ $Suspend -eq "0" ];then
	CheckTimeout $DelayTriggerTime $TotalTest $SuspendDelayTime $NativeDir $Domain &

	WaitTime=$((($DelayTriggerTime+$SuspendDelayTime)*$TotalTest+25))
	echo $WaitTime

	sleep $WaitTime
	echo "over $WaitTime check time"

	enabled=$(adb shell "cat /sys/module/otg_wakelock/parameters/enabled")
	echo $enabled | grep -q "enabled"
	if [ $? != "0" ];then
		echo $enabled | tee /$NativeDir/$Domain/enabled
	else
		echo "N" > /$NativeDir/$Domain/enabled
	fi

	if [ -e $NativeDir/$Domain/enabled ];then
		grep N $NativeDir/$Domain/enabled
		result=$?
		echo $result
	else
	        result="-1";
		echo $result
	fi

	if [ $result -eq "0" ];then
		echo $enabled | grep -q "enabled"
		if [ $? != "0" ];then		
			adb shell "echo Y > /sys/module/otg_wakelock/parameters/enabled"
		fi
	
		#kill process will enable keyguard
		adb shell setprop persist.sys.test ShellKillSuspendIsdone
		KillProcess $HangPid
	else
		HangFuncPid=$(ps aux | grep "cat /sys/module/otg_wakelock/parameters/enabled" |awk '{print $2}' )
		echo $HangFuncPid
		kill -9 $HangFuncPid
	fi

	sleep 5

	rm $NativeDir/$Domain/enabled
	#mkdir -p $NativeDir/$Domain
fi

cd $NativeDir

if [ $Suspend -eq "0" ];then
	if [ $result -eq "0" ];then
		adb pull /data/data/$PKGName/$Domain $Domain
	else
		echo "<TestItem domain=\"suspend\" type=\"suspend\" description=\"suspend\">" > $NativeDir/$Domain/TestItemResult.xml
		echo "<Pass>False</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
		Id=$(less $NativeDir/TestItemConfig.xml | grep -A 2 "suspend" | grep "<ID>"| cut -f2 --delimiter='<' | cut -f2 --delimiter='>')
		echo "<ID>$Id</ID>" >> $NativeDir/$Domain/TestItemResult.xml
		LogFileName=$(echo "suspend"|tr ' ' '_')
		echo "<Log>"$LogFileName".log</Log>" >> $NativeDir/$Domain/TestItemResult.xml
		echo "<Remark1></Remark1>" >> $NativeDir/$Domain/TestItemResult.xml
		echo "<Remark2></Remark2>" >> $NativeDir/$Domain/TestItemResult.xml
		echo "<Remark3></Remark3>" >> $NativeDir/$Domain/TestItemResult.xml
		echo "</TestItem>" >> $NativeDir/$Domain/TestItemResult.xml
		echo "" >> $NativeDir/$Domain/TestItemResult.xml
		echo "Shell --> Suspend doesn't success" > $NativeDir/$Domain/$LogFileName.log
	fi
fi

#User=$(who -m | awk '{print $1}')
#echo $User

#chown -R $User:$User $Domain

if [ $result -eq "0" ];then
	adb uninstall $PKGName
	adb uninstall $CheckAdbPKGName
fi

exit 0
