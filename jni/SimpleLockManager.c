#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "common.c"


void initIDs(JNIEnv *env) {
}

void JNICALL init(JNIEnv *env, jclass dis) {

	initIDs(env);

}

jint JNICALL acquirePageLock(JNIEnv *env, jclass dis, jint pageId) {
}

jint JNICALL acquireRecordLock(JNIEnv *env, jclass dis, jobject record) {
}

void JNICALL releaseLock(JNIEnv *env, jclass dis, jint lockId) {
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[4];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/transaction/SimpleLockManager");

	nm[0].name = "init";
	nm[0].signature = "()V";
	nm[0].fnPtr = init;

	nm[1].name = "acquirePageLock";
	nm[1].signature = "(I)I";
	nm[1].fnPtr = acquirePageLock;

	nm[2].name = "acquireRecordLock";
	nm[2].signature = "(Lcom/atteo/jello/Record;)I";
	nm[2].fnPtr = acquireRecordLock;

	nm[3].name = "releaseLock";
	nm[3].signature = "(I)V";
	nm[3].fnPtr = releaseLock;

	(*env)->RegisterNatives(env,klass,nm,4);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

