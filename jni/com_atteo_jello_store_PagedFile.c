#include <jni.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

int PagedFileFD;

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    getPageSizeNative
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_atteo_jello_store_PagedFile_getPageSizeNative
(JNIEnv *env, jclass dis) {
	return getpagesize();
}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    closeNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_PagedFile_closeNative
(JNIEnv *env, jclass dis) {
	jint result;

	for (;;) {
		result = (jint) close(PagedFileFD);

		if ((result != -1) || (errno != EINTR)) {
			break;
		}

		/*
		 * If we didn't break above, that means that the close() call
		 * returned due to EINTR. We shield Java code from this
		 * possibility by trying again.
		 */
	}
}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    length
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_atteo_jello_store_PagedFile_length
(JNIEnv *env, jclass dis) {
	int cur = lseek(PagedFileFD, 0, SEEK_CUR);
	int end = lseek(PagedFileFD, 0, SEEK_END);
	lseek(PagedFileFD, cur, SEEK_SET);
	return end;
}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    openNative
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_atteo_jello_store_PagedFile_openNative
(JNIEnv *env, jclass dis, jstring fullpath) {
	const jbyte *str;
	str = (*env)->GetStringUTFChars(env, fullpath, NULL);
	PagedFileFD = open(str,O_RDWR);
	(*env)->ReleaseStringUTFChars(env, fullpath, str);

	return PagedFileFD;

}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    readFromPosition
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_PagedFile_readFromPosition
(JNIEnv *env, jclass dis, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	int r = 0;
	int count = 0;
	jbyte *bytes;
	lseek(PagedFileFD, position, SEEK_SET);
	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	while (length > 0) {
		r = read(PagedFileFD, (void *) (bytes + count), length);
		length -= r;
		count += r;
		if ((r != -1) || (errno != EINTR)) {
			break;
		}

		/*
		 * If we didn't break above, that means that the read() call
		 * returned due to EINTR. We shield Java code from this
		 * possibility by trying again. Note that this is different
		 * from EAGAIN, which should result in this code throwing
		 * an InterruptedIOException.
		 */
	}

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

	if (r == 0) {
		return;
	}
}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    setLength
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_PagedFile_setLength
(JNIEnv *env, jclass dis, jint len) {
	ftruncate(PagedFileFD, len);
}

/*
 * Class:     com_atteo_jello_store_PagedFile
 * Method:    writeOnPosition
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_PagedFile_writeOnPosition
(JNIEnv *env, jclass dis, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	jlong result;
	jbyte *bytes;
	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	lseek(PagedFileFD, position, SEEK_SET);
	for (;;) {
		result = write(PagedFileFD, (const char *) bytes, (int) length);

		if ((result != -1) || (errno != EINTR)) {
			break;
		}

		/*
		 * If we didn't break above, that means that the read() call
		 * returned due to EINTR. We shield Java code from this
		 * possibility by trying again. Note that this is different
		 * from EAGAIN, which should result in this code throwing
		 * an InterruptedIOException.
		 */
	}

	(*env)->ReleaseByteArrayElements(env, buffer, bytes, JNI_ABORT);
}
