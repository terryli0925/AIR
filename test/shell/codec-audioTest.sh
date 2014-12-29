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
			adb shell setprop persist.sys.test ShellKillCodecAudioIsdone
			KillProcess $1
			break
	fi	
result=$(adb shell getprop persist.sys.test)
echo $result | grep -q "done"
done
}

#=======================In Developing======================
audio_aac_lc_ltp="1"
audio_aacplus="1"
audio_amr_nb="1"
audio_amr_wb="1"
audio_mp3="1"
audio_midi="1"
audio_ogg="1"
audio_pcm_wave="1"

LoadConfig () {
configFile=$1
less $configFile | grep -A 1 aac_lc_ltp | grep True 
Result=$?
audio_aac_lc_ltp=$Result
echo audio_aac_lc_ltp is $audio_aac_lc_ltp --  0 is True, 1 is False

less $configFile | grep -A 1 aac+ | grep True 
Result=$?
audio_aacplus=$Result
echo audio_aacplus is $audio_aacplus --  0 is True, 1 is False

less $configFile | grep -A 1 amr-nb | grep True 
Result=$?
audio_amr_nb=$Result
echo audio_amr_nb is $audio_amr_nb --  0 is True, 1 is False

less $configFile | grep -A 1 amr-wb | grep True 
Result=$?
audio_amr_wb=$Result
echo audio_amr_wb is $audio_amr_wb --  0 is True, 1 is False

less $configFile | grep -A 1 mp3 | grep True 
Result=$?
audio_mp3=$Result
echo audio_mp3 is $audio_mp3 --  0 is True, 1 is False

less $configFile | grep -A 1 midi | grep True 
Result=$?
audio_midi=$Result
echo audio_midi is $audio_midi --  0 is True, 1 is False

less $configFile | grep -A 1 ogg | grep True 
Result=$?
audio_ogg=$Result
echo audio_ogg is $audio_ogg --  0 is True, 1 is False

less $configFile | grep -A 1 pcm_wave | grep True 
Result=$?
audio_pcm_wave=$Result
echo audio_pcm_wave is $audio_pcm_wave --  0 is True, 1 is False
}
#=======================In Developing======================

# codec audio testing 
cd $NativeDir
adb install -r CodecAudioTest.apk
PKGName="com.compal.codecaudiotest"
# ActivityName="CodecAudioTestActivity"
Domain="codec-audio"
#TestItem="14"
TestItem="0"
adb shell rm -r /data/data/$PKGName/$Domain/
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/

LoadConfig $NativeDir/TestItemConfig.xml

if [ $audio_aac_lc_ltp -eq "0" ];then
TestItem=$(($TestItem+3))
fi

if [ $audio_aacplus -eq "0" ];then
TestItem=$(($TestItem+3))
fi

if [ $audio_amr_nb -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $audio_amr_wb -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $audio_mp3 -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $audio_midi -eq "0" ];then
TestItem=$(($TestItem+3))
fi

if [ $audio_ogg -eq "0" ];then
TestItem=$(($TestItem+1))
fi

if [ $audio_pcm_wave -eq "0" ];then
TestItem=$(($TestItem+1))
fi
echo $TestItem

if [ $TestItem -gt "0" ];then
adb shell setprop persist.sys.test ShellInstallCodecAudio
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
