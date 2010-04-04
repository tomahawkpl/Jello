#include <jni.h>
#include <stdlib.h>
#include "common.c"

struct FreeSpaceInfo {
	int pageId;
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

jfieldID fidListPageId, fidListPageAccessibleData;
jmethodID midListPageSetNext, midPagedFileReadPage, midPagedFileWritePage;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;

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
	jclass pagedFileClass;

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
	fidListPageAccessibleData = (*env)->GetFieldID(env, klass, "accessibleData", "[B");
	midListPageSetNext = (*env)->GetMethodID(env, klass, "setNext", "(I)V");
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

	listPage = (*env)->NewGlobalRef(env,listPageObject);
	pagedFile = (*env)->NewGlobalRef(env, pagedFileObject);

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
}


void writeFreeSpaceInfo(JNIEnv *env, int page) {
	jboolean isCopy;
	jbyteArray buffer;
	jbyte *bytes;

	(*env)->SetIntField(env, listPage, fidListPageId, freeSpaceInfo[page].pageId);

	buffer = (*env)->GetObjectField(env, listPage, fidListPageAccessibleData);
	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) bytes, (void*) freeSpaceInfo[page].data, freeSpaceInfoPageCapacity);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

	(*env)->CallVoidMethod(env, listPage, midListPageSetNext,
			page + 1 < pageCount ? freeSpaceInfo[page+1].pageId : -1);
	(*env)->CallVoidMethod(env, pagedFile, midPagedFileWritePage, listPage);
}


void addPages(JNIEnv *env, jclass dis, int count) {
	struct FreeSpaceInfo *newPage;
	int i;
	int lastNewPage;

	lastNewPage = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, count);


//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d pages, new pageCount: %d", count, pageCount);
	if (lastNewPage == -1)
		return;

	freeSpaceInfo = realloc(freeSpaceInfo, pageCount + count);

	for (i=0;i<count;i++) {
		newPage = &freeSpaceInfo[pageCount + i];

		newPage->pageId = lastNewPage - count + 1 + i;
		newPage->data = calloc(1, freeSpaceInfoPageCapacity);

//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "setting used: %d", newPage->pageId);
	}

	pageCount += count;

	for(i=pageCount - count;i<pageCount;i++)
		setPageUsed(env, dis, i, JNI_TRUE);
}

void JNICALL removePages(JNIEnv *env, jclass dis, jint count) {
	struct FreeSpaceInfo *page;
	int i;
	int lastNewPage;

	if (count > pageCount)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Trying to remove too many Space Manager pages");

	pageCount -= count;

	for (i=0;i<count;i++) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "setting not used: %d", freeSpaceInfo[pageCount + count - i - 1].pageId);

		setPageUsed(env, dis, freeSpaceInfo[pageCount + count - i - 1].pageId, JNI_FALSE);

		free(&freeSpaceInfo[pageCount + count - i - 1].data);
		free(&freeSpaceInfo[pageCount + count - i - 1]);

	}

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
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "difference %d", difference);
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


	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return JNI_FALSE;

	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];

	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
		if (fsi->data[offset+i] != 0)
			return JNI_TRUE;
	}

	return JNI_FALSE;

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

		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "freeSpaceInfoPage: %d, offset: %d", freeSpaceInfoPage, offset + i);
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] = newValue;
	}


	writeFreeSpaceInfo(env, freeSpaceInfoPage);
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


	writeFreeSpaceInfo(env, freeSpaceInfoPage);

}

void JNICALL setAreasUsed(JNIEnv *env, jclass dis, jint pageId, jbyteArray areas, jboolean used) {
	int i;
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	jboolean isCopy;
	jbyte *areasByte;

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
		
	(*env)->ReleaseByteArrayElements(env, areas, areasByte, 0);
	
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

	writeFreeSpaceInfo(env, 0);


}


jboolean JNICALL load(JNIEnv *env, jclass dis) {
	(*env)->SetIntField(env, listPage, fidListPageId, pageFreeSpaceInfo);
	(*env)->CallVoidMethod(env, pagedFile, midPagedFileReadPage, listPage);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	JNINativeMethod nm[11];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/space/SpaceManagerNative");

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

	nm[6].name = "setAreasUsed";
	nm[6].signature = "(I[BZ)V";
	nm[6].fnPtr = setPageUsed;

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

	(*env)->RegisterNatives(env,klass,nm,11);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

