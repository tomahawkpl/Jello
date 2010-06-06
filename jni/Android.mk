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

# --- BTree

include $(CLEAR_VARS)

LOCAL_MODULE    := BTree
LOCAL_SRC_FILES := btree/BTreeNative.cpp btree/BTree.cpp btree/BTreeLeaf.cpp btree/AVLTree.cpp \
	btree/BTreeElement.cpp btree/BTreeNode.cpp btree/ChildInfo.cpp btree/PageIds.cpp btree/misc.cpp \
	btree/RecordInfo.cpp btree/ChildInfoFactory.cpp btree/RecordInfoFactory.cpp btree/NodeContent.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- PagedFileNative

include $(CLEAR_VARS)

LOCAL_MODULE    := PagedFileNative
LOCAL_SRC_FILES := store/PagedFileNative.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)


# --- PageSizeProvider

include $(CLEAR_VARS)

LOCAL_MODULE    := PageSizeProvider
LOCAL_SRC_FILES := PageSizeProvider.c

include $(BUILD_SHARED_LIBRARY)


# --- SpaceManager

include $(CLEAR_VARS)

LOCAL_MODULE    := SpaceManagerNative
LOCAL_SRC_FILES := space/SpaceManagerNative.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


# --- AppendOnlyCacheNative

include $(CLEAR_VARS)

LOCAL_MODULE    := AppendOnlyCacheNative
LOCAL_SRC_FILES := space/AppendOnlyCacheNative.c
include $(BUILD_SHARED_LIBRARY)

# --- NextFitHistogramNative

include $(CLEAR_VARS)

LOCAL_MODULE    := NextFitHistogramNative
LOCAL_SRC_FILES := space/NextFitHistogramNative.c
include $(BUILD_SHARED_LIBRARY)

# --- SimpleLockManager

include $(CLEAR_VARS)

LOCAL_MODULE    := SimpleLockManager
LOCAL_SRC_FILES := transaction/SimpleLockManager.c
include $(BUILD_SHARED_LIBRARY)


# --- AppendOnly

include $(CLEAR_VARS)

LOCAL_MODULE    := AppendOnly
LOCAL_SRC_FILES := space/AppendOnly.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- NextFit

include $(CLEAR_VARS)

LOCAL_MODULE    := NextFit
LOCAL_SRC_FILES := space/NextFit.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

# --- Hybrid

include $(CLEAR_VARS)

LOCAL_MODULE    := Hybrid
LOCAL_SRC_FILES := space/Hybrid.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
