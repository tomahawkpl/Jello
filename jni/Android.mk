# Copyright (C) 2009 The Android Open Source Project
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
#
LOCAL_PATH := $(call my-dir)


# --- PagedFileFast

include $(CLEAR_VARS)

LOCAL_MODULE    := PagedFileNative
LOCAL_SRC_FILES := PagedFileNative.c

include $(BUILD_SHARED_LIBRARY)


# --- OSInfo

include $(CLEAR_VARS)

LOCAL_MODULE    := PageSizeProvider
LOCAL_SRC_FILES := PageSizeProvider.c

include $(BUILD_SHARED_LIBRARY)


# --- SpaceManager

include $(CLEAR_VARS)

LOCAL_MODULE    := SpaceManagerNative
LOCAL_SRC_FILES := SpaceManagerNative.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


# --- AppendOnlyCacheNative

include $(CLEAR_VARS)

LOCAL_MODULE    := AppendOnlyCacheNative
LOCAL_SRC_FILES := AppendOnlyCacheNative.c
include $(BUILD_SHARED_LIBRARY)

# --- NextFitHistogramNative

include $(CLEAR_VARS)

LOCAL_MODULE    := NextFitHistogramNative
LOCAL_SRC_FILES := NextFitHistogramNative.c
include $(BUILD_SHARED_LIBRARY)

# --- SimpleLockManager

include $(CLEAR_VARS)

LOCAL_MODULE    := SimpleLockManager
LOCAL_SRC_FILES := SimpleLockManager.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- BTree

include $(CLEAR_VARS)

LOCAL_MODULE    := BTree
LOCAL_SRC_FILES := BTree.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- AppendOnly

include $(CLEAR_VARS)

LOCAL_MODULE    := AppendOnly
LOCAL_SRC_FILES := AppendOnly.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- NextFit

include $(CLEAR_VARS)

LOCAL_MODULE    := NextFit
LOCAL_SRC_FILES := NextFit.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- Hybrid

include $(CLEAR_VARS)

LOCAL_MODULE    := Hybrid
LOCAL_SRC_FILES := Hybrid.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
