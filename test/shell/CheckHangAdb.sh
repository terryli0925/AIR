#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
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

times=0
echo $times
LL=10000

echo "$1"
while [ $? != "10000" ]
do
	#PIDs1st=$(ps aux | grep adb | grep "getprop persist.sys.test" | awk '{print $2}')
	PIDs1st=$(ps aux | grep adb | grep "$1" | awk '{print $2}')
	for Pid1st in $PIDs1st
	do
		echo $Pid1st
		sleep 3
		#XXX=$(ps aux | grep adb | grep "getprop persist.sys.test" | awk '{print $2}')
		XXX=$(ps aux | grep adb | grep "$1" | awk '{print $2}')
		echo $XXX | grep $Pid1st
		if [ $? == "0" ];then
			echo $Pid1st is hang
			kill -9 $Pid1st
		fi
	done	
	#sleep 1
# if [ $times -gt $LL ];then
#	 break;
# fi
done
