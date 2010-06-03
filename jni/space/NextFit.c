#include <jni.h>
#include <stdlib.h>
#include "common.c"
#include <android/log.h>

jobject nextFitHistogram, spaceManager, pagedFile;

short pageSize, blockSize;
int maxRecordSize;
int NextFitHistogramNoWitness, NextFitHistogramNoPage;
int PagedFilePageAddFailed;
int SpaceManagerPolicyAcquireFailed;
int lastAcquired;

jmethodID midNextFitHistogramUpdate, midNextFitHistogramGetWitness;
jmethodID midSpaceManagerSetPageUsed, midSpaceManagerIsPageUsed, midSpaceManagerUpdate, midSpaceManagerIsBlockUsed,
	midSpaceManagerSetRecordUsed, midSpaceManagerFreeSpaceOnPage, midSpaceManagerCreate, midSpaceManagerLoad;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;
jmethodID midRecordSetChunkUsed, midRecordGetPagesUsed, midRecordGetPageUsage, midRecordClearUsage;
jfieldID fidPageUsagePageId;
jfieldID fidSpaceManagerPolicyAcquireFailed;
jfieldID fidPagedFilePageAddFailed;

void initIDs(JNIEnv *env) {
	jclass nextFitHistogramClass;
	jclass pagedFileClass;
	jclass spaceManagerClass;
	jclass spaceManagerPolicyClass;
	jclass recordClass;
	jclass pageUsageClass;

	jfieldID fidNextFitHistogramNoWitness, fidNextFitHistogramNoPage;

	nextFitHistogramClass = (*env)->FindClass(env, "com/atteo/jello/space/NextFitHistogram");
	if (nextFitHistogramClass == NULL)
		return;

	midNextFitHistogramUpdate = (*env)->GetMethodID(env, nextFitHistogramClass,
			"update", "(ISS)V");
	if (midNextFitHistogramUpdate == NULL)
		return;

	midNextFitHistogramGetWitness = (*env)->GetMethodID(env, nextFitHistogramClass,
			"getWitness", "(S)I");
	if (midNextFitHistogramGetWitness == NULL)
		return;

	fidNextFitHistogramNoWitness = (*env)->GetStaticFieldID(env, nextFitHistogramClass,
			"NO_WITNESS", "I");
	if (fidNextFitHistogramNoWitness == NULL)
		return;
	NextFitHistogramNoWitness = (*env)->GetStaticIntField(env, nextFitHistogramClass, fidNextFitHistogramNoWitness);

	fidNextFitHistogramNoPage= (*env)->GetStaticFieldID(env, nextFitHistogramClass,
			"NO_PAGE", "I");
	if (fidNextFitHistogramNoPage == NULL)
		return;
	NextFitHistogramNoPage = (*env)->GetStaticIntField(env, nextFitHistogramClass, fidNextFitHistogramNoPage);


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

	midSpaceManagerCreate = (*env)->GetMethodID(env, spaceManagerClass,
			"create", "()V");
	if (midSpaceManagerCreate == NULL)
		return;

	midSpaceManagerLoad = (*env)->GetMethodID(env, spaceManagerClass,
			"load", "()Z");
	if (midSpaceManagerLoad == NULL)
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

void JNICALL init(JNIEnv *env, jclass dis, jobject histogram, jobject file, jobject manager,
		jshort pageSizeArg, jshort blockSizeArg, jint maxRecordSizeArg) {
	initIDs(env);
	pagedFile = (*env)->NewGlobalRef(env,file);
	spaceManager = (*env)->NewGlobalRef(env,manager);
	nextFitHistogram = (*env)->NewGlobalRef(env,histogram);
	pageSize = pageSizeArg;
	blockSize = blockSizeArg;
	maxRecordSize = maxRecordSizeArg;

	lastAcquired = 0;

}

void initHistogram(JNIEnv *env) {
	int pages = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	int i;
	int free;

	for (i=0;i<pages;i++) {
		free = (*env)->CallShortMethod(env, spaceManager, midSpaceManagerFreeSpaceOnPage, i);
		(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, i, 0, free);
	}

}

void JNICALL create(JNIEnv *env, jclass dis) {
	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerCreate);
	initHistogram(env);

}

jboolean JNICALL load(JNIEnv *env, jclass dis) {
	if ((*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerLoad) == JNI_FALSE)
		return JNI_FALSE;
	initHistogram(env);

	return JNI_TRUE;
}


void removePages(JNIEnv *env) {
	int id;
	int count = 0;
	id = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount) - 1;

	while ((*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerIsPageUsed, id) == JNI_FALSE) {
		count++;
		(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id, pageSize, 0);
		id--;
	}

	if (count > 0) {
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileRemovePages, count);
		(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
	}
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

jint JNICALL acquirePage(JNIEnv *env, jclass dis) {
	int pages = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	int i;

	int id;
	int checkedId;
	jboolean pageUsed;

	id = (*env)->CallIntMethod(env, nextFitHistogram, midNextFitHistogramGetWitness, pageSize);

	if (id == NextFitHistogramNoWitness) {
		for (i=0;i<pages;i++) {
			checkedId = (i + lastAcquired) % pages;
			pageUsed = (*env)->CallBooleanMethod(env, spaceManager, midSpaceManagerIsPageUsed, checkedId);
			if (pageUsed == JNI_FALSE) {
				id = checkedId;
				(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id, pageSize, 0);
				break;
			}
		}
	} else if (id == NextFitHistogramNoPage) {
		id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);
		if (id == PagedFilePageAddFailed)
			return SpaceManagerPolicyAcquireFailed;

	} else
		(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id, pageSize, 0);

	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_TRUE);

	lastAcquired = (id + 1) % pages;


	return id;
}

void JNICALL releasePage(JNIEnv *env, jclass dis, jint id) {
	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetPageUsed, id, JNI_FALSE);
	(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id, 0, pageSize);

	removePages(env);

}

jboolean JNICALL acquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {
	int pages = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	int chunks = length / pageSize;

	int spaceToSpare;

	int leftToAcquire = length;
	short reservedOnThisPage;
	short freeSpace;
	short r;

	int i, j;
	int id;
	int checkedId;

	if (length > maxRecordSize)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException", "Requested record is too big");

	if (length % pageSize > 0)
		chunks++;
	spaceToSpare = chunks * pageSize - length;
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "acquiring record: %d", length);

//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "will use chunks: %d", chunks);

	for (i = 0; i < chunks; i++) {
		id = (*env)->CallIntMethod(env, nextFitHistogram, midNextFitHistogramGetWitness,
				(pageSize - spaceToSpare));

		if (id == NextFitHistogramNoWitness) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*no witness");
			for (j=0;j<pages;j++) {
				checkedId = (j+lastAcquired) % pages;
				freeSpace = (*env)->CallShortMethod(env, spaceManager,
						midSpaceManagerFreeSpaceOnPage, checkedId);

				if (freeSpace >= (pageSize - spaceToSpare)) {
//					__android_log_print(ANDROID_LOG_INFO, "Jello",  "found %d, freeSpace: %d",
//							checkedId, freeSpace);
					id = checkedId;
					reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire
						: freeSpace;
					r = reserveBlocks(env, record, id, reservedOnThisPage);

					(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id,
							freeSpace, freeSpace - r);

					spaceToSpare -= pageSize - reservedOnThisPage;
					leftToAcquire -= reservedOnThisPage;
					lastAcquired = (checkedId + 1) % pages;
					break;
				}
			}

		} else if (id == NextFitHistogramNoPage) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*no page");
			id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);

			if (id == PagedFilePageAddFailed)
				return JNI_FALSE;

			(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerUpdate);
			freeSpace = pageSize;
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;
			r = reserveBlocks(env, record, id, reservedOnThisPage);


			(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id, -1, freeSpace - r);

			spaceToSpare -= pageSize - reservedOnThisPage;
			leftToAcquire -= reservedOnThisPage;

		} else {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*page from histogram: %d", id);
			freeSpace = (*env)->CallShortMethod(env, spaceManager, midSpaceManagerFreeSpaceOnPage, id);
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "freeSpace: %d", freeSpace);
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;

			r = reserveBlocks(env, record, id, reservedOnThisPage);

			(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, id,
					freeSpace, freeSpace - r);

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
	short oldSpace[recordPages];
	jobject p;

	for (i = 0; i < recordPages; i++) {
		p = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage, i);
		pageId = (*env)->GetIntField(env, p, fidPageUsagePageId);
		oldSpace[i] = (*env)->CallShortMethod(env, spaceManager, midSpaceManagerFreeSpaceOnPage, pageId);
	}

	(*env)->CallVoidMethod(env, spaceManager, midSpaceManagerSetRecordUsed, record, JNI_FALSE);

	for (i = 0; i < recordPages; i++) {
		p = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage, i);
		pageId = (*env)->GetIntField(env, p, fidPageUsagePageId);
		newSpace = (*env)->CallShortMethod(env, spaceManager, midSpaceManagerFreeSpaceOnPage, pageId);
		(*env)->CallVoidMethod(env, nextFitHistogram, midNextFitHistogramUpdate, pageId, oldSpace[i], newSpace);
	}

//	free(oldSpace);

	removePages(env);

}

jboolean JNICALL reacquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {
	releaseRecord(env,dis,record);
	(*env)->CallVoidMethod(env, record, midRecordClearUsage);
	return acquireRecord(env, dis, record, length);

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[8];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/NextFit");

	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/space/NextFitHistogram;Lcom/atteo/jello/store/PagedFile;Lcom/atteo/jello/space/SpaceManager;SSI)V";
	nm[0].fnPtr = init;

	nm[1].name = "acquirePage";
	nm[1].signature = "()I";
	nm[1].fnPtr = acquirePage;

	nm[2].name = "releasePage";
	nm[2].signature = "(I)V";
	nm[2].fnPtr = releasePage;

	nm[3].name = "acquireRecord";
	nm[3].signature = "(Lcom/atteo/jello/Record;I)Z";
	nm[3].fnPtr = acquireRecord;

	nm[4].name = "reacquireRecord";
	nm[4].signature = "(Lcom/atteo/jello/Record;I)Z";
	nm[4].fnPtr = reacquireRecord;

	nm[5].name = "releaseRecord";
	nm[5].signature = "(Lcom/atteo/jello/Record;)V";
	nm[5].fnPtr = releaseRecord;

	nm[6].name = "create";
	nm[6].signature = "()V";
	nm[6].fnPtr = create;

	nm[7].name = "load";
	nm[7].signature = "()Z";
	nm[7].fnPtr = load;

	(*env)->RegisterNatives(env,klass,nm,8);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

