# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

# don't include this package in any target
LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_TAGS := tests
# and when built explicitly put it in the data partition
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/air/storageTest

LOCAL_JAVA_LIBRARIES := android.test.runner

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
		../GenericFunction/src/com/compal/genericfunction/GenericFunction.java

LOCAL_PACKAGE_NAME := StorageTest

#LOCAL_INSTRUMENTATION_FOR := CtsTestStubs
LOCAL_INSTRUMENTATION_FOR := Gallery2

#LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := media

include $(BUILD_PACKAGE)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
