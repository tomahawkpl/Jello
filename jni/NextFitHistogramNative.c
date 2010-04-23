#include <jni.h>
#include <stdlib.h>
#include "common.c"

int *witnesses, *counts;

int NextFitHistogramNoWitness, NextFitHistogramNoPage;
int count;

short classSize;
int histogramClasses;

void initIDs(JNIEnv *env) {
	jclass nextFitHistogramClass;
	jfieldID fidNextFitHistogramNoWitness, fidNextFitHistogramNoPage;

	nextFitHistogramClass = (*env)->FindClass(env, "com/atteo/jello/space/NextFitHistogram");
	if (nextFitHistogramClass == NULL)
		return;

	fidNextFitHistogramNoWitness = (*env)->GetStaticFieldID(env, nextFitHistogramClass,
			"NO_WITNESS", "I");
	if (fidNextFitHistogramNoWitness == NULL)
		return;
	NextFitHistogramNoWitness = (*env)->GetStaticIntField(env, nextFitHistogramClass, fidNextFitHistogramNoWitness);

	fidNextFitHistogramNoPage = (*env)->GetStaticFieldID(env, nextFitHistogramClass,
			"NO_PAGE", "I");
	if (fidNextFitHistogramNoPage == NULL)
		return;
	NextFitHistogramNoPage = (*env)->GetStaticIntField(env, nextFitHistogramClass, fidNextFitHistogramNoPage);


}

void JNICALL init(JNIEnv *env, jclass dis, jshort pageSize, jint classes) {
	int i;
	initIDs(env);

	if (pageSize % classes != 0)
		JNI_ThrowByName(env, "java/lang/IllegalArgumentException", "histogramClasses doesn't divide pageSize");

	classSize = (short) (pageSize / classes);

	classes++; // empty pages have a separate class

	histogramClasses = classes;
	count = 0;

	witnesses = malloc(histogramClasses * sizeof(int));
	counts = malloc(histogramClasses * sizeof(int));

	for (i = 0; i < histogramClasses; i++) {
		witnesses[i] = -1;
		counts[i] = 0;
	}
}

jint JNICALL getClassSize(JNIEnv *env, jclass dis) {
	return classSize;
}

jint JNICALL getWitness(JNIEnv *env, jclass dis, jshort freeSpace) {
	int loc = classFor(freeSpace);
	int found = 0;
	int w;
	loc++;
	if (loc == histogramClasses)
		loc = histogramClasses - 1;

	while (loc < histogramClasses) {
		if (counts[loc] > 0) {
			found = 1;
			w = witnesses[loc];
			if (w != -1)
				return w;
		}
		loc++;
	}

	if (found == 0)
		return NextFitHistogramNoPage;

	return NextFitHistogramNoWitness;

}

void JNICALL update(JNIEnv *env, jclass dis, jint id, short previousFreeSpace, short freeSpace) {
	int loc;
	if (previousFreeSpace != -1) {
		loc = classFor(previousFreeSpace);
		counts[loc]--;
		if (witnesses[loc] == id)
			witnesses[loc] = -1;

		count--;
	}

	if (freeSpace == -1)
		return;

	loc = classFor(freeSpace);
	counts[loc]++;
	witnesses[loc] = id;
	count++;
}

int classFor(int freeSpace) {
	return freeSpace / classSize;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[4];
	jclass klass;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = (*env)->FindClass(env,"com/atteo/jello/space/NextFitHistogramNative");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "getClassSize";
	nm[0].signature = "()S";
	nm[0].fnPtr = getClassSize;

	nm[1].name = "getWitness";
	nm[1].signature = "(S)I";
	nm[1].fnPtr = getWitness;

	nm[2].name = "update";
	nm[2].signature = "(ISS)V";
	nm[2].fnPtr = update;

	nm[3].name = "init";
	nm[3].signature = "(SI)V";
	nm[3].fnPtr = init;

	(*env)->RegisterNatives(env,klass,nm,4);

	(*env)->DeleteLocalRef(env, klass);


	return JNI_VERSION_1_4;
}

