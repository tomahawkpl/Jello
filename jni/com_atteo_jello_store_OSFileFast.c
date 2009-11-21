#include <jni.h>
#include <unistd.h>
#include <sys/types.h>
#include <errno.h>

/*
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    openNative
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_atteo_jello_store_OSFileFast_openNative (JNIEnv *env, jobject dis, jstring fullpath) {

	
}

/*
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    lengthNative
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_atteo_jello_store_OSFileFast_lengthNative(JNIEnv *env, jobject dis, jint fd) {
	int cur = lseek((int) fd, 0, SEEK_CUR);
	int end = lseek((int) fd, 0, SEEK_END);
	lseek((int) fd, cur, SEEK_SET);
	return end;
}

/*
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    readFromPositionNative
 * Signature: (I[BII)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_OSFileFast_readFromPositionNative(JNIEnv *env, jobject dis,
		jint fd, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	int r = 0;
	int count = 0;
	jbyte *bytes;
	lseek(fd, position, SEEK_SET);
	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	while (length > 0) {
		r = read(fd, (void *) (bytes + count), length);
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
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    writeOnPositionNative
 * Signature: (I[BII)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_OSFileFast_writeOnPositionNative (JNIEnv *env, jobject dis,
		jint fd, jbyteArray buffer, jint position, jint length) {
	jboolean isCopy;
	jlong result;
	jbyte *bytes;
	bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);

	lseek(fd, position, SEEK_SET);
	for (;;) {
		result = write(fd, (const char *) bytes, (int) length);

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

/*
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    closeNative
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_OSFileFast_closeNative(JNIEnv *env, jobject dis, jint fd) {
	jint result;

	for (;;) {
		result = (jint) close((int)fd);

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
 * Class:     com_atteo_jello_store_OSFileFast
 * Method:    setLengthNative
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_atteo_jello_store_OSFileFast_setLengthNative(JNIEnv *env, jobject dis,
		jint fd, jint len) {
	ftruncate((int)fd, len); 
}

