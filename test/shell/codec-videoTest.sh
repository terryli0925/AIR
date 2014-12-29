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
			adb shell setprop persist.sys.test ShellKillCodecVideoIsdone
			KillProcess $1
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================
video_3gp_h263="1"
video_3gp_h264_avc="1"
video_3gp_mpeg4_sp="1"
video_mp4_h263="1"
video_mp4_h264_avc="1"
video_mp4_mpeg4_sp="1"

LoadConfig () {
configFile=$1
less $configFile | grep -A 1 h263_3gp | grep True 
Result=$?
video_3gp_h263=$Result
echo video_3gp_h263 is $video_3gp_h263 --  0 is True, 1 is False

less $configFile | grep -A 1 h264_avc_3gp | grep True 
Result=$?
video_3gp_h264_avc=$Result
echo video_3gp_h264_avc is $video_3gp_h264_avc --  0 is True, 1 is False

less $configFile | grep -A 1 mpeg-4_sp_3gp | grep True 
Result=$?
video_3gp_mpeg4_sp=$Result
echo video_3gp_mpeg4_sp is $video_3gp_mpeg4_sp --  0 is True, 1 is False

less $configFile | grep -A 1 h263_mp4 | grep True 
Result=$?
video_mp4_h263=$Result
echo video_mp4_h263 is $video_mp4_h263 --  0 is True, 1 is False

less $configFile | grep -A 1 h264_avc_mp4 | grep True 
Result=$?
video_mp4_h264_avc=$Result
echo video_mp4_h264_avc is $video_mp4_h264_avc --  0 is True, 1 is False

less $configFile | grep -A 1 mpeg-4_sp_mp4 | grep True 
Result=$?
video_mp4_mpeg4_sp=$Result
echo video_mp4_mpeg4_sp is $video_mp4_mpeg4_sp --  0 is True, 1 is False
}
#=======================In Developing======================

# codec video testing 
cd $NativeDir
adb install -r CodecVideoTest.apk
PKGName="com.compal.codecvideotest"
# ActivityName="CodecVideoTestActivity"
Domain="codec-video"
#TestItem="6"
TestItem="0"
adb shell rm -r /data/data/$PKGName/$Domain/
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/

LoadConfig $NativeDir/TestItemConfig.xml

if [ $video_3gp_h263 -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $video_3gp_h264_avc -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $video_3gp_mpeg4_sp -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $video_mp4_h263 -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $video_mp4_h264_avc -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $video_mp4_mpeg4_sp -eq "0" ];then
TestItem=$(($TestItem+1))
fi
echo $TestItem

if [ $TestItem -gt "0" ];then
adb shell setprop persist.sys.test ShellInstallCodecVideo
# adb shell am start -n $PKGName/.$ActivityName
adb shell am instrument -w $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
sleep 5

HangPid=$(adb shell ps | grep $PKGName |awk '{print $2}' )
TimeOut=$((($TestItem*30)/5+2))
echo $HangPid
echo $TimeOut
CheckTimeout $HangPid $TimeOut
fi

mkdir -p $NativeDir/$Domain
cd $NativeDir

if [ $TestItem -gt "0" ];then
adb pull /data/data/$PKGName/$Domain $Domain
fi

adb uninstall $PKGName
exit 0
