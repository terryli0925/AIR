#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
#!/bin/bash
NativeFile=$(readlink -f $0)
NativeDir=$(dirname $NativeFile)
echo $NativeDir
cd $NativeDir
./BluetoothTest.sh
cd $NativeDir
./BluetoothTestPair.sh

Domain="bluetooth"
cat $Domain/TestItemResultPair.xml >> $Domain/TestItemResult.xml
rm $Domain/TestItemResultPair.xml
