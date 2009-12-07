#include <jni.h>
#include <unistd.h>

jint JNICALL getPageSize
(JNIEnv *env, jclass dis) {
	return getpagesize();
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[1];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/store/OSInfo");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "getPageSize";
	nm[0].signature = "()I";
	nm[0].fnPtr = getPageSize;

	(*env)->RegisterNatives(env,klass,nm,1);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}
