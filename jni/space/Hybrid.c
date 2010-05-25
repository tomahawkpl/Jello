#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include "common.c"


struct FreeSpaceInfo {
	int pageId;
	int dirty;
	unsigned char *data;
};


// histogram
int *witnesses, *counts;

int HistogramNoWitness = -1, HistogramNoPage = -2;
int pagesInHistogram;

short classSize;
int histogramClasses;
//---

struct FreeSpaceInfo *freeSpaceInfo;

int ByteSize = 8;
unsigned char *bitCounts;
int freeSpaceInfoPageCount;
short pageSize, blockSize;
int maxRecordSize;
int PagedFilePageAddFailed;
int SpaceManagerPolicyAcquireFailed;

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
jmethodID midRecordGetPagesUsed, midRecordGetPageUsage;
jfieldID fidPageUsageUsage, fidPageUsagePageId;
jmethodID midRecordSetChunkUsed, midRecordGetPagesUsed, midRecordGetPageUsage, midRecordClearUsage;
jfieldID fidPageUsagePageId;

int lastAcquired;

void setPageUsed(JNIEnv *env, int id, int used);

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
	jclass pagedFileClass;
	jclass spaceManagerPolicyClass;
	jclass recordClass;
	jclass pageUsageClass;
	jclass listPageClass;
	jfieldID fidPagedFilePageAddFailed, fidSpaceManagerPolicyAcquireFailed;

	spaceManagerPolicyClass = (*env)->FindClass(env, "com/atteo/jello/space/SpaceManagerPolicy");
	if (spaceManagerPolicyClass == NULL)
		return;

	fidSpaceManagerPolicyAcquireFailed = (*env)->GetStaticFieldID(env, spaceManagerPolicyClass,
			"ACQUIRE_FAILED", "I");
	if (fidSpaceManagerPolicyAcquireFailed == NULL)
		return;

	SpaceManagerPolicyAcquireFailed = (*env)->GetStaticIntField(env, spaceManagerPolicyClass,
			fidSpaceManagerPolicyAcquireFailed);

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
	midPagedFileReadPage = (*env)->GetMethodID(env, pagedFileClass,
			"readPage", "(Lcom/atteo/jello/store/Page;)V");
	if (midPagedFileReadPage == NULL)
		return;

	midPagedFileWritePage = (*env)->GetMethodID(env, pagedFileClass,
			"writePage", "(Lcom/atteo/jello/store/Page;)V");
	if (midPagedFileWritePage == NULL)
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

	fidPageUsageUsage = (*env)->GetFieldID(env, pageUsageClass,
			"usage","[B");
	if (fidPageUsageUsage == NULL)
		return;

	listPageClass = (*env)->FindClass(env,"com/atteo/jello/store/ListPage");
	if (listPageClass == NULL)
		return;

	fidListPageId = (*env)->GetFieldID(env, listPageClass, "id", "I");
	fidListPageAccessibleData = (*env)->GetFieldID(env, listPageClass, "accessibleData", "[B");
	midListPageSetNext = (*env)->GetMethodID(env, listPageClass, "setNext", "(I)V");
}

void JNICALL init(JNIEnv *env, jclass dis, jobject pagedFileArg, jobject listPageArg, jshort pageSizeArg, 
		jshort blockSizeArg, jint maxRecordSizeArg, jshort freeSpaceInfosPerPageArg,
		jshort freeSpaceInfoSizeArg, jshort freeSpaceInfoPageCapacityArg, jint pageFreeSpaceInfoArg,
		jint histogramClassesArg) {
	int i;
	initIDs(env);

	precomputeBits();

	listPage = (*env)->NewGlobalRef(env,listPageArg);
	pagedFile = (*env)->NewGlobalRef(env, pagedFileArg);

	pageSize = pageSizeArg;
	blockSize = blockSizeArg;
	maxRecordSize = maxRecordSizeArg;

	freeSpaceInfosPerPage = freeSpaceInfosPerPageArg;
	freeSpaceInfoSize = freeSpaceInfoSizeArg;
	freeSpaceInfoPageCapacity = freeSpaceInfoPageCapacityArg;
	pageFreeSpaceInfo = pageFreeSpaceInfoArg;

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);

	lastAcquired = 0;

	// histogram

	if (pageSize %  histogramClassesArg != 0)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException", "histogramClasses doesn't divide pageSize");

	classSize = (short) (pageSize / histogramClassesArg);

	histogramClassesArg++; // empty pages have a separate class

	histogramClasses = histogramClassesArg;
	pagesInHistogram = 0;

	witnesses = malloc(histogramClasses * sizeof(int));
	counts = malloc(histogramClasses * sizeof(int));

	for (i = 0; i < histogramClasses; i++) {
		witnesses[i] = -1;
		counts[i] = 0;
	}

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "init done");
}

//
//  SpaceManagerNative
//

void addFreeSpaceInfoPages(JNIEnv *env, int count) {
	struct FreeSpaceInfo *newPage;
	int i;
	int lastNewPage;

	lastNewPage = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, count);

	//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d pages, new freeSpaceInfoPageCount: %d", count, freeSpaceInfoPageCount + count);
	if (lastNewPage == -1)
		return;

	freeSpaceInfo = realloc(freeSpaceInfo, freeSpaceInfoPageCount + count);

	for (i=0;i<count;i++) {
		newPage = &freeSpaceInfo[freeSpaceInfoPageCount + i];

		newPage->pageId = lastNewPage - count + 1 + i;
		newPage->data = calloc(1, freeSpaceInfoPageCapacity);
		newPage->dirty = 1;

	}

	freeSpaceInfoPageCount += count;

	for(i=freeSpaceInfoPageCount - count;i<freeSpaceInfoPageCount;i++)
		setPageUsed(env, freeSpaceInfo[i].pageId, 1);
}

void removeFreeSpaceInfoPages(JNIEnv *env, jint count) {
	int i;

	if (count > freeSpaceInfoPageCount)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Trying to remove too many Space Manager pages");


	for (i=0;i<count;i++) {
		//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "setting not used: %d", freeSpaceInfo[freeSpaceInfoPageCount - i - 1].pageId);

		setPageUsed(env, freeSpaceInfo[freeSpaceInfoPageCount - i - 1].pageId, 0);

		free(freeSpaceInfo[freeSpaceInfoPageCount - i - 1].data);

	}

	freeSpaceInfoPageCount -= count;

	freeSpaceInfo = realloc(freeSpaceInfo, freeSpaceInfoPageCount);

}

void freeSpaceInfoUpdate(JNIEnv *env) {
	int currentPages;
	int difference;

	pagedFileSize = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	currentPages = freeSpaceInfoPageCount * freeSpaceInfosPerPage;

	//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "pagedFileSize:%d currentPages:%d", pagedFileSize, currentPages);
	if (pagedFileSize >= currentPages) {
		difference = (pagedFileSize - currentPages) / freeSpaceInfosPerPage + 1;
		//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding:%d", difference);
		addFreeSpaceInfoPages(env, difference);
		//---
		freeSpaceInfoUpdate(env);
		//---
		return;
	}

	if (pagedFileSize <= currentPages - freeSpaceInfosPerPage) {
		difference = (currentPages - pagedFileSize) / freeSpaceInfosPerPage;
		//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "removing:%d", difference);
		removeFreeSpaceInfoPages(env, difference);
		return;
	}
}

int getFreeSpaceInfoPage(int id) {
	return id / freeSpaceInfosPerPage;
}

short getFreeSpaceInfoOffset(int id) {
	return id % freeSpaceInfosPerPage;
}

int isPageUsed(int id) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= freeSpaceInfoPageCount)
		return 0;

	fsi = &freeSpaceInfo[freeSpaceInfoPage];

	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
		if (fsi->data[offset+i] != 0)
			return 1;
	}

	return 0;
}

void freeSpaceInfoCommit(JNIEnv *env) {
	jboolean isCopy;
	jbyteArray buffer;
	jbyte *bytes;
	int i;

	for (i=0;i<freeSpaceInfoPageCount;i++) {
		if (freeSpaceInfo[i].dirty == 0)
			continue;

		(*env)->SetIntField(env, listPage, fidListPageId, freeSpaceInfo[i].pageId);

		buffer = (*env)->GetObjectField(env, listPage, fidListPageAccessibleData);
		bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

		memcpy((void*) bytes, (void*) freeSpaceInfo[i].data, freeSpaceInfoPageCapacity);

		(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

		(*env)->CallVoidMethod(env, listPage, midListPageSetNext,
				i + 1 < freeSpaceInfoPageCount ? freeSpaceInfo[i+1].pageId : -1);
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileWritePage, listPage);

		freeSpaceInfo[i].dirty = 0;
	}

}

void setPageUsed(JNIEnv *env, int id, int used) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	int i;
	unsigned char newValue;

	if (used == 1)
		newValue = 255;
	else
		newValue = 0;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= freeSpaceInfoPageCount) {
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

int isBlockUsed(JNIEnv *env, int id, short block) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= freeSpaceInfoPageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return 0;

	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];
	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	i = block / ByteSize;
	block %= ByteSize;

	if (fsi->data[offset+i] & (1 << block))
		return 1;
	return 0;

}

void setBlockUsed(JNIEnv *env, int id, short block, int used) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	int i;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= freeSpaceInfoPageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return;
	}


	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	i = block / ByteSize;
	block %= ByteSize;

	if (used == 1)
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] |= 1 << block;
	else
		freeSpaceInfo[freeSpaceInfoPage].data[offset+i] &= ~(1 << block);


	freeSpaceInfo[freeSpaceInfoPage].dirty = 1;

}

void setRecordUsed(JNIEnv *env, jobject record, int used) {
	int i,p;
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	short offset;
	jboolean isCopy;
	jbyte *areasByte;
	jbyteArray areas;

	int pageId;
	jobject pageUsage;

	int pages;
       	pages = (*env)->CallIntMethod(env, record, midRecordGetPagesUsed);

	for (p=0;p<pages;p++) {
		pageUsage = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage,p);
		pageId = (*env)->GetIntField(env, pageUsage, fidPageUsagePageId);
		areas = (*env)->GetObjectField(env, pageUsage, fidPageUsageUsage);

		freeSpaceInfoPage = getFreeSpaceInfoPage(pageId);
		freeSpaceInfoOffset = getFreeSpaceInfoOffset(pageId);

		if (freeSpaceInfoPage >= freeSpaceInfoPageCount) {
			JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
					"Page with this id is not known to the Space Manager");
			return;
		}


		offset = freeSpaceInfoOffset * freeSpaceInfoSize;

		areasByte = (*env)->GetByteArrayElements(env, areas, &isCopy);

		if (used == 1)
			for (i=0;i<freeSpaceInfoSize;i++)
				freeSpaceInfo[freeSpaceInfoPage].data[offset+i] |= areasByte[i];
		else
			for (i=0;i<freeSpaceInfoSize;i++)
				freeSpaceInfo[freeSpaceInfoPage].data[offset+i] &= ~(areasByte[i]);

		freeSpaceInfo[freeSpaceInfoPage].dirty = 1;

		(*env)->ReleaseByteArrayElements(env, areas, areasByte, 0);

	}

}	

long totalFreeSpace() {
	int i, j, k, offset;
	jlong freeSpace = 0;
	struct FreeSpaceInfo *fsi;
	int pages = 0;

	for (i=0;i<freeSpaceInfoPageCount;i++) {
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

short freeSpaceOnPage(JNIEnv *env, int id) {
	int freeSpaceInfoPage;
	short freeSpaceInfoOffset;
	struct FreeSpaceInfo *fsi;
	short offset;
	int i;
	int result = 0;

	freeSpaceInfoPage = getFreeSpaceInfoPage(id);
	freeSpaceInfoOffset = getFreeSpaceInfoOffset(id);

	if (freeSpaceInfoPage >= freeSpaceInfoPageCount) {
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException",
				"Page with this id is not known to the Space Manager");
		return -1;

	}

	fsi = &freeSpaceInfo[freeSpaceInfoPage];
	offset = freeSpaceInfoOffset * freeSpaceInfoSize;

	for (i=0;i<freeSpaceInfoSize;i++) {
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "%d %d %d", offset + i, fsi->data[offset+i], bitCounts[fsi->data[offset+i]]);
		result += blockSize * bitCounts[fsi->data[offset+i]];
	}

	return result;

}

//
// NextFitHistogramNative
//


int getClassSize() {
	return classSize;
}

int classFor(int freeSpace) {
	return freeSpace / classSize;
}

int getWitness(short freeSpace) {
	int loc = classFor(freeSpace);
	int found = 0;
	int w;
	loc++;
	if (loc == histogramClasses)
		loc = histogramClasses - 1;

	while (loc < histogramClasses) {
		if (counts[loc] > 0) {
			found = 1;
			w = witnesses[loc];
			if (w != -1)
				return w;
		}
		loc++;
	}

	if (found == 0)
		return HistogramNoPage;

	return HistogramNoWitness;

}

void updateHistogram(int id, short previousFreeSpace, short freeSpace) {
	int loc;
	if (previousFreeSpace != -1) {
		loc = classFor(previousFreeSpace);
		counts[loc]--;
		if (witnesses[loc] == id)
			witnesses[loc] = -1;

		pagesInHistogram--;
	}

	if (freeSpace == -1)
		return;

	loc = classFor(freeSpace);
	counts[loc]++;
	witnesses[loc] = id;
	pagesInHistogram++;
}


int acquirePageNF(JNIEnv *env) {
	int pages = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	int i;

	int id;
	int checkedId;
	jboolean pageUsed;

	id = getWitness(pageSize);

	if (id == HistogramNoWitness) {
		for (i=0;i<pages;i++) {
			checkedId = (i + lastAcquired) % pages;
			pageUsed = isPageUsed(checkedId);
			if (pageUsed == JNI_FALSE) {
				id = checkedId;
				updateHistogram(id, pageSize, 0);
				break;
			}
		}
	} else if (id == HistogramNoPage) {
		id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);
		if (id == PagedFilePageAddFailed)
			return SpaceManagerPolicyAcquireFailed;

	} else
		updateHistogram(id, pageSize, 0);

	freeSpaceInfoUpdate(env);
	setPageUsed(env, id, 1);

	lastAcquired = (id + 1) % pages;

	return id;
}

jint JNICALL acquirePage(JNIEnv *env, jclass dis) {
	return acquirePageNF(env);
}

void removeEmptyPages(JNIEnv *env) {
	int id;
	int count = 0;
	id = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount) - 1;

	while (isPageUsed(id) == 0) {
		count++;
		updateHistogram(id, pageSize, -1);
		id--;
	}

	if (count > 0) {
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileRemovePages, count);
		freeSpaceInfoUpdate(env);
	}
}

void JNICALL releasePage(JNIEnv *env, jclass dis, jint id) {
	setPageUsed(env, id, 0);
	updateHistogram(id, 0, pageSize);

	removeEmptyPages(env);

	freeSpaceInfoCommit(env);
}

short reserveBlocks(JNIEnv *env, jobject record, int id, short length) {
	short block = 0;
	short start = -1;
	short result = 0;
	while (length > 0) {
		if (isBlockUsed(env, id, block) == 0) {
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

int acquireRecordNF(JNIEnv *env, jobject record, jint length) {
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
		id = getWitness(pageSize - spaceToSpare);

		if (id == HistogramNoWitness) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*no witness");
			for (j=0;j<pages;j++) {
				checkedId = (j+lastAcquired) % pages;
				freeSpace = freeSpaceOnPage(env, checkedId);

				if (freeSpace >= (pageSize - spaceToSpare)) {
//					__android_log_print(ANDROID_LOG_INFO, "Jello",  "found %d, freeSpace: %d",
//							checkedId, freeSpace);
					id = checkedId;
					reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire
						: freeSpace;
					r = reserveBlocks(env, record, id, reservedOnThisPage);

					updateHistogram(id, freeSpace, freeSpace - r);

					spaceToSpare -= pageSize - reservedOnThisPage;
					leftToAcquire -= reservedOnThisPage;
					lastAcquired = (checkedId + 1) % pages;
					break;
				}
			}

		} else if (id == HistogramNoPage) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*no page");
			id = (*env)->CallIntMethod(env, pagedFile, midPagedFileAddPages, 1);

			if (id == PagedFilePageAddFailed)
				return 0;

			freeSpaceInfoUpdate(env);
			freeSpace = pageSize;
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;
			r = reserveBlocks(env, record, id, reservedOnThisPage);

			updateHistogram(id, -1, freeSpace - r);

			spaceToSpare -= pageSize - reservedOnThisPage;
			leftToAcquire -= reservedOnThisPage;

		} else {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "*page from histogram: %d", id);
			freeSpace = freeSpaceOnPage(env, id);
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "freeSpace: %d", freeSpace);
			reservedOnThisPage = (leftToAcquire < freeSpace) ? (short) leftToAcquire : freeSpace;

			r = reserveBlocks(env, record, id, reservedOnThisPage);

			updateHistogram(id, freeSpace, freeSpace - r);

			spaceToSpare -= pageSize - reservedOnThisPage;
			leftToAcquire -= reservedOnThisPage;

		}

	}

	setRecordUsed(env, record, 1);
	freeSpaceInfoCommit(env);

	return 1;

}

jboolean JNICALL acquireRecord(JNIEnv *env, jclass dis, jobject record, jint length) {
	return acquireRecordNF(env, record, length);
}

void initHistogram(JNIEnv *env) {
	int pages = (*env)->CallIntMethod(env, pagedFile, midPagedFileGetPageCount);
	int i;
	int free;

	for (i=0;i<pages;i++) {
		free = freeSpaceOnPage(env, i);
		updateHistogram(i, -1, free);
	}

}

void JNICALL create(JNIEnv *env, jclass dis) {
	freeSpaceInfo = malloc(sizeof(struct FreeSpaceInfo));
	freeSpaceInfo[0].pageId = pageFreeSpaceInfo;
	freeSpaceInfo[0].data = calloc(1, freeSpaceInfoPageCapacity);
	freeSpaceInfoPageCount = 1;

	setPageUsed(env, pageFreeSpaceInfo, 1);

	freeSpaceInfoUpdate(env);

	freeSpaceInfoCommit(env);

	initHistogram(env);
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "create done");

}

jboolean JNICALL load(JNIEnv *env, jclass dis) {
	int i;
	int nextPageId;
	jboolean isCopy;
	jbyteArray buffer;
	jbyte *bytes;
	freeSpaceInfoPageCount = 0;
	nextPageId = pageFreeSpaceInfo;

	i = 0;
	while (nextPageId != -1) {
		freeSpaceInfoPageCount++;
		freeSpaceInfo = realloc(freeSpaceInfo, freeSpaceInfoPageCount);
		(*env)->SetIntField(env, listPage, fidListPageId, nextPageId);
		(*env)->CallVoidMethod(env, pagedFile, midPagedFileReadPage, listPage);
		nextPageId = (*env)->GetIntField(env, listPage, fidListPageId);

		buffer = (*env)->GetObjectField(env, listPage, fidListPageAccessibleData);
		bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

		memcpy((void*) freeSpaceInfo[i].data, (void*) bytes, freeSpaceInfoPageCapacity);

		(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

		freeSpaceInfo[i].dirty = 0;
		i++;
	}

	initHistogram(env);

	return JNI_TRUE;
}

void JNICALL releaseRecord(JNIEnv *env, jclass dis, jobject record) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "releasing");
	int recordPages = (*env)->CallIntMethod(env, record, midRecordGetPagesUsed);
	short newSpace;
	int pageId;
	int i;
	short oldSpace[recordPages];
	jobject p;
	for (i = 0; i < recordPages; i++) {
		p = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage, i);
		pageId = (*env)->GetIntField(env, p, fidPageUsagePageId);
		oldSpace[i] = freeSpaceOnPage(env, pageId);
	}

	setRecordUsed(env, record, 0);

	for (i = 0; i < recordPages; i++) {
		p = (*env)->CallObjectMethod(env, record, midRecordGetPageUsage, i);
		pageId = (*env)->GetIntField(env, p, fidPageUsagePageId);
		newSpace = freeSpaceOnPage(env, pageId);
		updateHistogram(pageId, oldSpace[i], newSpace);
	}

	removeEmptyPages(env);
	freeSpaceInfoCommit(env);
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

	klass = (*env)->FindClass(env,"com/atteo/jello/space/Hybrid");

	nm[0].name = "init";
	nm[0].signature = "(Lcom/atteo/jello/store/PagedFile;Lcom/atteo/jello/store/ListPage;SSISSSII)V";
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

