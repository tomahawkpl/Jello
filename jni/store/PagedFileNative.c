#include <jni.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include "common.c"

int pagedFileFD;
void *mapping;
int readOnly;
long fileLength;
short pageSize;
int pages;
int prot;
int isOpened;
int openFlags;
int pageAddFailed;

jfieldID fidPageData, fidPageId;

void initIDs(JNIEnv *env) {
	jclass cls;
	jfieldID fid;
	cls = (*env)->FindClass(env, "com/atteo/jello/store/Page");
	
	if (cls == NULL)
		return;

	fidPageData = (*env)->GetFieldID(env, cls, "data", "[B");
	if (fidPageData == NULL) {
		return;
	}

	fidPageId = (*env)->GetFieldID(env, cls, "id", "I");
	if (fidPageId == NULL) {
		return;
	}

	cls = (*env)->FindClass(env, "com/atteo/jello/store/PagedFile");
	if (cls == NULL) {
		return;
	}
	fid = (*env)->GetStaticFieldID(env, cls, "PAGE_ADD_FAILED", "I");
	if (fid == NULL) {
		return;
	}

	pageAddFailed = (*env)->GetStaticIntField(env, cls, fid);


}

void mapFile(JNIEnv *env) {
	mapping = mmap(NULL, fileLength, prot, MAP_SHARED, pagedFileFD, 0);
	if (mapping == MAP_FAILED) {
		close(pagedFileFD);
		switch(errno) {
			case EACCES:
				JNI_ThrowByName(env, "java/io/IOException", "mmap() on file failed (EACCES)");
				break;
			case EINVAL:
				JNI_ThrowByName(env, "java/io/IOException", "mmap() on file failed (EINVAL)");
				break;
			case EAGAIN:
				JNI_ThrowByName(env, "java/io/IOException", "mmap() on file failed (EAGAIN)");
				break;
			default:
				JNI_ThrowByName(env, "java/io/IOException", "mmap() on file failed");
				break;
		}

	}

}

long getFileLength() {
	long cur = lseek(pagedFileFD, 0, SEEK_CUR);
	long end = lseek(pagedFileFD, 0, SEEK_END);
	lseek(pagedFileFD, cur, SEEK_SET);
	return end;
}

void JNICALL init(JNIEnv *env, jclass dis) {
	initIDs(env);
}

jint JNICALL openNative(JNIEnv *env, jclass dis, jstring fullpath, jboolean ro, jshort ps) {
	const char *str;

	if (isOpened)
		JNI_ThrowByName(env, "java/lang/InternalError", "File already opened, close first");

	pageSize = ps;

	if (ro == 1)
		openFlags = O_RDONLY;
	else
		openFlags = O_RDWR;
	

	str = (*env)->GetStringUTFChars(env, fullpath, NULL);
	pagedFileFD = open(str,openFlags);
	(*env)->ReleaseStringUTFChars(env, fullpath, str);

	if (pagedFileFD == -1) {
		JNI_ThrowByName(env, "java/io/IOException", "Couldn't open database file");
	}

	fileLength = getFileLength();

	if (fileLength > 0 && fileLength % pageSize != 0) {
		fileLength = ((fileLength / pageSize) + 1) * pageSize;

		ftruncate(pagedFileFD, fileLength);
	}

	pages = fileLength / pageSize;

	readOnly = ro;
	isOpened = 1;

	prot = PROT_READ;

	if (readOnly == 0)
		prot |= PROT_WRITE;

	if (fileLength > 0)
		mapFile(env);
	else
		mapping = NULL;


	return pagedFileFD;

}


void JNICALL closeNative(JNIEnv *env, jclass dis) {
	jint result;

	if (!isOpened)
		return;

	if (mapping != NULL)
		if (munmap(mapping, fileLength) == -1)
			JNI_ThrowByName(env, "java/io/IOException", "munmap() failed (close)");

	if (readOnly == 0)
		if (fsync(pagedFileFD) == 1)
			JNI_ThrowByName(env, "java/io/IOException", "fsync() failed (close)");

	for (;;) {
		result = (jint) close(pagedFileFD);

		if ((result != -1) || (errno != EINTR)) {
			break;
		}
	}

	isOpened = 0;
}

jint JNICALL addPages(JNIEnv *env, jclass dis, jint count) {
	long newLength;
	if (readOnly)
		JNI_ThrowByName(env,"java/lang/InternalError","File opened in read-only mode");

	newLength = fileLength + count * pageSize;

	if (mapping != NULL) {
		if (munmap(mapping, fileLength) == -1)
			JNI_ThrowByName(env, "java/io/IOException", "munmap() failed (addPages)");
	}

	if (ftruncate(pagedFileFD, newLength) == -1) {
		JNI_ThrowByName(env, "java/io/IOException", "ftruncate() failed (addPages)");
		return pageAddFailed;
	}


	fileLength = newLength;

	pages = fileLength / pageSize;

	if (fileLength == 0)
		mapping = NULL;
	else
		mapFile(env);

	return pages - 1;

}

jint JNICALL removePages(JNIEnv *env, jclass dis, jint count) {
	long newLength;
	if (readOnly)
		JNI_ThrowByName(env,"java/lang/InternalError","File opened in read-only mode");

	newLength = fileLength - count * pageSize;

	if (newLength < 0)
		newLength = 0;

	if (mapping != NULL)
		if (munmap(mapping, fileLength) == -1)
			JNI_ThrowByName(env, "java/io/IOException", "munmap() failed (removePages)");

	if (ftruncate(pagedFileFD, newLength) == -1)
		JNI_ThrowByName(env, "java/io/IOException", "ftruncate() failed (addPages)");

	fileLength = newLength;

	pages = fileLength / pageSize;

	if (fileLength == 0)
		mapping = NULL;
	else
		mapFile(env);

	return fileLength/pageSize - 1;

}

jlong JNICALL getFileLengthNative(JNIEnv *env, jclass dis) {
	return (jlong)getFileLength();
}

jint JNICALL getPageCount(JNIEnv *env, jclass dis) {
	return pages;
}

void JNICALL syncPages(JNIEnv *env, jclass dis, jint position, jint count) {
		if (mapping != NULL)
			msync((void*) (mapping+position*pageSize), count*pageSize, MS_SYNC | MS_INVALIDATE);
}

void JNICALL syncAll(JNIEnv *env, jclass dis) {
		if (mapping != NULL)
			msync((void*) (mapping), fileLength, MS_SYNC | MS_INVALIDATE);
}

void JNICALL readPage(JNIEnv *env, jclass dis, jobject page) {
	jboolean isCopy;
	jbyteArray buffer;
	jbyte *bytes;
	jint id;

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/lang/InternalError","Attempting to read from an empty file");

	buffer = (*env)->GetObjectField(env, page, fidPageData);
	id = (*env)->GetIntField(env, page, fidPageId);

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) bytes, (void*) (mapping + (int)id * pageSize), pageSize);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

}


void JNICALL writePage(JNIEnv *env, jclass dis, jobject page) {
	jboolean isCopy;
	jbyte *bytes;
	jbyteArray buffer;
	jint id;

	if (readOnly)
		JNI_ThrowByName(env,"java/lang/InternalError","File opened in read-only mode");

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/lang/InternalError","Attempting to write to an empty file");


	buffer = (*env)->GetObjectField(env, page, fidPageData);
	id = (*env)->GetIntField(env, page, fidPageId);

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) (mapping + (int)id * pageSize), (void*) bytes, pageSize);
	
	(*env)->ReleaseByteArrayElements(env, buffer, bytes, JNI_ABORT);
}



jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[11];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	klass = (*env)->FindClass(env,"com/atteo/jello/store/PagedFileNative");

	nm[0].name = "openNative";
	nm[0].signature = "(Ljava/lang/String;ZS)I";
	nm[0].fnPtr = openNative;

	nm[1].name = "close";
	nm[1].signature = "()V";
	nm[1].fnPtr = closeNative;

	nm[2].name = "addPages";
	nm[2].signature = "(I)I";
	nm[2].fnPtr = addPages;

	nm[3].name = "removePages";
	nm[3].signature = "(I)V";
	nm[3].fnPtr = removePages;

	nm[4].name = "getPageCount";
	nm[4].signature = "()I";
	nm[4].fnPtr = getPageCount;

	nm[5].name = "getFileLength";
	nm[5].signature = "()J";
	nm[5].fnPtr = getFileLengthNative;

	nm[6].name = "syncPages";
	nm[6].signature = "(II)V";
	nm[6].fnPtr = syncPages;

	nm[7].name = "syncAll";
	nm[7].signature = "()V";
	nm[7].fnPtr = syncAll;

	nm[8].name = "readPage";
	nm[8].signature = "(Lcom/atteo/jello/store/Page;)V";
	nm[8].fnPtr = readPage;

	nm[9].name = "writePage";
	nm[9].signature = "(Lcom/atteo/jello/store/Page;)V";
	nm[9].fnPtr = writePage;

	nm[10].name = "init";
	nm[10].signature = "()V";
	nm[10].fnPtr = init;


	(*env)->RegisterNatives(env,klass,nm,11);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

