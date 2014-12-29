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


AirPid=$(ps aux | grep "air.sh"|grep -v grep |awk '{print $2}')
echo Old AirPid is $AirPid
for xPid in $AirPid
do
	echo kill -9 $xPid
	kill -9 $xPid
done

#====== Collect log
Collx (){
cd $NativeDir
LogFiles=$(find ./ -type f -name "*.log" | grep "/air/")
mkdir $NativeDir/AirLog
cp TestItemConfig.xml $NativeDir/AirLog/
cp TestItemResult.xml $NativeDir/AirLog/
for Log in $LogFiles
do
	cp $Log $NativeDir/AirLog/
done
cd $NativeDir
zip -r AirLog.zip AirLog/ 
rm -rf AirLog
}
#======= Collect TestItemResult.xml
Colly (){
cd $NativeDir
ResultFiles=$(find ./ -type f -name "TestItemResult.xml" | grep "/air/")
echo "<configuration>" > TestItemResult.xml
for Result in $ResultFiles
do
	cat $Result >> $NativeDir/TestItemResult.xml
done
echo "</configuration>" >> TestItemResult.xml
}
Colly
Collx
