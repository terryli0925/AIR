#
# (C) Copyright 2011-2012 Compal Electronics, Inc. 
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
#
#!/bin/bash
adb install -r ColdBootTest.apk
adb remount
adb push ./ColdBootTest /system/bin/
adb shell chmod 4755 /system/bin/ColdBootTest