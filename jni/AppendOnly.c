#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "common.c"

jobject appendOnlyCache, spaceManager, pagedFile;

short pageSize;
int AppendOnlyCacheNoPage;
int PagedFilePageAddFailed;
int SpaceManagerPolicyAcquireFailed;

jmethodID midAppendOnlyCacheUpdate, midAppendOnlyCacheGetBestId;
jmethodID midSpaceManagerSetPageUsed, midSpaceManagerIsPageUsed, midSpaceManagerUpdate;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;

void initIDs(JNIEnv *env) {
	jclass klass;
	jclass appendOnlyCacheClass;
	jclass spaceManagerClass;
	jclass pagedFileClass;
	jclass spaceManagerPolicyClass;
	jclass recordClass;
	jfieldID fidAppendOnlyCacheNoPage, fidPagedFilePageAddFailed, fidSpaceManagerPolicyAcquireFailed;


	appendOnlyCacheClass = (*env)->FindClass(env, "com/atteo/jello/space/AppendOnlyCache");
	if (appendOnlyCacheClass == NULL)
		return;

	fidAppendOnlyCacheNoPage = (*env)->GetStaticFieldID(env, appendOnlyCacheClass,
			"NO_PAGE", "I");
	if (fidAppendOnlyCacheNoPage == NULL)
		return;
	AppendOnlyCacheNoPage = (*env)->GetStaticIntField(env, appendOnlyCacheClass, fidAppendOnlyCacheNoPage);

	midAppendOnlyCacheUpdate = (*env)->GetMethodID(env, appendOnlyCacheClass,
			"update", "(IS)V");
	if (midAppendOnlyCacheUpdate == NULL)
		return;

	midAppendOnlyCacheGetBestId = (*env)->GetMethodID(env, appendOnlyCacheClass,
			"getBestId", "(S)I");
	if (midAppendOnlyCacheGetBestId == NULL)
		return;


	spaceManagerPolicyClass = (*env)->FindClass(env, "com/atteo/jello/space/SpaceManagerPolicy");
	if (spaceManagerPolicyClass == NULL)
		return;

	fidSpaceManagerPolicyAcquireFailed = (*env)->GetStaticFieldID(env, spaceManagerPolicyClass,
			"ACQUIRE_FAILED", "I");
	if (fidSpaceManagerPolicyAcquireFailed == NULL)
		return;

	SpaceManagerPolicyAcquireFailed = (*env)->GetStaticIntField(env, spaceManagerPolicyClass,
			fidSpaceManagerPolicyAcquireFailed);


	spaceManagerClass = (*env)->FindClass(env, "com/atteo/jello/space/SpaceManager");
	if (spaceManagerClass == NULL)
		return;

	midSpaceManagerSetPageUsed = (*env)->GetMethodID(env, spaceManagerClass,
			"setPageUsed", "(IZ)V");
	if (midSpaceManagerSetPageUsed == NULL)
		return;

	midSpaceManagerIsPageUsed = (*env)->GetMethodID(env, spaceManagerClass,
			"isPageUsed", "(I)Z");
	if (midSpaceManagerIsPageUsed == NULL)
		return;

	midSpaceManagerUpdate = (*env)->GetMethodID(env, spaceManagerClass,
			"update", "()V");
	if (midSpaceManagerUpdate == NULL)
		return;


	pagedFileClass = (*env)->FindClass(env, "com/atteo/jello/store/PagedFile");
	if (pagedFileClass == NULL)
		return;

	fidPagedFilePageAddFailed = (*env)->GetStaticFieldID(env, pagedFileClass,
			"PAGE_ADD_FAILED", "I");
	if (fidPagedFilePageAddFailed == NULL)
		return;
	PagedFilePageAddFailed = (*env)->GetStaticIntField(env, pagedFileClass, fidPagedFilePageAddFailed);

	midPagedFileAddPages = (*env)->GetMethodID(env, pagedFileClass,
			"addPages", "(I)I");
	if (midPagedFileAddPages == NULL)
		return;

	midPagedFileRemovePages = (*env)->GetMethodID(env, pagedFileClass,
			"removePages", "(I)V");
	if (midPagedFileRemovePages == NULL)
		return;

	midPagedFileGetPageCount = (*env)->GetMethodID(env, pagedFileClass,
			"getPageCount", "()I");
	if (midPagedFileGetPageCount == NULL)
		return;


	recordClass = (*env)->FindClass(env, "com/atteo/jello/Record");
	if (recordClass == NULL)
		return;
}

void JNICALL init(JNIEnv *env, jclass dis, jobject cache, jobject manager, jobject file, jshort pageSizeArg) {
	initIDs(env);

	appendOnlyCache = (*env)->NewGlobalRef(env,cache);
	spaceManager = (*env)->NewGlobalRef(env,manager);
	pagedFile = (*env)->NewGlobalRef(env,file);
	pageSize = pageSizeArg;
}

jint JNICALL acquirePage(JNIEnv *env, jclass dis) {
	int id;
	id = (*env)->CallIntMethod(env, appendOnlyCache, midAppendOnlyCacheGetBestId, pageSize);
	if (id != AppendOnlyCacheNoPage) {
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_TRUE);
		(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, 0);
	} else {
		id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);
		if (id == PagedFilePageAddFailed)
			return SpaceManagerPolicyAcquireFailed;

		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_TRUE);
	}

	return id;

}

void removePages(JNIEnv *env) {
	int id;
	int count = 0;
	id = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount) - 1;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "start");
	while ((*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerIsPageUsed, id) == JNI_FALSE) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "removePages:%d", id);
		count++;
		(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, 0);
		id--;
	}

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "end %d", count);

	if (count > 0) {
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileRemovePages, count);
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
	}
}

void JNICALL releasePage(JNIEnv *env, jclass dis, jint id) {
	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_FALSE);
	(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, pageSize);

	removePages(env);
}

JNIEXPORT jobject JNICALL acquireRecord(JNIEnv *env, jclass dis, jint length) {

}

jint JNICALL reacquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {

}

void JNICALL releaseRecord(JNIEnv *env, jclass dis, jobject record) {

	removePages(env);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[4];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/AppendOnly");

	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/space/AppendOnlyCache;Lcom/atteo/jello/space/SpaceManager;Lcom/atteo/jello/store/PagedFile;S)V";
	nm[0].fnPtr = init;

	nm[1].name = "acquirePage";
	nm[1].signature = "()I";
	nm[1].fnPtr = acquirePage;

	nm[2].name = "releasePage";
	nm[2].signature = "(I)V";
	nm[2].fnPtr = releasePage;

	nm[3].name = "reacquireRecord";
	nm[3].signature = "(Lcom/atteo/jello/Record;I)V";
	nm[3].fnPtr = reacquireRecord;

//	nm[4].name = "acquireRecord";
//	nm[4].signature = "(I)Lcom/atteo/jello/Record;";
//	nm[4].fnPtr = acquireRecord;

//	nm[5].name = "releaseRecord";
//	nm[5].signature = "(Lcom/atteo/jello/Record;)V";
//	nm[5].fnPtr = releaseRecord;


	(*env)->RegisterNatives(env,klass,nm,4);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

