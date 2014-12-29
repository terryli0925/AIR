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
while [ $? != "0" -a $times -le $2 ]
do
	times=$(($times+1))
	echo $times
	#adb shell chmod 4755 /dev/rtc0
	sleep 5     #single check interval
	if [ $times -gt $2 ];then
			echo "over $2 check, per check is 5s interval"
			adb shell setprop persist.sys.test ShellKillColdBootIsdone
			KillProcess $1
			success="flase"
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================


# cold boot testing 
cd $NativeDir
PKGName="com.compal.coldboottest"
ActivityName="ColdBootTestActivity"
Domain="coldboot"
TotalTest="1"

adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb install -r ColdBootTest.apk
adb remount
adb push ./ColdBootTest /system/bin/
adb shell chmod 4755 /system/bin/ColdBootTest
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/

#set property
adb shell setprop persist.sys.bootcycle ""
adb shell setprop persist.sys.totalbootcycle ""
adb shell setprop persist.sys.bootdelay "10"
adb shell setprop persist.sys.test ""

adb shell am start -n $PKGName/.$ActivityName

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
HangPid=$(adb shell ps | grep $PKGName |awk '{print $2}' )
TimeOut=$(($TotalTest*25))
#TimeOut=5
echo $HangPid
echo $TimeOut
success="true"
CheckTimeout $HangPid $TimeOut $success

mkdir -p $NativeDir/$Domain
cd $NativeDir

if [ $success = "true" ];then
	adb pull /data/data/$PKGName/$Domain $Domain
	adb uninstall $PKGName
else
	echo "<TestItem domain=\"coldboot\" type=\"cold boot\" description=\"cold boot\">" > $NativeDir/$Domain/TestItemResult.xml
	echo "<Pass>False</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
	Id=$(less $NativeDir/TestItemConfig.xml | grep -A 2 "cold boot" | grep "<ID>"| cut -f2 --delimiter='<' | cut -f2 --delimiter='>')
	echo "<ID>$Id</ID>" >> $NativeDir/$Domain/TestItemResult.xml
	LogFileName=$(echo "cold boot"|tr ' ' '_')
	echo "<Log>"$LogFileName".log</Log>" >> $NativeDir/$Domain/TestItemResult.xml
	echo "<Remark1></Remark1>" >> $NativeDir/$Domain/TestItemResult.xml
	echo "<Remark2></Remark2>" >> $NativeDir/$Domain/TestItemResult.xml
	echo "<Remark3></Remark3>" >> $NativeDir/$Domain/TestItemResult.xml
	echo "</TestItem>" >> $NativeDir/$Domain/TestItemResult.xml
	echo "" >> $NativeDir/$Domain/TestItemResult.xml
	echo "Shell --> Cold boot doesn't success" > $NativeDir/$Domain/$LogFileName.log
	#cp ./ColdBootTestFalseResult/TestItemResult.xml $Domain
	#cp ./ColdBootTestFalseResult/Boot.log $Domain
fi

exit 0
