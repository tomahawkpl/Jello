#include <jni.h>
#include <stdlib.h>
#include "common.c"

jobject appendOnlyCache;

jmethodID midAppendOnlyCacheUpdate;

void initIDs(JNIEnv *env) {
	jclass klass;
	jclass appendOnlyCacheClass;

	appendOnlyCacheClass = (*env)->FindClass(env, "com/atteo/jello/space/AppendOnlyCache");
	if (appendOnlyCacheClass == NULL)
		return;

	midAppendOnlyCacheUpdate = (*env)->GetMethodID(env, appendOnlyCacheClass,
			"update", "(IS)V");

	if (midAppendOnlyCacheUpdate == NULL)
		return;

}

void JNICALL init(JNIEnv *env, jclass dis, jobject cache) {
	initIDs(env);

	appendOnlyCache = cache;
}

jint JNICALL acquirePage(JNIEnv *env, jclass dis) {

}

void JNICALL releasePage(JNIEnv *env, jclass dis, jint id) {

}

jobject JNICALL acquireRecord(JNIEnv *env, jclass dis, jint length) {

}

jint JNICALL reacquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {

}

void JNICALL releaseRecord(JNIEnv *env, jclass dis, jobject record) {

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[6];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/AppendOnly");

	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/space/AppendOnlyCache;)V";
	nm[0].fnPtr = init;

	nm[1].name = "acquirePage";
	nm[1].signature = "()I";
	nm[1].fnPtr = acquirePage;

	nm[2].name = "releasePage";
	nm[2].signature = "(I)V";
	nm[2].fnPtr = releasePage;

	nm[3].name = "acquireRecord";
	nm[3].signature = "(I)Lcom/atteo/jello/Record;";
	nm[3].fnPtr = acquireRecord;

	nm[4].name = "reacquireRecord";
	nm[4].signature = "(Lcom/atteo/jello/Record;I)V";
	nm[4].fnPtr = reacquireRecord;

	nm[5].name = "releaseRecord";
	nm[5].signature = "(Lcom/atteo/jello/Record;)V";
	nm[5].fnPtr = releaseRecord;


	(*env)->RegisterNatives(env,klass,nm,6);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

