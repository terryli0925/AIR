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
#Usage: CheckTimout $PID $TIME(s) $Base(sec)
times=0
echo $times

result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
while [ $? != "0" ]
do
	times=$(($times+1))
	echo $times
	sleep $3     #single check interval
	if [ $times -gt $2 ];then
			echo "over $2 check, per check is 5s interval"
			adb shell setprop persist.sys.test ShellKillStorageIsdone
			KillProcess $1
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================

Domain="storage"
cd $NativeDir
rm -rf $Domain

StorageTest (){
cd $NativeDir
adb install -r StorageTest.apk
PKGName="com.compal.storagetest"
#TargetPKGName="com.android.settings"
TargetPKGName="com.android.gallery3d"
Domain="storage"
adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/TestItemConfig.xml
adb shell mkdir /data/data/$PKGName/$Domain
adb shell setprop persist.sys.test ShellInstallStorage   #-----
# adb shell am start -n $PKGName/.$ActivityName
adb shell am instrument -w -r $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
#sleep 2
HangPid=$(adb shell ps | grep $TargetPKGName |awk '{print $2}' )
TimeOut=1
Retry1=0
while [ -z "$HangPid" ]
do
	Retry1=$(($Retry1+1))
	echo Retry $Retry1, HangPid is null
	if [ $Retry1 -gt 10 ];then
		break
	fi
	sleep 2
	HangPid=$(adb shell ps | grep $TargetPKGName |awk '{print $2}' )
done
echo $HangPid
echo $TimeOut
if [ -z "$HangPid" ];then
	echo HangPid is null
	sleep 10
else
	CheckTimeout $HangPid $TimeOut 5
fi

mkdir -p $NativeDir/$Domain
cd $NativeDir
}

RWTest (){
cd $NativeDir
adb install -r RWTest.apk
PKG3Name="com.compal.rwtest"
Domain="storage"
Activity3Name="RWTestActivity"
adb shell rm -r /data/data/$PKG3Name/$Domain
adb shell rm /data/data/$PKG3Name/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKG3Name/TestItemConfig.xml
adb shell mkdir /data/data/$PKG3Name/$Domain
adb shell setprop persist.sys.test ShellInstallStorage   #-----
adb shell am start -n $PKG3Name/.$Activity3Name
# adb shell am instrument -w -r $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
sleep 2
HangPid=$(adb shell ps | grep $PKG3Name |awk '{print $2}' )
TimeOut=1
Retry1=0
while [ -z "$HangPid" ]
do
	Retry1=$(($Retry1+1))
	echo Retry $Retry1, HangPid is null
	if [ $Retry1 -gt 10 ];then
		break
	fi
	sleep 2
	HangPid=$(adb shell ps | grep $PKG3Name |awk '{print $2}' )
done
echo $HangPid
echo $TimeOut
if [ -z "$HangPid" ];then
	echo HangPid is null
	sleep 10
else
	CheckTimeout $HangPid $TimeOut 5
fi

}

MtpBeTest="1"

LoadConfig (){
	#Usage: LoadConfig $ConfigFileName $Type
	configFile=$1
	less $configFile | grep -A 1 $2 | grep True
	MtpBeTest=$?
	echo TestMtp is $MtpBeTest --  0 is True, 1 is False
}
OutputMtpResult (){	
	#Usage: OutputResult $Result $Domain $Type $Descr $ConfigFile
	echo "<TestItem domain=\"$2\" type=\"$3\" description=\"$4\">" >> $NativeDir/$Domain/TestItemResult.xml
	if [ $1 -eq "1" ];then
		echo "<Pass>True</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
	else
		echo "<Pass>False</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
	fi
	Id=$(less $5 | grep -A 2 "$4" | grep "<ID>"| cut -f2 --delimiter='<' | cut -f2 --delimiter='>')
	echo "<ID>"$Id"</ID>" >> $NativeDir/$Domain/TestItemResult.xml
	LogFileName=$(echo "$4"|tr ' ' '_')
	echo "<Log>"$LogFileName".log</Log>" >> $NativeDir/$Domain/TestItemResult.xml
	less $NativeDir/Tail.xml >> $NativeDir/$Domain/TestItemResult.xml
}

MtpTest (){

times=0
Domain="storage"
Type="mtp"
Des1="share internal storage by mtp"
MtpIn=1
MtpSd=1
LogFileName1=$(echo "$Des1"|tr ' ' '_')
Des2="share sd card by mtp"
LogFileName2=$(echo "$Des2"|tr ' ' '_')
mtp-detect > $NativeDir/$Domain/$LogFileName1.log
MtpStorage=$(less $NativeDir/$Domain/$LogFileName1.log | grep "StorageDescription") 
echo $MtpStorage | grep  "Internal Storage"
MtpIn=$?
echo $MtpStorage | grep  "SD Card"
MtpSd=$?
while [ $MtpIn != "0" -o $MtpSd != "0" ]
do
	times=$(($times+1))
	echo =========== retry $times
	if [ $times -gt 3 ];then
		break
	fi	
	sleep 2     #single check interval
mtp-detect >> $NativeDir/$Domain/$LogFileName1.log
MtpStorage=$(less $NativeDir/$Domain/$LogFileName1.log | grep "StorageDescription") 
echo $MtpStorage | grep  "Internal Storage"
MtpIn=$?
echo $MtpStorage | grep  "SD Card"
MtpSd=$?
done

if [ $MtpIn == "0" ];then
	echo "MtpInternal is OK"                            #write to TestItemResult.xml
	OutputMtpResult 1 $Domain $Type "$Des1" TestItemConfig.xml 
else
	echo "MtpInternal is Fail"                            #write to TestItemResult.xml
	OutputMtpResult 0 $Domain $Type "$Des1" TestItemConfig.xml
fi

cp $NativeDir/$Domain/$LogFileName1.log $NativeDir/$Domain/$LogFileName2.log
if [ $MtpSd == "0" ];then
	echo "MtpSdCard is OK"
	OutputMtpResult 1 $Domain $Type "$Des2" TestItemConfig.xml
else
	echo "MtpSdCard is Fail"
	OutputMtpResult 0 $Domain $Type "$Des2" TestItemConfig.xml
fi

#File ID: 1537
#Filename: 1330574401529.jpg
#File size 45629 (0x000000000000B23D) bytes
#Parent ID: 1528
#Storage ID: 0x00020001
#Filetype: JPEG file

MtpSdCardFileName="SdTest.txt"                        #file had been writen by StorageTest.apk
Files=$(mtp-files)
echo $Files| grep $MtpSdCardFileName
if [ $? == "0" ];then
	echo "MtpSdCard file is OK"
fi

#WrongMtpSdCardFileName="13xxxxxx30574401529.jpg"
#echo $Files| grep $WrongMtpSdCardFileName
#if [ $? == "0" ];then
#	echo "Fail: Wrong MtpSdCard file exits"
#else
#	echo "OK: Wrong MtpSdCard file does not exits"
#fi
}




UnInstallApk () {
#Usage: UnInstallApk $APKFilename Ex: UnInstallApk $NativeDir/CameraTest.apk
	RmApk=$(aapt d badging $1  | grep package | cut -f2 --delimiter="'")
    echo UnInstall $RmApk -- $1	
	adb uninstall $RmApk
}


MountTest (){

adb shell setprop persist.sys.test StartMountTest
cd $NativeDir
adb install -r MountTest.apk
ActivityName="MountTest"
PKG2Name="com.compal.mounttest"
adb shell rm -r /data/data/$PKG2Name/$Domain
adb shell rm /data/data/$PKG2Name/TestItemConfig.xml
adb shell mkdir /data/data/$PKG2Name/$Domain
adb push $NativeDir/TestItemConfig.xml /data/data/$PKG2Name/TestItemConfig.xml
echo "before intent"
adb shell am start -n $PKG2Name/.$ActivityName 
echo "after intent"
MountCheck=$(adb shell getprop persist.sys.mounttime)
echo $MountCheck
HangPid=$(adb shell ps | grep $PKG2Name |awk '{print $2}' )
Retry1=0
while [ -z "$HangPid" ]
do
	Retry1=$(($Retry1+1))
	echo Retry $Retry1, HangPid is null
	if [ $Retry1 -gt 10 ];then
		break
	fi
	sleep 2
	HangPid=$(adb shell ps | grep $PKG2Name |awk '{print $2}' )
done
echo $HangPid
if [ -z "$HangPid" ];then
	echo HangPid is null
	sleep 100
else
	CheckTimeout $HangPid 20 5 #$MountCheck 5    #use "5 x Java's time"      or sleep 20
fi
}

StorageTest
RWTest
LoadConfig $NativeDir/TestItemConfig.xml mtp
if [ $MtpBeTest -eq "0" ];then
	MtpTest
fi
echo "before MountTest"
MountTest
echo "after MountTest"


cd $NativeDir
adb pull /data/data/$PKGName/$Domain/TestItemResult.xml $Domain/TestItemResult1.xml
adb shell rm /data/data/$PKGName/$Domain/TestItemResult.xml
adb pull /data/data/$PKGName/$Domain $Domain

adb pull /data/data/$PKG2Name/$Domain/TestItemResult.xml $Domain/TestItemResult2.xml
adb shell rm /data/data/$PKG2Name/$Domain/TestItemResult.xml 
adb pull /data/data/$PKG2Name/$Domain $Domain

adb pull /data/data/$PKG3Name/$Domain/TestItemResult.xml $Domain/TestItemResult3.xml
adb shell rm /data/data/$PKG3Name/$Domain/TestItemResult.xml 
adb pull /data/data/$PKG3Name/$Domain $Domain

cat $Domain/TestItemResult1.xml >> $Domain/TestItemResult.xml
cat $Domain/TestItemResult2.xml >> $Domain/TestItemResult.xml
cat $Domain/TestItemResult3.xml >> $Domain/TestItemResult.xml

rm $Domain/TestItemResult1.xml
rm $Domain/TestItemResult2.xml
rm $Domain/TestItemResult3.xml

adb uninstall $PKGName
adb uninstall $PKG2Name
adb uninstall $PKG3Name
#UnInstallApk $NativeDir/StorageTest.apk
#UnInstallApk $NativeDir/MountTest.apk
#UnInstallApk $NativeDir/RWTest.apk
