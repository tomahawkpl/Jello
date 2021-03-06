#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "common.c"

struct FreeSpaceInfo {
	int pageId;
	int dirty;
	unsigned char *data;
};

unsigned char *bitCounts;

void JNICALL setPageUsed(JNIEnv *env, jclass dis, jint id, jboolean used);
void JNICALL update(JNIEnv *env, jclass dis);

int pageCount;

int ByteSize = 8;

struct FreeSpaceInfo *freeSpaceInfo;

jint pageFreeSpaceInfo;

jint pagedFileSize;

jshort freeSpaceInfoPageCapacity;
jshort freeSpaceInfosPerPage;
jshort freeSpaceInfoSize;
jshort blockSize;

jobject pagedFile;

jobject listPage;

jfieldID fidListPageId, fidListPageData;
jmethodID midListPageSetNext, midListPageGetNext, midPagedFileReadPage, midPagedFileWritePage;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;
jmethodID midRecordGetPagesUsed, midRecordGetPageUsage;
jfieldID fidPageUsageUsage, fidPageUsagePageId;

unsigned char bitcount (unsigned char n)  {
	unsigned char count = 0 ;
	while (n)  {
		count++ ;
		n &= (n - 1) ;
	}
	return count;
}


void precomputeBits() {
	unsigned char i;
	bitCounts = malloc(256);

	bitCounts[0] = ByteSize - bitcount(0);
	for (i=1;i!=0;i++)
		bitCounts[i] = ByteSize - bitcount(i);
}

void initIDs(JNIEnv *env) {
	jclass klass;
	jclass recordClass;
	jclass pagedFileClass;
	jclass pageUsageClass;

	// get PagedFile class
	pagedFileClass = (*env)->FindClass(env, "com/atteo/jello/store/PagedFile");
	if (pagedFileClass == NULL)
		return;

	midPagedFileGetPageCount = (*env)->GetMethodID(env, pagedFileClass,
			"getPageCount", "()I");
	if (midPagedFileGetPageCount == NULL)
		return;

	midPagedFileReadPage = (*env)->GetMethodID(env, pagedFileClass,
			"readPage", "(Lcom/atteo/jello/store/Page;)V");
	if (midPagedFileReadPage == NULL)
		return;

	midPagedFileWritePage = (*env)->GetMethodID(env, pagedFileClass,
			"writePage", "(Lcom/atteo/jello/store/Page;)V");
	if (midPagedFileWritePage == NULL)
		return;

	midPagedFileAddPages = (*env)->GetMethodID(env, pagedFileClass,
			"addPages", "(I)I");
	if (midPagedFileAddPages == NULL)
		return;

	midPagedFileRemovePages = (*env)->GetMethodID(env, pagedFileClass,
			"removePages", "(I)V");
	if (midPagedFileRemovePages == NULL)
		return;

	klass = (*env)->FindClass(env,"com/atteo/jello/store/ListPage");
	if (klass == NULL)
		return;

	fidListPageId = (*env)->GetFieldID(env, klass, "id", "I");
	if (fidListPageId == NULL)
		return;

	fidListPageData = (*env)->GetFieldID(env, klass, "data", "[B");
	if (fidListPageData== NULL)
		return;

	midListPageSetNext = (*env)->GetMethodID(env, klass, "setNext", "(I)V");
	if (midListPageSetNext == NULL)
		return;

	midListPageGetNext = (*env)->GetMethodID(env, klass, "getNext", "()I");
	if (midListPageGetNext == NULL)
		return;

	recordClass = (*env)->FindClass(env, "com/atteo/jello/Record");
	if (recordClass == NULL)
		return;

	midRecordGetPagesUsed = (*env)->GetMethodID(env, recordClass,
			"getPagesUsed", "()I");
	if (midRecordGetPagesUsed == NULL)
		return;

	midRecordGetPageUsage = (*env)->GetMethodID(env, recordClass,
			"getPageUsage", "(I)Lcom/atteo/jello/PageUsage;");
	if (midRecordGetPageUsage == NULL)
		return;


	pageUsageClass = (*env)->FindClass(env, "com/atteo/jello/PageUsage");
	if (pageUsageClass == NULL)
		return;

	fidPageUsageUsage = (*env)->GetFieldID(env, pageUsageClass,
			"usage", "[B");
	if (fidPageUsageUsage == NULL)
		return;

	fidPageUsagePageId = (*env)->GetFieldID(env, pageUsageClass,
			"pageId", "I");
	if (fidPageUsagePageId == NULL)
		return;
}

void JNICALL init(JNIEnv *env, jclass dis, jobject pagedFileObject, jobject listPageObject,
		jshort freeSpaceInfosPerPageArg, jshort freeSpaceInfoSizeArg, jshort freeSpaceInfoPageCapacityArg,
		jshort pageFreeSpaceInfoArg, jshort blockSizeArg) {

	initIDs(env);
	precomputeBits();

	freeSpaceInfosPerPage = freeSpaceInfosPerPageArg;
	freeSpaceInfoSize = freeSpaceInfoSizeArg;
	freeSpaceInfoPageCapacity = freeSpaceInfoPageCapacityArg;
	pageFreeSpaceInfo = pageFreeSpaceInfoArg;
	blockSize = blockSizeArg;

	freeSpaceInfo = NULL;

	listPage = (*env)->NewGlobalRef(env,listPageObject);
	pagedFile = (*env)->NewGlobalRef(env, pagedFileObject);

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
}


void addPages(JNIEnv *env, jclass dis, jint count) {
	struct FreeSpaceInfo *newPage;
	int i;
	int lastNewPage;

	lastNewPage = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, count);


//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d pages, new pageCount: %d", count, pageCount + count);
	if (lastNewPage == -1)
		return;

	freeSpaceInfo = realloc(freeSpaceInfo, pageCount + count);

	for (i=0;i<count;i++) {
		newPage = &freeSpaceInfo[pageCount + i];

		newPage->pageId = lastNewPage - count + 1 + i;
		newPage->data = calloc(1, freeSpaceInfoPageCapacity);
		newPage->dirty = 1;

	}

	pageCount += count;

	for(i=pageCount - count;i<pageCount;i++)
		setPageUsed(env, dis, freeSpaceInfo[i].pageId, JNI_TRUE);
}

void removePages(JNIEnv *env, jclass dis, jint count) {
	struct FreeSpaceInfo *page;
	int i;
	int lastNewPage;

	if (count > pageCount)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Trying to remove too many Space Manager pages");


	for (i=0;i<count;i++) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "setting not used: %d", freeSpaceInfo[pageCount - i - 1].pageId);

		setPageUsed(env, dis, freeSpaceInfo[pageCount - i - 1].pageId, JNI_FALSE);

		free(freeSpaceInfo[pageCount - i - 1].data);

	}

	pageCount -= count;

	freeSpaceInfo = realloc(freeSpaceInfo, pageCount);

}

void JNICALL update(JNIEnv *env, jclass dis) {
	int currentPages;
	int difference;

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	currentPages = pageCount * freeSpaceInfosPerPage;

//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "pagedFileSize:%d currentPages:%d", pagedFileSize, currentPages);
	if (pagedFileSize >= currentPages) {
		difference = (pagedFileSize - currentPages) / freeSpaceInfosPerPage + 1;
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding:%d", difference);
		addPages(env, dis, difference);
		//---
		update(env, dis);
		//---
		return;
	}

	if (pagedFileSize <= currentPages - freeSpaceInfosPerPage) {
		difference = (currentPages - pagedFileSize) / freeSpaceInfosPerPage;
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "removing:%d", difference);
		removePages(env, dis, difference);
		return;
	}
}

int getFreeSpaceInfoPage(int id) {
	return id / freeSpaceInfosPerPage;
}

short getFreeSpaceInfoOffset(int id) {
	return id % freeSpaceInfosPerPage;
}

jboolean JNICALL isPageUsed(JNIEnv *env, jclass dis, jint id) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);


	if (freeSpaceInfoPage >= pageCount)
		return JNI_FALSE;

	fsi = &freeSpaceInfo[freeSpaceInfoPage];

	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
		if (fsi->data[offset+i] != 0)
			return JNI_TRUE;
	}

	return JNI_FALSE;

}

void JNICALL commit(JNIEnv *env, jclass dis) {
	jboolean isCopy;
	jbyteArray buffer;
	jbyte *bytes;
	int i;

	for (i=0;i<pageCount;i++) {
		if (freeSpaceInfo[i].dirty == 0)
			continue;

		(*env)->SetIntField(env, listPage, fidListPageId, freeSpaceInfo[i].pageId);

		buffer = (*env)->GetObjectField(env, listPage, fidListPageData);
		bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

		bytes += 4;

		memcpy((void*) bytes, (void*) freeSpaceInfo[i].data, freeSpaceInfoPageCapacity);

		(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

		(*env)->CallVoidMethod(env, listPage, midListPageSetNext,
				i + 1 < pageCount ? freeSpaceInfo[i+1].pageId : -1);
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileWritePage, listPage);

		freeSpaceInfo[i].dirty = 0;
	}
}

void JNICALL setPageUsed(JNIEnv *env, jclass dis, jint id, jboolean used) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	int i;
	unsigned char newValue;

	if (used == JNI_TRUE)
		newValue = 255;
	else
		newValue = 0;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);



	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return;
	}


	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {

//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "freeSpaceInfoPage: %d, offset: %d", freeSpaceInfoPage, offset + i);
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] = newValue;
	}

	freeSpaceInfo[freeSpaceInfoPage].dirty = 1;
}

jboolean JNICALL isBlockUsed(JNIEnv *env, jclass dis, jint id, jshort block) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return JNI_FALSE;

	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];
	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	i = block / ByteSize;
	block %= ByteSize;

	if (fsi->data[offset+i] & (1 << block))
		return JNI_TRUE;
	return JNI_FALSE;

}

void JNICALL setBlockUsed(JNIEnv *env, jclass dis, jint id, jshort block, jboolean used) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return;
	}


	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	i = block / ByteSize;
	block %= ByteSize;

	if (used == JNI_TRUE)
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] |= 1 << block;
	else
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] &= ~(1 << block);


	freeSpaceInfo[freeSpaceInfoPage].dirty = 1;

}

void JNICALL setRecordUsed(JNIEnv *env, jclass dis, jobject record, jboolean used) {
	int i,p;
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	jboolean isCopy;
	jbyte *areasByte;
	jbyteArray areas;

	int pageId;
	jobject pageUsage;

	int pages = (*env)->CallIntMethod(env, record, midRecordGetPagesUsed);

	for (p=0;p<pages;p++) {
		pageUsage = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage,p);
		pageId = (*env)->GetIntField(env, pageUsage, fidPageUsagePageId);
		areas = (*env)->GetObjectField(env, pageUsage, fidPageUsageUsage);
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "setRecordUsed: %d", pageId);

		freeSpaceInfoPage = getFreeSpaceInfoPage(pageId);
		freeSpaceInfoOffset = getFreeSpaceInfoOffset(pageId);

		if (freeSpaceInfoPage >= pageCount) {
			JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
					"Page with this id is not known to the Space Manager");
			return;
		}


		offset = freeSpaceInfoOffset * freeSpaceInfoSize;

		areasByte = (*env)->GetByteArrayElements(env, areas, &isCopy);

		if (used == JNI_TRUE)
			for (i=0;i<freeSpaceInfoSize;i++)
				freeSpaceInfo[freeSpaceInfoPage].data[offset+i] |= areasByte[i];
		else
			for (i=0;i<freeSpaceInfoSize;i++)
				freeSpaceInfo[freeSpaceInfoPage].data[offset+i] &= ~(areasByte[i]);

		freeSpaceInfo[freeSpaceInfoPage].dirty = 1;

		(*env)->ReleaseByteArrayElements(env, areas, areasByte, 0);

	}

}	

jlong JNICALL totalFreeSpace(JNIEnv *env, jclass dis) {
	int i, j, k, offset;
	jlong freeSpace = 0;
	struct FreeSpaceInfo *fsi;
	int pages = 0;

	for (i=0;i<pageCount;i++) {
		fsi = &freeSpaceInfo[i];
		for (j=0;j<freeSpaceInfosPerPage;j++) {
			pages++;
			if (pages > pagedFileSize)
				break;
			offset = j * freeSpaceInfoSize;
			for (k=0;k<freeSpaceInfoSize;k++) {
				//__android_log_print(ANDROID_LOG_INFO, "Jello",  "%d %d %d", offset + k, fsi->data[offset+k], bitCounts[fsi->data[offset+k]]);
				freeSpace += blockSize * bitCounts[fsi->data[offset+k]];
			}
		}
	}

	return freeSpace;
}

jshort JNICALL freeSpaceOnPage(JNIEnv *env, jclass dis, jint id) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i, j;
	int result = 0;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return JNI_FALSE;

	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];
	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "%d %d %d", offset + i, fsi->data[offset+i], bitCounts[fsi->data[offset+i]]);
		result += blockSize * bitCounts[fsi->data[offset+i]];
	}

	return result;

}

void JNICALL create(JNIEnv *env, jclass dis) {
	freeSpaceInfo = malloc(sizeof(struct FreeSpaceInfo));
	freeSpaceInfo[0].pageId = pageFreeSpaceInfo;
	freeSpaceInfo[0].data = calloc(1, freeSpaceInfoPageCapacity);
	pageCount = 1;

	setPageUsed(env, dis, pageFreeSpaceInfo, JNI_TRUE);

	update(env, dis);

	commit(env, dis);

}


jboolean JNICALL load(JNIEnv *env, jclass dis) {
	int i;
	int nextPageId;
	jbyteArray buffer;
	jbyte *bytes;
	jboolean isCopy;
	nextPageId = pageFreeSpaceInfo;

	if (freeSpaceInfo != NULL) {
		for (i=0; i<pageCount; i++)
			free(freeSpaceInfo[i].data);
		free(freeSpaceInfo);
		freeSpaceInfo = NULL;
	}

	pageCount = 0;

	i = 0;
	while (nextPageId != -1) {
		pageCount++;
		freeSpaceInfo = realloc(freeSpaceInfo, pageCount);
		freeSpaceInfo[i].pageId = nextPageId;
		(*env)->SetIntField(env, listPage, fidListPageId, nextPageId);
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileReadPage, listPage);
		nextPageId = (*env)->CallIntMethod(env, listPage, midListPageGetNext);

		buffer = (*env)->GetObjectField(env, listPage, fidListPageData);
		bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);
		bytes += 4;
		freeSpaceInfo[i].data = malloc(freeSpaceInfoPageCapacity);
		memcpy((void*) freeSpaceInfo[i].data, (void*) bytes, freeSpaceInfoPageCapacity);

		(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

		freeSpaceInfo[i].dirty = 0;
		i++;

	}


	return JNI_TRUE;

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[12];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/SpaceManagerNative");

	if (klass == NULL)
		return;


	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/store/PagedFile;Lcom/atteo/jello/store/ListPage;SSSIS)V";
	nm[0].fnPtr = init;

	nm[1].name = "create";
	nm[1].signature = "()V";
	nm[1].fnPtr = create;

	nm[2].name = "load";
	nm[2].signature = "()Z";
	nm[2].fnPtr = load;

	nm[3].name = "update";
	nm[3].signature = "()V";
	nm[3].fnPtr = update;

	nm[4].name = "isPageUsed";
	nm[4].signature = "(I)Z";
	nm[4].fnPtr = isPageUsed;

	nm[5].name = "setPageUsed";
	nm[5].signature = "(IZ)V";
	nm[5].fnPtr = setPageUsed;

	nm[6].name = "setRecordUsed";
	nm[6].signature = "(Lcom/atteo/jello/Record;Z)V";
	nm[6].fnPtr = setRecordUsed;

	nm[7].name = "isBlockUsed";
	nm[7].signature = "(IS)Z";
	nm[7].fnPtr = isBlockUsed;

	nm[8].name = "setBlockUsed";
	nm[8].signature = "(ISZ)V";
	nm[8].fnPtr = setBlockUsed;

	nm[9].name = "freeSpaceOnPage";
	nm[9].signature = "(I)S";
	nm[9].fnPtr = freeSpaceOnPage;

	nm[10].name = "totalFreeSpace";
	nm[10].signature = "()J";
	nm[10].fnPtr = totalFreeSpace;

	nm[11].name = "commit";
	nm[11].signature = "()V";
	nm[11].fnPtr = commit;

	(*env)->RegisterNatives(env,klass,nm,12);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

