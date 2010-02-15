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

jlong getStaticLongField(JNIEnv *env, const char *className, const char *fieldName) {
	jfieldID fid;
	jclass klass;

	klass = (*env)->FindClass(env,klass);

	if (klass == NULL)
		JNI_ThrowByName(env, "java/lang/ClassNotFoundException", "Couldn't find class (getStaticLongField)");

	fid = (*env)->GetStaticFieldID(env, klass, fieldName, "J");

	if (fid == NULL)
		JNI_ThrowByName(env, "java/lang/NoSuchFieldException", "Couldn't find field (getStaticLongField)");
	return (*env)->GetStaticLongField(env, klass, fid);

}
