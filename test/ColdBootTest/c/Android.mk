#ifneq ($(TARGET_SIMULATOR),true)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/coldbootTest

LOCAL_SRC_FILES:= ColdBootTest.c

LOCAL_MODULE := ColdBootTest

include $(BUILD_EXECUTABLE)
