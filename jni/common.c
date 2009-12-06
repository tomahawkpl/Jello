#include <jni.h>
#include <unistd.h>


void JNI_ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
	jclass cls = (*env)->FindClass(env, name);
	/* if cls is NULL, an exception has already been thrown */
	if (cls != NULL) {
		(*env)->ThrowNew(env, cls, msg);
	}
	/* free the local ref */
	(*env)->DeleteLocalRef(env, cls);
}

