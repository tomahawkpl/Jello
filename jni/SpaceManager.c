#include <jni.h>
#include <stdlib.h>
#include "common.c"


struct FreeSpaceInfo {
	jlong pageId;
	jlong nextPageId;
	struct FreeSpaceInfo *next;
	unsigned char *data;
};

struct FreeSpaceInfo *freeSpaceInfo;

jint pageFreeSpaceMap;
jint freeSpaceMapPageCapacity;
jlong freeSpaceInfoSize;

jclass pagedFileClass;
jobject pagedFile;
jobject spaceManager;

void JNICALL initVariables(JNIEnv *env, jclass dis, jobject obj) {
	jclass klass;
	jfieldID fid;

	spaceManager = obj;
	pageFreeSpaceMap = getStaticLongField(env, "com/atteo/jello/store/DatabaseFile", "PAGE_FREE_SPACE_MAP");
	//freeSpaceMapPageCapacity = getIntField(env, obj, "freeSpaceMapPageCapacity");

	// get PagedFile class
	pagedFileClass = (*env)->FindClass(env, "com/atteo/jello/store/PagedFile");
	if (pagedFileClass == NULL)
		JNI_ThrowByName(env, "java/lang/ClassNotFoundException", "Couldn't find class (getStaticLongField)");

	// get PagedFile instance
	klass = (*env)->FindClass(env,"com/atteo/jello/store/SpaceManager");

	if (klass == NULL)
		JNI_ThrowByName(env, "java/lang/ClassNotFoundException", "Couldn't find class (getStaticLongField)");

	fid = (*env)->GetFieldID(env, klass, "pagedFile", "Lcom/atteo/jello/store/PagedFile;");

	if (fid == NULL)
		JNI_ThrowByName(env, "java/lang/NoSuchFieldException", "Couldn't find field (getStaticLongField)");

	pagedFile = (*env)->GetObjectField(env, spaceManager, fid);
}

void JNICALL create
(JNIEnv *env, jclass dis, jlong spaceStart) {
	jlong pageFreeSpaceMap;
	jmethodID mid;
	long i;
       	freeSpaceInfo = malloc(sizeof(struct FreeSpaceInfo));
	freeSpaceInfo->pageId = pageFreeSpaceMap;
	freeSpaceInfo->nextPageId = 0;

	
	mid = (*env)->GetMethodID(env, pagedFileClass, "writePage", "(Lcom/atteo/jello/store/Page;)V");

	if (mid == NULL)
		JNI_ThrowByName(env, "java/lang/NoSuchFieldMethod", "Couldn't find method (create)");

	(*env)->CallVoidMethod(env, pagedFile, mid, freeSpaceInfo[0]);
	
	
}


jobject JNICALL acquireRecordSpace
(JNIEnv *env, jclass dis, jint length) {

}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[2];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/store/SpaceManager");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "initVariables";
	nm[0].signature = "(Lcom/atteo/jello/store/SpaceManager;)V";
	nm[0].fnPtr = initVariables;

	nm[1].name = "create";
	nm[1].signature = "(J)V";
	nm[1].fnPtr = create;



	(*env)->RegisterNatives(env,klass,nm,2);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

