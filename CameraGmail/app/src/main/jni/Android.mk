LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := account
LOCAL_SRC_FILES := account.c info.c

include $(BUILD_SHARED_LIBRARY)
