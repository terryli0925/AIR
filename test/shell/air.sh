#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
#!/bin/bash

#Usage: ./air.sh $Image.tar.bz2 $AIR.zip $ConfigFile

#Time=$(date -d now +%Y_%m_%d_%k%M)
#DIR_Result="$PWD/dotest"
#mkdir -p $DIR_Result$Time 
NativeFile=$(readlink -f $0)
NativeDir=$(dirname $NativeFile)


# We need to set the bin directory for adb * monkeyrunner
ADB_PATH="$PWD/air/AirSDK/tools"
case "$PATH" in
		*$ADB_PATH*) echo "ADB_PATH present" 
				;;
		*)     echo "$PATH add $ADB_PATH "
				export PATH="$PATH:$ADB_PATH"
				;;
esac

echo $PATH
#===========================================================
Arg1=$1
Arg2=$2
Arg3=$3

#====== unTar Image

unTarImage (){
# QBJP000.0.0012.tar.bz2 
echo "=======================" $Arg1
#tar jxvf $Arg1
unzip $Arg1
}
unTarImage

#====== unTar air.zip

unTarAir (){
#######unzip air.zip   # zip -r air.zip air/
rm -rf air
unzip $Arg2   # zip -r air.zip air/
}
unTarAir

exec > >(tee -a $NativeDir/air/screen.log)

#====== Setup TestItemConfig.xml
SetUpTools (){
if [ -f $Arg3 ]; then
	echo "$Arg3 exists, adopt it"
	TestOrder=$(less $Arg3 | grep domain | cut -f2 --delimiter='=' | cut -f2 --delimiter='"' | uniq)
else
	echo"$Arg3 don't exist, use default"
	TestOrder=$(less "$NativeDir"/TestItemConfig.xml | grep domain | cut -f2 --delimiter='=' | cut -f2 --delimiter='"' | uniq)
fi	
echo $TestOrder
for TestDomain in $TestOrder
do
	echo $TestDomain"Test"
	cp $NativeDir/TestItemConfig.xml $NativeDir/air/$TestDomain"Test"/TestItemConfig.xml
	ls $NativeDir/air/$TestDomain"Test"/TestItemConfig.xml
	cp -a $NativeDir/air/tools $NativeDir/air/$TestDomain"Test"/
	ls $NativeDir/air/$TestDomain"Test"/tools/bin/

done
}
SetUpTools

#====== Burn Image
BurnImage (){

#===== QBJP0 =======
#TarFileList=$(tar jtvf $Arg1| awk '{print $6}')
#NewDir=$(echo $TarFileList | grep "burn_to_emmc.sh" | cut -f1 -d/)
#echo "==========" $NewDir
#cd $NativeDir/$NewDir
#./burn_to_emmc.sh
#==================

#===== V0JET =======
ZipFileList=$(unzip -l $Arg1 | awk '{print $4}'|grep -v "Name")
NewDir=$(echo $ZipFileList |grep "flash.sh" |cut -f1 -d/| cut -f2 --delimiter=' ')
echo "==========" $NewDir
cd $NativeDir/$NewDir
chmod +x -R *
./flash.sh
#==================
sleep 1

i=0
adb kill-server
adb start-server
Check=$($NativeDir/air/AirSDK/tools/adb devices)
echo $Check | grep "device$"
until [ "$?" == "0" ]
do
sleep 10s
echo "no devices 10s $i"
i=$(($i+1))
if [ "$i" == "10" ]; then
echo "Ten time - redo ./fastboot reboot"
./fastboot reboot
fi
adb kill-server
adb start-server
Check=$($NativeDir/air/AirSDK/tools/adb devices)
echo $Check | grep "device$"
done
echo "find devices"

for (( count1=1; count1<=5; count1=count1+1 ))
#for count1 in $(seq 1 5)
do
	echo "sleep 10 $count1"
	sleep 10s
done
}
BurnImage

#====== Start Test
StartTest (){
$NativeDir/air/AirSDK/tools/adb shell sqlite3 /data/data/com.android.providers.settings/databases/settings.db "UPDATE secure set value=0 where name='device_provisioned'" 

$NativeDir/air/AirSDK/tools/adb reboot
for (( count2=1; count2<=5; count2=count2+1 ))
#for count2 in $(seq 1 5)
do
                echo "sleep 10 $count2"
                sleep 10s
done

sleep 8
cd $NativeDir

./air/CheckHangAdb.sh "getprop persist.sys.test" &
./air/CheckHangAdb.sh "uninstall" &
adb logcat > $NativeDir/air/logcat.log &
for TestDomain in $TestOrder
do
	echo "TestDomain"
	cd $NativeDir
	chmod +x $NativeDir/air/$TestDomain"Test"/$TestDomain"Test".sh
	./air/$TestDomain"Test"/$TestDomain"Test".sh
done
}
StartTest
AirPid=$(ps aux | grep "air.sh"|grep -v grep |awk '{print $2}')
echo Old AirPid is $AirPid
for xPid in $AirPid
do
        echo kill -9 $xPid
        kill -9 $xPid
done
