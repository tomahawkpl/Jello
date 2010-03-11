#include <jni.h>
#include <stdlib.h>
#include "common.c"

struct FreeSpaceInfo {
	jint pageId;
	jbyte *data;
};

void JNICALL setPageUsed(JNIEnv *env, jclass dis, jint id, jboolean used);

int pageCount;

int ByteSize = 8;

struct FreeSpaceInfo *freeSpaceInfo;

jint pageFreeSpaceInfo;

jshort freeSpaceInfoPageCapacity;
jshort freeSpaceInfosPerPage;
jshort freeSpaceInfoSize;
jshort blockSize;

jobject pagedFile;

jobject listPage;

jfieldID fidListPageId, fidListPageAccessibleData;
jmethodID midListPageSetNext, midPagedFileReadPage, midPagedFileWritePage;
jmethodID midPagedFileAddPages, midPagedFileRemovePages, midPagedFileGetPageCount;

void initIDs(JNIEnv *env) {
	jclass pagedFileClass;
	jclass klass;

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

	freeSpaceInfosPerPage = freeSpaceInfosPerPageArg;
	freeSpaceInfoSize = freeSpaceInfoSizeArg;
	freeSpaceInfoPageCapacity = freeSpaceInfoPageCapacityArg;
	pageFreeSpaceInfo = pageFreeSpaceInfoArg;
	blockSize = blockSizeArg;

	listPage = (*env)->NewGlobalRef(env,listPageObject);
	pagedFile = (*env)->NewGlobalRef(env, pagedFileObject);

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

	if (lastNewPage == -1)
		return;

	freeSpaceInfo = realloc(freeSpaceInfo, pageCount + count);

	for (i=0;i<count;i++) {
		newPage = &freeSpaceInfo[pageCount + i];

		newPage->pageId = lastNewPage - count + 1 + i;
		newPage->data = calloc(1, freeSpaceInfoPageCapacity);

		setPageUsed(env, dis, newPage->pageId, JNI_TRUE);
	}
	pageCount += count;
}

void JNICALL removePages(JNIEnv *env, jclass dis, jint count) {
	struct FreeSpaceInfo *page;
	int i;
	int lastNewPage;

	if (count >= pageCount) {
		count = pageCount - 1;
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Trying to remove too many Space Manager pages");
	}

	pageCount -= count;

	for (i=0;i<count;i++) {
		setPageUsed(env, dis, pageCount + count - i - 1, JNI_FALSE);

		free(&freeSpaceInfo[pageCount+count-i].data);
		free(&freeSpaceInfo[pageCount+count-i]);

	}

	freeSpaceInfo = realloc(freeSpaceInfo, pageCount);

}

void JNICALL update(JNIEnv *env, jclass dis) {
	long pagedFileSize;
	int currentPages;
	long difference;

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	currentPages = pageCount * freeSpaceInfosPerPage;

	if (pagedFileSize > currentPages) {
		difference = (pagedFileSize - currentPages) / freeSpaceInfosPerPage + 1;
		addPages(env, dis, difference);
		//---
		update(env, dis);
		//---
		return;
	}

	if (pagedFileSize <= currentPages - freeSpaceInfosPerPage) {
		difference = (currentPages - pagedFileSize) / freeSpaceInfosPerPage;
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
		JNI_ThrowByName(env, "java/lang/InvalidArgumentException",
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
	jbyte newValue;

	if (used == JNI_TRUE)
		newValue = 255;
	else
		newValue = 0;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);


	if (freeSpaceInfoPage >= pageCount) {
		JNI_ThrowByName(env, "java/lang/InvalidArgumentException",
				"Page with this id is not known to the Space Manager");
		return;
	}


	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
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
		JNI_ThrowByName(env, "java/lang/InvalidArgumentException",
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
		JNI_ThrowByName(env, "java/lang/InvalidArgumentException",
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
		JNI_ThrowByName(env, "java/lang/InvalidArgumentException",
				"Page with this id is not known to the Space Manager");
		return JNI_FALSE;
		
	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];
	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++)
		for (j=0;j<ByteSize;j++) {
			if (fsi->data[offset+i] & (1 << j))
				result += blockSize;

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
	JNINativeMethod nm[9];
	jclass klass;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/space/SpaceManagerNative");
	/* register methods with (*env)->RegisterNatives */

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

	nm[6].name = "isBlockUsed";
	nm[6].signature = "(IS)Z";
	nm[6].fnPtr = isBlockUsed;

	nm[7].name = "setBlockUsed";
	nm[7].signature = "(ISZ)V";
	nm[7].fnPtr = setBlockUsed;

	nm[8].name = "freeSpaceOnPage";
	nm[8].signature = "(I)S";
	nm[8].fnPtr = freeSpaceOnPage;

	(*env)->RegisterNatives(env,klass,nm,9);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

