# busybox
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := TestItemConfig.xml
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air
LOCAL_MODULE_STEM := TestItemConfig.xml
LOCAL_MODULE_TAGS := debug
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)


