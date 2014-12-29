# busybox
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := CtsHardwareTestCases.apk
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/cameraTest/tools/android-cts/repository/testcases
LOCAL_MODULE_STEM := CtsHardwareTestCases.apk
LOCAL_MODULE_TAGS := debug
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := CtsTestStubs.apk
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/cameraTest/tools/android-cts/repository/testcases
LOCAL_MODULE_STEM := CtsTestStubs.apk
LOCAL_MODULE_TAGS := debug
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

