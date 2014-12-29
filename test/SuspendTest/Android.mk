LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/suspendTest

LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
		../GenericFunction/src/com/compal/genericfunction/GenericFunction.java

LOCAL_PACKAGE_NAME := SuspendTest

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))