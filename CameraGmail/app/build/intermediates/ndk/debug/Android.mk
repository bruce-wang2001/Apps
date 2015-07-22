LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := account
LOCAL_SRC_FILES := \
	/home/bruce/AndroidStudioProjects/CameraGmail/app/src/main/jni/info.c \
	/home/bruce/AndroidStudioProjects/CameraGmail/app/src/main/jni/Android.mk \
	/home/bruce/AndroidStudioProjects/CameraGmail/app/src/main/jni/account.c \

LOCAL_C_INCLUDES += /home/bruce/AndroidStudioProjects/CameraGmail/app/src/main/jni
LOCAL_C_INCLUDES += /home/bruce/AndroidStudioProjects/CameraGmail/app/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
