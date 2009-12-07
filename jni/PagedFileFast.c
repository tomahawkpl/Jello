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
void *mapping = NULL;
int readOnly;
int fileLength;
int pageSize;
int pages;
int prot;
int isOpened;
int openFlags;

void mapFile(JNIEnv *env) {
	mapping = mmap(mapping, fileLength, prot, MAP_PRIVATE, pagedFileFD, 0);
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

int getFileLength() {
	int cur = lseek(pagedFileFD, 0, SEEK_CUR);
	int end = lseek(pagedFileFD, 0, SEEK_END);
	lseek(pagedFileFD, cur, SEEK_SET);
	return end;

}

jint JNICALL openNative
(JNIEnv *env, jclass dis, jstring fullpath, jboolean ro, jint ps) {
	const jbyte *str;

	if (isOpened)
		JNI_ThrowByName(env, "java/io/IOException", "File already opened, close first");

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

	if (fileLength > 0)
		mapFile(env);

	prot = PROT_READ;

	if (!readOnly)
		prot |= PROT_WRITE;

	return pagedFileFD;

}


void JNICALL closeNative
(JNIEnv *env, jclass dis) {
	jint result;

	if (!isOpened)
		return;

	if (readOnly == 0) {
		if (mapping != NULL)
			msync(mapping, fileLength, MS_SYNC);
		fsync(pagedFileFD);
	}

	if (mapping != NULL)
		munmap(mapping, fileLength);

	for (;;) {
		result = (jint) close(pagedFileFD);

		if ((result != -1) || (errno != EINTR)) {
			break;
		}
	}
}

jint JNICALL addPages
(JNIEnv *env, jclass dis, jint count) {
	int newLength;
	if (readOnly)
		JNI_ThrowByName(env,"java/lang/IllegalAccessException","File opened in read-only mode");

	newLength = fileLength + count * pageSize;

	ftruncate(pagedFileFD, newLength);

	if (mapping != NULL) {
		munmap(mapping, fileLength);
	}

	fileLength = newLength;

	pages = fileLength / pageSize;

	if (fileLength == 0)
		mapping = NULL;
	else
		mapFile(env);

	return fileLength/pageSize - 1;

}

jint JNICALL removePages
(JNIEnv *env, jclass dis, jint count) {
	int newLength;
	if (readOnly)
		JNI_ThrowByName(env,"java/lang/IllegalAccessException","File opened in read-only mode");

	newLength = fileLength - count * pageSize;

	if (newLength < 0)
		newLength = 0;

	ftruncate(pagedFileFD, newLength);

	if (mapping != NULL) {
		munmap(mapping, fileLength);
	}

	fileLength = newLength;

	pages = fileLength / pageSize;

	if (fileLength == 0)
		mapping = NULL;
	else
		mapFile(env);

	return fileLength/pageSize - 1;

}

jint JNICALL getFileLengthNative
(JNIEnv *env, jclass dis) {
	return (jint)getFileLength();
}

jint JNICALL getPageCount
(JNIEnv *env, jclass dis) {
	return pages;
}

jboolean JNICALL isReadOnly
(JNIEnv *env, jclass dis) {
	return (jboolean)readOnly;
}

void JNICALL syncPages
(JNIEnv *env, jclass dis, jint position, jint count) {
	if (mapping != NULL)
		msync((void*) (mapping+position*pageSize), count*pageSize, MS_SYNC | MS_INVALIDATE);
}

void JNICALL syncAll
(JNIEnv *env, jclass dis) {
	if (mapping != NULL)
		msync((void*) (mapping), fileLength, MS_SYNC | MS_INVALIDATE);
}

void JNICALL readPage
(JNIEnv *env, jclass dis, jint id, jbyteArray buffer) {
	jboolean isCopy;
	jbyte *bytes;

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/io/IOException","Attempting to read from an empty file");

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) bytes, (void*) (mapping + id * pageSize), pageSize);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

}



void JNICALL writePage
(JNIEnv *env, jclass dis, jint id, jbyteArray buffer) {
	jboolean isCopy;
	jbyte *bytes;

	if (readOnly)
		JNI_ThrowByName(env,"java/lang/IllegalAccessException","File opened in read-only mode");

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/io/IOException","Attempting to write to an empty file");

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) (mapping + id * pageSize), (void*) bytes, pageSize);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, JNI_ABORT);
}



jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[11];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/store/PagedFileFast");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "openNative";
	nm[0].signature = "(Ljava/lang/String;ZI)I";
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
	nm[5].signature = "()I";
	nm[5].fnPtr = getFileLengthNative;

	nm[6].name = "isReadOnly";
	nm[6].signature = "()Z";
	nm[6].fnPtr = isReadOnly;

	nm[7].name = "syncPages";
	nm[7].signature = "(II)V";
	nm[7].fnPtr = syncPages;

	nm[8].name = "syncAll";
	nm[8].signature = "()V";
	nm[8].fnPtr = syncAll;

	nm[9].name = "readPage";
	nm[9].signature = "(I[B)V";
	nm[9].fnPtr = readPage;

	nm[10].name = "writePage";
	nm[10].signature = "(I[B)V";
	nm[10].fnPtr = writePage;


	(*env)->RegisterNatives(env,klass,nm,11);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

