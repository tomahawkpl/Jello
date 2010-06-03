#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "common.c"

jobject appendOnlyCache, spaceManager, pagedFile;

short pageSize, blockSize;
int maxRecordSize;
int AppendOnlyCacheNoPage;
int PagedFilePageAddFailed;
int SpaceManagerPolicyAcquireFailed;

jmethodID midAppendOnlyCacheUpdate, midAppendOnlyCacheGetBestId, midAppendOnlyCacheGetFreeSpace;
jmethodID midSpaceManagerSetPageUsed, midSpaceManagerIsPageUsed, midSpaceManagerUpdate, midSpaceManagerIsBlockUsed,
	midSpaceManagerSetRecordUsed, midSpaceManagerFreeSpaceOnPage;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;
jmethodID midRecordSetChunkUsed, midRecordGetPagesUsed, midRecordGetPageUsage, midRecordClearUsage;
jfieldID fidPageUsagePageId;

void initIDs(JNIEnv *env) {
	jclass appendOnlyCacheClass;
	jclass spaceManagerClass;
	jclass pagedFileClass;
	jclass spaceManagerPolicyClass;
	jclass recordClass;
	jclass pageUsageClass;
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

	midAppendOnlyCacheGetFreeSpace = (*env)->GetMethodID(env, appendOnlyCacheClass,
			"getFreeSpace", "(I)S");
	if (midAppendOnlyCacheGetFreeSpace == NULL)
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

	midSpaceManagerIsBlockUsed = (*env)->GetMethodID(env, spaceManagerClass,
			"isBlockUsed", "(IS)Z");
	if (midSpaceManagerIsBlockUsed == NULL)
		return;

	midSpaceManagerSetRecordUsed = (*env)->GetMethodID(env, spaceManagerClass,
			"setRecordUsed", "(Lcom/atteo/jello/Record;Z)V");
	if (midSpaceManagerSetRecordUsed == NULL)
		return;

	midSpaceManagerFreeSpaceOnPage = (*env)->GetMethodID(env, spaceManagerClass,
			"freeSpaceOnPage", "(I)S");
	if (midSpaceManagerFreeSpaceOnPage == NULL)
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

	midRecordSetChunkUsed = (*env)->GetMethodID(env, recordClass,
			"setChunkUsed", "(ISSZ)V");
	if (midRecordSetChunkUsed == NULL)
		return;

	midRecordGetPagesUsed = (*env)->GetMethodID(env, recordClass,
			"getPagesUsed", "()I");
	if (midRecordGetPagesUsed == NULL)
		return;

	midRecordGetPageUsage = (*env)->GetMethodID(env, recordClass,
			"getPageUsage", "(I)Lcom/atteo/jello/PageUsage;");
	if (midRecordGetPageUsage == NULL)
		return;

	midRecordClearUsage = (*env)->GetMethodID(env, recordClass,
			"clearUsage", "()V");
	if (midRecordClearUsage == NULL)
		return;

	pageUsageClass = (*env)->FindClass(env, "com/atteo/jello/PageUsage");
	if (pageUsageClass == NULL)
		return;

	fidPageUsagePageId = (*env)->GetFieldID(env, pageUsageClass,
			"pageId","I");
	if (fidPageUsagePageId == NULL)
		return;
}

void JNICALL init(JNIEnv *env, jclass dis, jobject cache, jobject manager, jobject file, jshort pageSizeArg,
		jshort blockSizeArg, jint maxRecordSizeArg) {
	initIDs(env);

	appendOnlyCache = (*env)->NewGlobalRef(env,cache);
	spaceManager = (*env)->NewGlobalRef(env,manager);
	pagedFile = (*env)->NewGlobalRef(env,file);
	pageSize = pageSizeArg;
	blockSize = blockSizeArg;
	maxRecordSize = maxRecordSizeArg;
}

jint JNICALL acquirePage(JNIEnv *env, jclass dis) {
	int id;
	id = (*env)->CallIntMethod(env, appendOnlyCache, midAppendOnlyCacheGetBestId, pageSize);
	if (id != AppendOnlyCacheNoPage) {
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_TRUE);
		(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, 0);
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "acquired page from cache: %d", id);
	} else {
		id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);
		if (id == PagedFilePageAddFailed)
			return SpaceManagerPolicyAcquireFailed;

		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_TRUE);
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "acquired new page: %d", id);
	}

	return id;

}

void removePages(JNIEnv *env) {
	int id;
	int count = 0;
	id = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount) - 1;

	while ((*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerIsPageUsed, id) == JNI_FALSE) {
		count++;
		(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, 0);
		id--;
	}

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

short reserveBlocks(JNIEnv *env, jobject record, int id, short length) {
	short block = 0;
	short start = -1;
	short result = 0;
	while (length > 0) {
		if ((*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerIsBlockUsed, id, block) == JNI_FALSE) {
			length -= blockSize;
			if (start == -1)
				start = block;
		} else {
			if (start != -1) {
				(*env)->CallVoidMethod(env, record, midRecordSetChunkUsed, id, start, block, JNI_TRUE);
				result += block - start;
				start = -1;
			}
		}
		block++;
	}

	(*env)->CallVoidMethod(env, record, midRecordSetChunkUsed, id, start, block, JNI_TRUE);
	result += block - start;
	result *= blockSize;
	return result;
}

jboolean JNICALL acquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {
	int chunks = length / pageSize;

	int spaceToSpare;

	int leftToAcquire = length;
	short reservedOnThisPage;
	short freeSpace;
	short r;

	int i;
	int id;

	if (length > maxRecordSize)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException", "Requested record is too big");

	if (length % pageSize > 0)
		chunks++;
	spaceToSpare = chunks * pageSize - length;

	for (i = 0; i < chunks; i++) {
		id = (*env)->CallIntMethod(env, appendOnlyCache, midAppendOnlyCacheGetBestId, (pageSize - spaceToSpare));

		if (id != AppendOnlyCacheNoPage) {
			freeSpace = (*env)->CallShortMethod(env, appendOnlyCache, midAppendOnlyCacheGetFreeSpace, id);
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;

			r = reserveBlocks(env, record, id, reservedOnThisPage);

			(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, freeSpace - r);

			spaceToSpare -= pageSize - reservedOnThisPage;
			leftToAcquire -= reservedOnThisPage;

		} else {
			id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);

			if (id == PagedFilePageAddFailed)
				return JNI_FALSE;

			(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
			freeSpace = pageSize;
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;
			r = reserveBlocks(env, record, id, reservedOnThisPage);


			(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, id, freeSpace - r);

			spaceToSpare -= pageSize - reservedOnThisPage;
			leftToAcquire -= reservedOnThisPage;
		}
	}

	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetRecordUsed, record, JNI_TRUE);
	return JNI_TRUE;

}

void JNICALL releaseRecord(JNIEnv *env, jclass dis, jobject record) {
	int recordPages = (*env)->CallIntMethod(env, record, midRecordGetPagesUsed);
	short newSpace;
	int pageId;
	int i;
	jobject p;
	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetRecordUsed, record, JNI_FALSE);
	for (i = 0; i < recordPages; i++) {
		p = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage, i);
		pageId = (*env)->GetIntField(env, p, fidPageUsagePageId);
		newSpace = (*env)->CallShortMethod(env, spaceManager, midSpaceManagerFreeSpaceOnPage, pageId);
		(*env)->CallVoidMethod(env, appendOnlyCache, midAppendOnlyCacheUpdate, pageId, newSpace);
	}

	removePages(env);
}

jboolean JNICALL reacquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {
	releaseRecord(env,dis,record);
	(*env)->CallVoidMethod(env, record, midRecordClearUsage);
	return acquireRecord(env, dis, record, length);
}


jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[6];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/AppendOnly");

	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/space/AppendOnlyCache;Lcom/atteo/jello/space/SpaceManager;Lcom/atteo/jello/store/PagedFile;SSI)V";
	nm[0].fnPtr = init;

	nm[1].name = "acquirePage";
	nm[1].signature = "()I";
	nm[1].fnPtr = acquirePage;

	nm[2].name = "releasePage";
	nm[2].signature = "(I)V";
	nm[2].fnPtr = releasePage;

	nm[3].name = "reacquireRecord";
	nm[3].signature = "(Lcom/atteo/jello/Record;I)Z";
	nm[3].fnPtr = reacquireRecord;

	nm[4].name = "acquireRecord";
	nm[4].signature = "(Lcom/atteo/jello/Record;I)Z";
	nm[4].fnPtr = acquireRecord;

	nm[5].name = "releaseRecord";
	nm[5].signature = "(Lcom/atteo/jello/Record;)V";
	nm[5].fnPtr = releaseRecord;

	(*env)->RegisterNatives(env,klass,nm,6);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

