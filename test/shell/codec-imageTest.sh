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
ReturnPass="1"
ReturnFail="0"
convert_png2tif () {
	convert $1 -compress none $2
}
OCR_image () {
	tesseract $1 $2
	String=$(less $2'.txt')
	echo $String
	Codes=$3
	CodesNum=$(echo $Codes | tr ":" " " | wc -w)
	echo CodesNum is $CodesNum
	for mIndex in $(seq 1 $CodesNum)
	do
		Code=$(echo $Codes|cut -f $mIndex --delimiter=:)
		echo $Code 
		NumCode=$(echo $String | grep -o $Code | wc -l)
		echo $NumCode
		if [ $NumCode -gt $4 ];then
			echo $Code has $NumCode ">" $4 PASS
		else
			echo $Code has $NumCode "<" $4 FAIL
			return $ReturnFail #0   # 0: Fail
		fi      
	done
	return $ReturnPass #1   # 1: PASS
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

ImageJpeg="1"
ImageGif="1"
ImagePng="1"
ImageBmp="1"

LoadConfig () {
configFile=$1
less $configFile | grep -A 1 jpeg | grep True 
Result=$?
ImageJpeg=$Result
echo ImageJpeg is $ImageJpeg --  0 is True, 1 is False

less $configFile | grep -A 1 gif | grep True 
Result=$?
ImageGif=$Result
echo ImageGif is $ImageGif --  0 is True, 1 is False

less $configFile | grep -A 1 png | grep True 
Result=$?
ImagePng=$Result
echo ImagePng is $ImagePng --  0 is True, 1 is False

less $configFile | grep -A 1 bmp | grep True 
Result=$?
ImageBmp=$Result
echo ImageBmp is $ImageBmp --  0 is True, 1 is False
}

OutputResult () {
#Usage: OutputResult $Result $Domain $Type $Descr $ConfigFile
	echo "<TestItem domain=\"$2\" type=\"$3\" description=\"$4\">" >> $NativeDir/$Domain/TestItemResult.xml
if [ $1 -eq "1" ];then
	echo "<Pass>True</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
else
	echo "<Pass>False</Pass>" >> $NativeDir/$Domain/TestItemResult.xml
fi
Id=$(less $5 | grep -A 2 "$4" | grep "<ID>"| cut -f2 --delimiter='<' | cut -f2 --delimiter='>')
echo "<ID>$Id</ID>" >> $NativeDir/$Domain/TestItemResult.xml
echo "<Log></Log>" >> $NativeDir/$Domain/TestItemResult.xml
echo "<Remark1></Remark1>" >> $NativeDir/$Domain/TestItemResult.xml
echo "<Remark2></Remark2>" >> $NativeDir/$Domain/TestItemResult.xml
echo "<Remark3></Remark3>" >> $NativeDir/$Domain/TestItemResult.xml
echo "</TestItem>" >> $NativeDir/$Domain/TestItemResult.xml
echo "" >> $NativeDir/$Domain/TestItemResult.xml
#less $NativeDir/Tail.xml >> $NativeDir/$Domain/TestItemResult.xml
}

#==========Image display testing start =====================
MON_PATH=$(locate -w monkeyrunner |  grep out/host/linux-x86/bin/ | grep qbjm0 | head -1| xargs dirname)
case "$PATH" in
		*$MON_PATH*) echo "MON_PATH present" 
				;;
		*)     echo "$PATH add $MON_PATH "
				export PATH="$PATH:$MON_PATH"
				;;
esac

cd $NativeDir
adb shell rm -r /mnt/sdcard/com.compal.codecimagetest/
adb shell mkdir /mnt/sdcard/com.compal.codecimagetest/
adb push image_jpeg.jpeg /mnt/sdcard/com.compal.codecimagetest/
adb push image_gif.gif /mnt/sdcard/com.compal.codecimagetest/
adb push image_png.png /mnt/sdcard/com.compal.codecimagetest/
adb push image_bmp.bmp /mnt/sdcard/com.compal.codecimagetest/

LoadConfig $NativeDir/TestItemConfig.xml

if [ $ImageJpeg -eq "0" ];then
adb shell am start -n com.android.gallery3d/.app.Gallery -a android.intent.action.VIEW -d "file:///mnt/sdcard/com.compal.codecimagetest/image_jpeg.jpeg" -t image/jpeg
monkeyrunner ImageJpegDisplay.py
fi

if [ $ImageGif -eq "0" ];then
adb shell am start -n com.android.gallery3d/.app.Gallery -a android.intent.action.VIEW -d "file:///mnt/sdcard/com.compal.codecimagetest/image_gif.gif" -t image/gif
monkeyrunner ImageGifDisplay.py
fi

if [ $ImagePng -eq "0" ];then
adb shell am start -n com.android.gallery3d/.app.Gallery -a android.intent.action.VIEW -d "file:///mnt/sdcard/com.compal.codecimagetest/image_png.png" -t image/png
monkeyrunner ImagePngDisplay.py
fi

if [ $ImageBmp -eq "0" ];then
adb shell am start -n com.android.gallery3d/.app.Gallery -a android.intent.action.VIEW -d "file:///mnt/sdcard/com.compal.codecimagetest/image_bmp.bmp" -t image/bmp
monkeyrunner ImageBmpDisplay.py
fi

if [ $ImageJpeg -eq "0" ];then
convert_png2tif ImageJpegDisplay.png ImageJpegDisplay.tif
fi

if [ $ImageGif -eq "0" ];then
convert_png2tif ImageGifDisplay.png ImageGifDisplay.tif
fi

if [ $ImagePng -eq "0" ];then
convert_png2tif ImagePngDisplay.png ImagePngDisplay.tif
fi

if [ $ImageBmp -eq "0" ];then
convert_png2tif ImageBmpDisplay.png ImageBmpDisplay.tif
fi
	KEY="A"
	PassNumKey="3"
	#CODES="AHI:MOT:UVW:XY"
	#CODES="AHI:HAI:TAIX:XO:XO:YOHO:XWY:WXY"  # M => IVI, W => VV, I => \
	#CODES="AHO:HAO:VOTO:XO:HX:YOHO:XUY:UXY"
	CODES="ATU:TAU:XU:XUY:UXY"
	#PassNumKeyZoomMax="1"
	#CODESZoomMax="XO:HX"
	PassNumKeyZoomMax="0"
	CODESZoomMax="XU"

Domain="codec-image"
rm -rf $NativeDir/$Domain
mkdir -p $NativeDir/$Domain

if [ $ImageJpeg -eq "0" ];then
OCR_image ImageJpegDisplay.tif ImageJpegDisplay $CODES $PassNumKey
CheckPass $? "Image jpeg"
Result=$?
Type="jpeg"
Des="jpeg"
#Id="54"
OutputResult $Result $Domain $Type $Des $NativeDir/TestItemConfig.xml
fi

if [ $ImageGif -eq "0" ];then
OCR_image ImageGifDisplay.tif ImageGifDisplay $CODES $PassNumKey
CheckPass $? "Image gif"
Result=$?
Type="gif"
Des="gif"
#Id="55"
OutputResult $Result $Domain $Type $Des $NativeDir/TestItemConfig.xml
fi

if [ $ImagePng -eq "0" ];then
OCR_image ImagePngDisplay.tif ImagePngDisplay $CODES $PassNumKey
CheckPass $? "Image png"
Result=$?
Type="png"
Des="png"
#Id="56"
OutputResult $Result $Domain $Type $Des $NativeDir/TestItemConfig.xml
fi

if [ $ImageBmp -eq "0" ];then
OCR_image ImageBmpDisplay.tif ImageBmpDisplay $CODES $PassNumKey
CheckPass $? "Image bmp"
Result=$?
Type="bmp"
Des="bmp"
#Id="57"
OutputResult $Result $Domain $Type $Des $NativeDir/TestItemConfig.xml
fi


