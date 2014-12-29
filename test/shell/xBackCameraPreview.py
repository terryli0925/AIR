#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Using Lin"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."
"""
monkeyrunner test
"""
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
print("Monkey starts Camera Preview now!")
print("build.device = " + device.getProperty("build.device"))

result = device.takeSnapshot()
result.writeToFile('BackCameraPreview.png', 'png')

#MonkeyRunner.sleep(5)
#device.press('KEYCODE_FOCUS',MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(5)
device.press('KEYCODE_BACK',MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(1)


