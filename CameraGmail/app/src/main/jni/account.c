#include "com_jeremy_cameragmail_NativeLib.h"
#include "info.h"
#include <stdlib.h>

/*
 * Class:     com_jeremy_cameragmail_NativeLib
 * Method:    account
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_jeremy_cameragmail_NativeLib_account
  (JNIEnv *env, jobject obj)
{
    char *buf = get_account();
	jstring account = (*env)->NewStringUTF(env, buf);
    free(buf);
    return account;
}

/*
 * Class:     com_jeremy_cameragmail_NativeLib
 * Method:    password
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_jeremy_cameragmail_NativeLib_password
  (JNIEnv *env, jobject obj)
{
    int len;
    const uint8_t *buf = get_encrypt_passwd(&len);
    jbyteArray array = (*env)->NewByteArray(env, len);
    (*env)-> SetByteArrayRegion(env, array, 0, len, buf);
    return array;
}

/*
 * Class:     com_jeremy_cameragmail_NativeLib
 * Method:    getRawKey
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_jeremy_cameragmail_NativeLib_getRawKey
  (JNIEnv *env, jobject obj)
{
    const uint8_t *buf = get_raw_key();
    jbyteArray array = (*env)->NewByteArray(env, 16);
    (*env)-> SetByteArrayRegion(env, array, 0, 16, buf);
    return array;
}

/*
 * Class:     com_jeremy_cameragmail_NativeLib
 * Method:    email
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_jeremy_cameragmail_NativeLib_email
  (JNIEnv *env, jobject obj)
{
    char *buf = get_mail();
	jstring email = (*env)->NewStringUTF(env, buf);
    free(buf);
    return email;
}

