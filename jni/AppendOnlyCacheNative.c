#include <jni.h>
#include <stdlib.h>
#include "common.c"

struct CacheElement {
	int id;
	short freeSpace;
	struct CacheElement *next;
	struct CacheElement *prev;
};

struct CacheElement *cache;
int count;
int limit;
int minFreeSpace;

void JNICALL init(JNIEnv *env, jclass dis, jint cacheSize) {
	cache = NULL;
	count = 0;
	limit = cacheSize;
	minFreeSpace = -1;
}

jint JNICALL getFreeSpace(JNIEnv *env, jclass dis, jint id) {
	struct CacheElement *element = cache;

	while (element != NULL) {
		if (element->id == id)
			return element->freeSpace;
		element = element->next;
	}

	return -1;

}

jint JNICALL getBestId(JNIEnv *env, jclass dis, jshort freeSpace) {
	struct CacheElement *element = cache;
	struct CacheElement *best = NULL;
	int bestExtraSpace;

	while (element != NULL) {
		if (element->freeSpace >= freeSpace &&
				(best == NULL || bestExtraSpace > (element->freeSpace - freeSpace))) {
			bestExtraSpace = element->freeSpace - freeSpace;
			best = element;
		}
		element = element->next;
	}


	if (best != NULL)
		return best->id;
	else
		return -1;

}

jboolean JNICALL isEmpty(JNIEnv *env, jclass dis) {
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

	element->next = cache;
	element->prev = NULL;
	if (cache != NULL)
		cache->prev = element;
	cache = element;


}

void JNICALL update(JNIEnv *env, jclass dis, jint id, jshort freeSpace) {
	int leastSpace = -1;
	struct CacheElement *leastSpaceElem = NULL;
	struct CacheElement *element = cache;

	// check if already is in cache

	while (element != NULL) {
		if (element->id == id) {
			if (freeSpace == 0) {
				removeElem(element);
				free(element);
			} else
				element->freeSpace = freeSpace;
			return;
		}
		element = element->next;
	}

	if (freeSpace == 0)
		return;

	// is not in cache and freeSpace > 0

	// cache is not full
	if (count < limit) {
		element = malloc(sizeof(struct CacheElement));
		element->id = id;
		element->freeSpace = freeSpace;
		insertElem(element);
		return;
	}

	// cache is full
	
	// updated page has less space than any page in cache
	if (minFreeSpace != -1 && freeSpace > minFreeSpace)
		return;

	element = cache;

	while (element != NULL) {
		if (count == limit && (leastSpace == -1 || element->freeSpace < leastSpace)) {
			leastSpace = element->freeSpace;
			leastSpaceElem = element;
		}
		element = element->next;
	}

	if (freeSpace <= leastSpace) {
		minFreeSpace = leastSpace;
		return;
	}

	minFreeSpace = -1;

	removeElem(leastSpaceElem);
	element = leastSpaceElem;

	element->id = id;
	element->freeSpace = freeSpace;

	insertElem(element);

}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[5];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/space/AppendOnlyCacheNative");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "getFreeSpace";
	nm[0].signature = "(I)S";
	nm[0].fnPtr = getFreeSpace;

	nm[1].name = "getBestId";
	nm[1].signature = "(S)I";
	nm[1].fnPtr = getBestId;

	nm[2].name = "isEmpty";
	nm[2].signature = "()Z";
	nm[2].fnPtr = isEmpty;

	nm[3].name = "update";
	nm[3].signature = "(IS)V";
	nm[3].fnPtr = update;

	nm[4].name = "init";
	nm[4].signature = "(I)V";
	nm[4].fnPtr = init;

	(*env)->RegisterNatives(env,klass,nm,5);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

