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
int prot;
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

jint JNICALL getPageSizeNative
(JNIEnv *env, jclass dis) {
	return getpagesize();
}

int length() {
	int cur = lseek(pagedFileFD, 0, SEEK_CUR);
	int end = lseek(pagedFileFD, 0, SEEK_END);
	lseek(pagedFileFD, cur, SEEK_SET);
	return end;

}

jint JNICALL lengthNative
(JNIEnv *env, jclass dis) {
	return length();
}

jint JNICALL openNative
(JNIEnv *env, jclass dis, jstring fullpath, jint ro) {
	const jbyte *str;
	pageSize = getpagesize();


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

	fileLength = length();

	if (fileLength > 0 && fileLength % pageSize != 0) {
		fileLength = ((fileLength / pageSize) + 1) * pageSize;
		ftruncate(pagedFileFD, fileLength);
	}

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

void JNICALL readFromPosition
(JNIEnv *env, jclass dis, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	jbyte *bytes;

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/io/IOException","Attempting to read from an empty file");

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) bytes, (void*) (mapping+position), length);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

}

void JNICALL setLength
(JNIEnv *env, jclass dis, jint len) {
	int oldLen = fileLength;

	if (readOnly)
		JNI_ThrowByName(env,"java/lang/IllegalAccessException","File opened in read-only mode");

	ftruncate(pagedFileFD, len);

	if (mapping != NULL) {
		munmap(mapping, fileLength);
	}

	fileLength = len;

	if (fileLength == 0)
		mapping = NULL;
	else
		mapFile(env);


}


void JNICALL writeOnPosition
(JNIEnv *env, jclass dis, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	jbyte *bytes;

	if (readOnly)
		JNI_ThrowByName(env,"java/lang/IllegalAccessException","File opened in read-only mode");

	if (mapping == NULL)
		JNI_ThrowByName(env,"java/io/IOException","Attempting to write to an empty file");

	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	memcpy((void*) (mapping+position), (void*) bytes, length);

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, JNI_ABORT);
}

void syncArea
	(JNIEnv *env, jclass dis, jint position, jint length) {
		if (mapping != NULL)
			msync((void*) (mapping+position), length, MS_SYNC | MS_INVALIDATE);
	}


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[8];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/store/PagedFile");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "getPageSizeNative";
	nm[0].signature = "()I";
	nm[0].fnPtr = getPageSizeNative;

	nm[1].name = "openNative";
	nm[1].signature = "(Ljava/lang/String;I)I";
	nm[1].fnPtr = openNative;

	nm[2].name = "closeNative";
	nm[2].signature = "()V";
	nm[2].fnPtr = closeNative;

	nm[3].name = "length";
	nm[3].signature = "()I";
	nm[3].fnPtr = lengthNative;

	nm[4].name = "syncArea";
	nm[4].signature = "(II)V";
	nm[4].fnPtr = syncArea;

	nm[5].name = "readFromPosition";
	nm[5].signature = "([BII)V";
	nm[5].fnPtr = readFromPosition;

	nm[6].name = "writeOnPosition";
	nm[6].signature = "([BII)V";
	nm[6].fnPtr = writeOnPosition;

	nm[7].name = "setLength";
	nm[7].signature = "(I)V";
	nm[7].fnPtr = setLength;

	(*env)->RegisterNatives(env,klass,nm,8);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

