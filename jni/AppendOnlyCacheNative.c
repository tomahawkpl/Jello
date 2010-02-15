#include <jni.h>
#include <stdlib.h>
#include "common.c"

struct CacheElement {
	long id;
	int freeSpace;
	struct CacheElement *next;
	struct CacheElement *prev;
};

struct CacheElement *cache;
int count;
int limit;

void JNICALL init
(JNIEnv *env, jclass dis, jint cacheSize) {
	cache = NULL;
	count = 0;
	limit = cacheSize;
}

jint JNICALL getBestFreeSpace
(JNIEnv *env, jclass dis) {
	if (count > 0)
		return cache->freeSpace;
	else
		return -1;

}

jlong JNICALL getBestId
(JNIEnv *env, jclass dis) {
	if (count > 0)
		return cache->id;
	else
		return -1;

}

jboolean JNICALL isEmpty
(JNIEnv *env, jclass dis) {
	if (count > 0)
		return (jboolean)0;
	else
		return (jboolean)1;
}

void removeElem(struct CacheElement *element) {
	count--;
	if (element->prev != NULL)
		element->prev->next = element->next;
	if (element->next != NULL)
		element->next->prev = element->prev;
	if (element == cache)
		cache = element->next;

}

void insertElem(struct CacheElement *element) {
	struct CacheElement *e;
	count++;
	if (cache == NULL) {
		element->next = NULL;
		element->prev = NULL;
		cache = element;
		return;
	}

	if (cache->freeSpace < element->freeSpace) {
		element->next = cache;
		element->prev = NULL;
		cache->prev = element;
		cache = element;
		return;
	}

	e = cache;

	while (1) {
		if (e->next != NULL && element->freeSpace < e->next->freeSpace) {
			e->next->prev = element;
			element->next = e->next;
			element->prev = e;
			e->next = element;
			return;
		}
		
		if (e->next != NULL)
			e = e->next;
		else
			break;
	}

	e->next = element;
	element->prev = e;
	element->next = NULL;



}

void JNICALL update
(JNIEnv *env, jclass dis, jlong id, jint freeSpace) {
	int removed = 0;
	int leastSpace = -1;
	struct CacheElement *leastSpaceElem = NULL;
	struct CacheElement *element = cache;


	while (element != NULL) {
		if (element->id == id) {
			removed = 1;
			removeElem(element);
			break;
		}

		if (count == limit && (leastSpace == -1 || element->freeSpace < leastSpace)) {
			leastSpace = element->freeSpace;
			leastSpaceElem = element;
		}

		element = element->next;
	}

	if (freeSpace == 0) {
		if (element != NULL)
			free(element);
		return;
	}

	if (removed == 0 && leastSpace < freeSpace && leastSpace != -1) {
		removeElem(leastSpaceElem);
		element = leastSpaceElem;
		removed = 1;
	}


	if (count < limit) {
		if (removed == 0)
			element = malloc(sizeof(struct CacheElement));
		element->id = id;
		element->freeSpace = freeSpace;
		insertElem(element);
	} else
		if (element != NULL)
			free(element);


}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[5];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/store/AppendOnlyCacheNative");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "getBestFreeSpace";
	nm[0].signature = "()I";
	nm[0].fnPtr = getBestFreeSpace;

	nm[1].name = "getBestId";
	nm[1].signature = "()J";
	nm[1].fnPtr = getBestId;

	nm[2].name = "isEmpty";
	nm[2].signature = "()Z";
	nm[2].fnPtr = isEmpty;

	nm[3].name = "update";
	nm[3].signature = "(JI)V";
	nm[3].fnPtr = update;

	nm[4].name = "init";
	nm[4].signature = "(I)V";
	nm[4].fnPtr = init;

	(*env)->RegisterNatives(env,klass,nm,5);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

