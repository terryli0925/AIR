LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	   ../GenericFunction/src/com/compal/genericfunction/GenericFunction.java	

LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/storageTest
LOCAL_PACKAGE_NAME := MountTest
LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := media

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
