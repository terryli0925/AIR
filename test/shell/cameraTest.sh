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
ADB_PATH="$PPWD/AirSDK/tools"
case "$PATH" in
		*$ADB_PATH*) echo "ADB_PATH present" 
				;;
		*)     echo "$PATH add $ADB_PATH "
				export PATH="$PATH:$ADB_PATH"
				;;
esac

echo $PATH
#=======================In Developing======================
function KillProcess(){
#Usage: KillProcess $PID
#kill $1 && (sleep 2;kill -1 $1)&& (sleep 2; kill -9 $1)
adb shell kill -9 $1
}

TargetPKGName="com.android.camera"
function CheckTimeout(){
#Usage: CheckTimout $PID $TIME(s)
times=0
echo $times

result=$(adb shell ps)
echo $result | grep -q $TargetPKGName
while [ $? == "0" ]   #check com.android.camera exists 0:Yes
do
	times=$(($times+1))
	echo $times
	sleep 5     #single check interval
	if [ $times -gt $2 ];then
			echo "over $2 check, per check is 5s interval"
			KillProcess $1
			break
	fi	
result=$(adb shell ps)
echo $result | grep -q $TargetPKGName
done
}

#=======================In Developing======================
ReturnPass="0"
ReturnFail="1"
convert_jpg2tif () {
	convert $1 -compress lzw $2
}
convert_png2tif () {
	convert $1 -compress none $2
}
OCR_image () {
	tesseract $1 $2
	String=$(less $2'.txt')
	echo $String
	Codes=$3
	SumCodes=0
	SumPass=0
	CodesNum=$(echo $Codes | tr ":" " " | wc -w)
	echo CodesNum is $CodesNum
	for mIndex in $(seq 1 $CodesNum)
	do
		Code=$(echo $Codes|cut -f $mIndex --delimiter=:)
		echo $Code 
		NumCode=$(echo $String | grep -o $Code | wc -l)
		echo $NumCode
		SumCodes=$(($SumCodes+$NumCode))
		if [ $NumCode -gt $4 ];then
			echo $Code has $NumCode ">" $4 PASS
			SumPass=$(($SumPass+1))
		else
			echo $Code has $NumCode "<" $4 FAIL
			#return $ReturnFail #1   # 1: Fail
		fi		
	done

	if [ $SumPass -lt $(($CodesNum-1)) ];then
		return $ReturnFail #1   # 1: Fail
	fi
	if [ $SumPass -eq "0" ];then
		return $ReturnFail #1   # 1: Fail
	fi
		
	echo "TestWell"
	return $ReturnPass #0   # 0: PASS
}

CheckPass () {
#if [ $1 -eq "1" ];then
if [ $1 -eq $ReturnPass ];then
	echo $2 "is Pass"
	return $ReturnPass
else
	echo $2 "is Fail"
	return $ReturnFail
fi
}

FCamera="1"
RCamera="1"

LoadConfig () {
configFile=$1
less $configFile | grep -A 1 "front camera" | grep True 
Result=$?
FCamera=$Result
echo FCamera is $FCamera --  0 is True, 1 is False

less $configFile | grep -A 1 "rear camera" | grep True
Result=$?
RCamera=$Result
echo RCamera is $RCamera -- 0 is True, 1 is False
}

OutputResult () {
	#Usage: OutputResult $Result $Domain $Type $Descr $ConfigFile
	echo "<TestItem domain=\"$2\" type=\"$3\" description=\"$4\">" >> $NativeDir/$Domain/TestItemResult.xml
if [ $1 -eq "0" ];then
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

UnInstallApk () {
#Usage: UnInstallApk $APKFilename Ex: UnInstallApk $NativeDir/CameraTest.apk
	RmApk=$(aapt d badging $1  | grep package | cut -f2 --delimiter="'")
    echo UnInstall $RmApk -- $1	
	adb uninstall $RmApk
}
#==========Camera preview testing start =====================
cd $NativeDir
LoadConfig $NativeDir/TestItemConfig.xml

if [ $FCamera -eq "1" ] && [ $RCamera -eq "1" ]; then
	echo "No Camera Test"
	exit 0
fi

if [ $FCamera -eq "0" ];then
adb shell am start -n com.android.camera/.Camera --ei android.intent.extras.CAMERA_FACING 1
#$BIN_PATH/monkeyrunner xFrontCameraPreview.py
monkeyrunner xFrontCameraPreview.py
fi

if [ $RCamera -eq "0" ];then
adb shell am start -n com.android.camera/.Camera --ei android.intent.extras.CAMERA_FACING 0
#$BIN_PATH/monkeyrunner xBackCameraPreview.py 
monkeyrunner xBackCameraPreview.py 
fi

if [ $FCamera -eq "0" ];then
convert FrontCameraPreview.png -crop 1000x800+0+0 FrontCameraPreview1.png #remove the control & previous pic region from Preview image
convert_png2tif FrontCameraPreview1.png FrontCameraPreview.tif
fi

if [ $RCamera -eq "0" ];then
convert BackCameraPreview.png -crop 1000x800+0+0 BackCameraPreview1.png   #remove the control & previous pic region from Preview image
convert_png2tif BackCameraPreview1.png  BackCameraPreview.tif
fi
	KEY="A"
	PassNumKey="3"
	#CODES="AHI:MOT:UVW:XY"
	#CODES="AHI:HAI:TAIX:XO:XO:YOHO:XWY:WXY"  # M => IVI, W => VV, I => \
	#CODES="AHO:HAO:VOTO:XO:HX:YOHO:XUY:UXY"
	CODES="ATU:TAU:XU:XUY:UXY"
	#        6:  6:15:6  :6
	#PassNumKeyZoomMax="1"
	#CODESZoomMax="XO:HX"
	PassNumKeyZoomMax="0"
	CODESZoomMax="XU"


Domain="camera"
cd $NativeDir
rm -rf $Domain
mkdir -p $NativeDir/$Domain

if [ $FCamera -eq "0" ];then
Type="front camera"
Des=$Type" preview"
LogFileName=$(echo "$Des"|tr ' ' '_')
OCR_image FrontCameraPreview.tif FrontCameraPreview $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log
less $NativeDir/$Domain/$LogFileName.log | grep TestWell
CheckPass $? "Front Preview"
Result=$?
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi

if [ $RCamera -eq "0" ];then
Type="rear camera"
Des=$Type" preview"
LogFileName=$(echo "$Des"|tr ' ' '_')
OCR_image BackCameraPreview.tif BackCameraPreview $CODES $PassNumKey  | tee $NativeDir/$Domain/$LogFileName.log
less $NativeDir/$Domain/$LogFileName.log | grep TestWell
CheckPass $? "Rear Preview"
Result=$?
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi

#==========Camera preview testing done =====================


# camera testing 
cd $NativeDir
adb install -r CameraTest.apk 
PKGName="com.compal.cameratest"
ActivityName="CameraTest"
Domain="camera"
adb shell rm -r /data/data/$PKGName/$Domain
adb shell rm /data/data/$PKGName/TestItemConfig.xml
adb shell rm /sdcard/DCIM/Camera/*
adb push $NativeDir/TestItemConfig.xml /data/data/$PKGName/
#adb shell setprop persist.sys.test ShellInstall$Domain
#adb shell am start -n $PKGName/.$ActivityName
adb shell am instrument -w -r $PKGName/android.test.InstrumentationTestRunner &

#/adb shell ps | grep usb_otg |awk '{print $2}' | xargs /adb shell kill
sleep 3
HangPid=$(adb shell ps | grep $TargetPKGName |awk '{print $2}' )
TimeOut=13
echo $HangPid
echo $TimeOut
CheckTimeout $HangPid $TimeOut

cd $NativeDir
adb pull /sdcard/DCIM/Camera/ $Domain
#adb uninstall $PKGName
UnInstallApk $NativeDir/CameraTest.apk

ImgList=$(ls $Domain | sort |grep jpg) #sort for distingush front / back
fileindex="1"
RearFileIndex="0"
FrontFileIndex="0"
if [ $RCamera -eq "0" ];then   # test Rear camera
	RearFileIndex="1"
	if [ "$FCamera" -eq "0" ];then   # test Front camera
			FrontFileIndex="2"
	else
			FrontFileIndex="0"      # do NOT test Front camera
	fi
fi

if [ "$RCamera" -eq "1" ];then   # do NOT test Rear camera
	RearFileIndex="0"
	if [ "$FCamera" -eq "0" ];then   # test Front camera
			FrontFileIndex="1"
	else
			FrontFileIndex="0"      # do NOT test Front camera
	fi
fi

cd $NativeDir/$Domain
for File in $ImgList
do
	if [ "$fileindex" -eq "$RearFileIndex" ];then
		echo "1"
		convert_jpg2tif $File $fileindex.tif
		Type="rear camera"
		Des=$Type" snapshot"
		LogFileName=$(echo "$Des"|tr ' ' '_')
		OCR_image $fileindex.tif $fileindex $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log
		less $NativeDir/$Domain/$LogFileName.log | grep TestWell
		CheckPass $? "Rear Pic"
		Result=$?
		OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
	fi
	if [ "$fileindex" -eq "$FrontFileIndex" ];then
		echo "2"
		convert_jpg2tif $File $fileindex.tif
		Type="front camera"
		Des=$Type" snapshot"
		LogFileName=$(echo "$Des"|tr ' ' '_')
		OCR_image $fileindex.tif $fileindex $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log
		less $NativeDir/$Domain/$LogFileName.log | grep TestWell
		CheckPass $? "Front Pic"
		Result=$?
		OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
	fi
	echo $fileindex
	fileindex=$(($fileindex+1))
done
#===========================================================

#========check CameraVideo playback=========
#DeviceSDDir="/mnt/ext_sdcard/Air/"
#adb push 2.mp4 $DeviceSDDir    # one is back
#adb push 2.mp4 $DeviceSDDir    # two is front

#CTSDir="$NativeDir/tools/android-cts/tools/"
#export CTS_ROOT="$NativeDir/tools/"
#cd $CTSDir
#cts-tradefed run cts --package android.aircameramedia | tee AirCameraMediaResult
#less AirCameraMediaResult| grep "android\.aircameramedia\.cts\.AirMediaPlayerTest"

GenerateVideoFrame (){
FileList=$(ls $NativeDir/$Domain | sort |grep $1)  #sort for distingush front / back
FileListCount=$(ls $NativeDir/$Domain | sort |grep $1| wc -l)
fileindex=1
for File in $FileList
do
	if [ $fileindex -eq $RearFileIndex ];then
		ffmpeg -i $File -r 1 -f image2 back-%03d.jpg
	fi
	if [ $fileindex -eq $FrontFileIndex ];then
		ffmpeg -i $File -r 1 -f image2 front-%03d.jpg
	fi
	fileindex=$(($fileindex+1))
done

}
GenerateVideoFrame "mp4"
GenerateVideoFrame "3gp"

if [ $RCamera -eq "0" ];then
convert_jpg2tif back-001.jpg BackVideo.tif
Type="rear camera"
Des=$Type" recording"
LogFileName=$(echo "$Des"|tr ' ' '_')
OCR_image BackVideo.tif  BackVideo $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log
less $NativeDir/$Domain/$LogFileName.log | grep TestWell
CheckPass $? "Rear Video"
Result=$?
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi
if [ $FCamera -eq "0" ];then
convert_jpg2tif front-001.jpg FrontVideo.tif
Type="front camera"
Des=$Type" recording"
LogFileName=$(echo "$Des"|tr ' ' '_')
OCR_image FrontVideo.tif  FrontVideo $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log
less $NativeDir/$Domain/$LogFileName.log | grep TestWell
CheckPass $? "Front Video"
Result=$?
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi

#===========================================
cd $NativeDir

CTSPackageDir="$NativeDir/tools/android-cts/repository/testcases"
ExtStorageDir="/mnt/sdcard"
adb install -r  $CTSPackageDir/CtsTestStubs.apk
adb install -r  $CTSPackageDir/CtsHardwareTestCases.apk

if [ $FCamera -eq "0" ];then
adb shell rm $ExtStorageDir/test.jpg #for AirHugeTest
adb shell am instrument -w -e class android.hardware.cts.CameraTest#testTakePicFrontSmoothMinZoom com.android.cts.hardware/android.test.InstrumentationTestRunner
adb shell sync
sleep 1                  # for fix the blank image
sleep 5
Type="front camera"
Des=$Type" zoom in and zoom out"
LogFileName=$(echo "$Des"|tr ' ' '_')

adb pull $ExtStorageDir/test.jpg $NativeDir/$Domain/FrontMinZoomPic.jpg
PullResult=$?
if [ $PullResult -eq "0" ];then
	convert_jpg2tif  $NativeDir/$Domain/FrontMinZoomPic.jpg $NativeDir/$Domain/FrontMinZoomPic.tif
	OCR_image $NativeDir/$Domain/FrontMinZoomPic.tif $NativeDir/$Domain/FrontMinZoomPic $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log1
	less $NativeDir/$Domain/$LogFileName.log1 | grep TestWell
	CheckPass $? "FrontMinZoomPic"
	Result1=$?
else
	echo "Pull Fail"
	CheckPass $PullResult "FrontMinZoomPic"
	Result1=$?
fi
#Type="FrontCamera"
#Des="ZoomOut"
#OutputResult $Result $Domain $Type $Des
#exit 0

adb shell rm $ExtStorageDir/test.jpg #for AirHugeTest
adb shell am instrument -w -e class android.hardware.cts.CameraTest#testTakePicFrontSmoothMaxZoom com.android.cts.hardware/android.test.InstrumentationTestRunner
adb shell sync
sleep 1
sleep 5
adb pull $ExtStorageDir/test.jpg $NativeDir/$Domain/FrontMaxZoomPic.jpg
PullResult=$?
if [ $PullResult -eq "0" ];then
	convert_jpg2tif  $NativeDir/$Domain/FrontMaxZoomPic.jpg $NativeDir/$Domain/FrontMaxZoomPic.tif
	OCR_image $NativeDir/$Domain/FrontMaxZoomPic.tif $NativeDir/$Domain/FrontMaxZoomPic $CODESZoomMax $PassNumKeyZoomMax | tee $NativeDir/$Domain/$LogFileName.log2
	less $NativeDir/$Domain/$LogFileName.log2 | grep TestWell
	CheckPass $? "FrontMaxZoomPic"
	Result2=$?
else
	echo "Pull Fail"
	CheckPass $PullResult "FrontMaxZoomPic"
	Result2=$?
fi
cat $NativeDir/$Domain/$LogFileName.log1 >> $NativeDir/$Domain/$LogFileName.log
cat $NativeDir/$Domain/$LogFileName.log2 >> $NativeDir/$Domain/$LogFileName.log
#Type="FrontCamera"
#Des="ZoomIn"
#OutputResult $Result $Domain $Type $Des

if [ "$Result1" == "1" ] || [ "$Result2" == "1" ]; then
	Result=1
else
	Result=0
fi
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi

if [ $RCamera -eq "0" ];then
adb shell rm $ExtStorageDir/test.jpg #for AirHugeTest
adb shell am instrument -w -e class android.hardware.cts.CameraTest#testTakePicRearSmoothMinZoom com.android.cts.hardware/android.test.InstrumentationTestRunner
adb shell sync
sleep 1
sleep 5
Type="rear camera"
Des=$Type" zoom in and zoom out"
LogFileName=$(echo "$Des"|tr ' ' '_')

adb pull $ExtStorageDir/test.jpg $NativeDir/$Domain/RearMinZoomPic.jpg
PullResult=$?
if [ $PullResult -eq "0" ];then
	convert_jpg2tif  $NativeDir/$Domain/RearMinZoomPic.jpg $NativeDir/$Domain/RearMinZoomPic.tif
	OCR_image $NativeDir/$Domain/RearMinZoomPic.tif $NativeDir/$Domain/RearMinZoomPic $CODES $PassNumKey | tee $NativeDir/$Domain/$LogFileName.log1
	less $NativeDir/$Domain/$LogFileName.log1 | grep TestWell
	CheckPass $? "RearMinZoomPic"
	Result1=$?
else
	echo "Pull Fail"
	CheckPass $PullResult "RearMinZoomPic"
	Result1=$?
fi

#Type="RearCamera"
#Des="ZoomOut"
#OutputResult $Result $Domain $Type $Des

adb shell rm $ExtStorageDir/test.jpg #for AirHugeTest
adb shell am instrument -w -e class android.hardware.cts.CameraTest#testTakePicRearSmoothMaxZoom com.android.cts.hardware/android.test.InstrumentationTestRunner
adb shell sync
sleep 1
sleep 5
adb pull $ExtStorageDir/test.jpg $NativeDir/$Domain/RearMaxZoomPic.jpg
PullResult=$?
if [ $PullResult -eq "0" ];then
	convert_jpg2tif  $NativeDir/$Domain/RearMaxZoomPic.jpg $NativeDir/$Domain/RearMaxZoomPic.tif
	OCR_image $NativeDir/$Domain/RearMaxZoomPic.tif $NativeDir/$Domain/RearMaxZoomPic $CODESZoomMax $PassNumKeyZoomMax | tee $NativeDir/$Domain/$LogFileName.log2
	less $NativeDir/$Domain/$LogFileName.log2 | grep TestWell
	CheckPass $? "RearMaxZoomPic"
	Result2=$?
else
	echo "Pull Fail"
	CheckPass $PullResult "RearMinZoomPic"
	Result2=$?
fi
cat $NativeDir/$Domain/$LogFileName.log1 >> $NativeDir/$Domain/$LogFileName.log
cat $NativeDir/$Domain/$LogFileName.log2 >> $NativeDir/$Domain/$LogFileName.log
#Type="RearCamera"
#Des="ZoomIn"
#OutputResult $Result $Domain $Type $Des
if [ "$Result1" == "1" ] || [ "$Result2" == "1" ]; then
	Result=1
else
	Result=0
fi
OutputResult $Result $Domain "$Type" "$Des" $NativeDir/TestItemConfig.xml
fi
UnInstallApk $CTSPackageDir/CtsTestStubs.apk
UnInstallApk $CTSPackageDir/CtsHardwareTestCases.apk
