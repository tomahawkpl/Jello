#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#include "BTree.h"
#include "RecordInfo.h"

BTree *tree;

int freeSpaceInfoSize;

jfieldID fidRecordId, fidRecordPagesUsed, fidRecordSchemaVersion;
jmethodID midRecordGetPageUsage, midRecordSetPagesUsed;
jfieldID fidPageUsagePageId, fidPageUsageUsage;

void intToBytes(int i, unsigned char *data) {
	data[0] = i >> 0 & 0xFF;
	data[1] = i >> 8 & 0xFF;
	data[2] = i >> 16 & 0xFF;
	data[3] = i >> 24 & 0xFF;
}

void bytesToInt(int &i, unsigned char *data) {
	i = 0;
	i = data[3];
	i = (i << 8) | data[2];
	i = (i << 8) | data[1];
	i = (i << 8) | data[0];
}

void convertRecordToRecordInfo(JNIEnv *env, jobject record, RecordInfo *recordInfo) {
	int pagesUsed = env->GetIntField(record, fidRecordPagesUsed);
	int schemaVersion = env->GetIntField(record, fidRecordSchemaVersion);
	recordInfo->length = 8 + pagesUsed * (4 + freeSpaceInfoSize);
	recordInfo->data = (unsigned char*)malloc(recordInfo->length);
	intToBytes(schemaVersion, recordInfo->data);
	intToBytes(pagesUsed, recordInfo->data + 4);

	int pos = 8;
	jbyte *bytes;
	jbyteArray buffer;
	jboolean isCopy;

	for (int i=0;i<pagesUsed;i++) {
		jobject pageUsage = env->CallObjectMethod(record, midRecordGetPageUsage, i);
		int pageId = env->GetIntField(pageUsage, fidPageUsagePageId);

		intToBytes(pageId, recordInfo->data + pos);
		buffer = (jbyteArray)env->GetObjectField(pageUsage, fidPageUsageUsage);

		bytes = env->GetByteArrayElements(buffer, &isCopy);

		memcpy((void*) &recordInfo->data[pos + 4], (void*) bytes, freeSpaceInfoSize);

		env->ReleaseByteArrayElements(buffer, bytes, 0);

		pos += 4 + freeSpaceInfoSize;
	}

}

void convertRecordInfoToRecord(JNIEnv *env, jobject record, RecordInfo *recordInfo) {
	int id = env->GetIntField(record, fidRecordId);
	int pagesUsed, schemaVersion;
	bytesToInt(schemaVersion, recordInfo->data);
	bytesToInt(pagesUsed, recordInfo->data + 4);
	env->SetIntField(record, fidRecordId, id);
	env->SetIntField(record, fidRecordPagesUsed, pagesUsed);
	env->SetIntField(record, fidRecordSchemaVersion, schemaVersion);
	env->CallVoidMethod(record, midRecordSetPagesUsed, pagesUsed);

	int pos = 8;
	int pageId;
	jbyte *bytes;
	jbyteArray buffer;
	jboolean isCopy;

	for (int i=0;i<pagesUsed;i++) {
		jobject pageUsage = env->CallObjectMethod(record, midRecordGetPageUsage, i);

		bytesToInt(pageId, recordInfo->data + pos);
		env->SetIntField(pageUsage, fidPageUsagePageId, pageId);
		buffer = (jbyteArray)env->GetObjectField(pageUsage, fidPageUsageUsage);

		bytes = env->GetByteArrayElements(buffer, &isCopy);

		memcpy((void*) bytes, (void*) &recordInfo->data[pos + 4], freeSpaceInfoSize);

		env->ReleaseByteArrayElements(buffer, bytes, 0);

		pos += 4 + freeSpaceInfoSize;
	}
}

void initIDs(JNIEnv *env) {
	jclass klass;

	klass = env->FindClass("com/atteo/jello/Record");
	if (klass == NULL)
		return;

	fidRecordId = env->GetFieldID(klass, "id", "I");
	if (fidRecordId == NULL)
		return;

	fidRecordPagesUsed = env->GetFieldID(klass, "pagesUsed", "I");
	if (fidRecordPagesUsed == NULL)
		return;

	midRecordGetPageUsage = env->GetMethodID(klass, "getPageUsage", "(I)Lcom/atteo/jello/PageUsage;");
	if (midRecordGetPageUsage == NULL)
		return;

	midRecordSetPagesUsed = env->GetMethodID(klass, "setPagesUsed", "(I)V");
	if (midRecordSetPagesUsed == NULL)
		return;

	fidRecordSchemaVersion = env->GetFieldID(klass, "schemaVersion", "I");
	if (fidRecordSchemaVersion == NULL)
		return;

	klass = env->FindClass("com/atteo/jello/PageUsage");
	if (klass == NULL)
		return;

	fidPageUsagePageId = env->GetFieldID(klass, "pageId", "I");
	if (fidPageUsagePageId == NULL)
		return;

	fidPageUsageUsage = env->GetFieldID(klass, "usage", "[B");
	if (fidPageUsageUsage == NULL)
		return;
}

void JNICALL init(JNIEnv *env, jclass dis,
		int freeSpaceInfoSizeArg, short bTreeLeafCapacity, short bTreeNodeCapacity) {
	initIDs(env);
	freeSpaceInfoSize = freeSpaceInfoSizeArg;

	tree = new BTree(bTreeLeafCapacity, bTreeNodeCapacity);
}


void JNICALL create(JNIEnv *env, jclass dis, jint id) {
}

void JNICALL load(JNIEnv *env, jclass dis, jint id) {
}

void JNICALL remove(JNIEnv *env, jclass dis, jint id) {
	tree->remove(id);
}

void JNICALL insert(JNIEnv *env, jclass dis, jobject record) {
	RecordInfo *recordInfo = new RecordInfo();

	int id = env->GetIntField(record, fidRecordId);
	convertRecordToRecordInfo(env, record, recordInfo);

	tree->add(id, recordInfo);
}

jboolean JNICALL find(JNIEnv *env, jclass dis, jobject record) {
	int id = env->GetIntField(record, fidRecordId);
	RecordInfo *recordInfo = tree->find(id);
	if (recordInfo == NULL)
		return JNI_FALSE;
	
	convertRecordInfoToRecord(env, record, recordInfo);

	return JNI_TRUE;
}

void JNICALL update(JNIEnv *env, jclass dis, jobject record) {
	RecordInfo *recordInfo = new RecordInfo();

	int id = env->GetIntField(record, fidRecordId);
	convertRecordToRecordInfo(env, record, recordInfo);

	tree->update(id, recordInfo);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[7];
	jclass klass;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = env->FindClass("com/atteo/jello/index/BTree");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "create";
	nm[0].signature = "(I)V";
	nm[0].fnPtr = (void*)create;

	nm[1].name = "load";
	nm[1].signature = "(I)V";
	nm[1].fnPtr = (void*)load;

	nm[2].name = "remove";
	nm[2].signature = "(I)V";
	nm[2].fnPtr = (void*)remove;

	nm[3].name = "insert";
	nm[3].signature = "(Lcom/atteo/jello/Record;)V";
	nm[3].fnPtr = (void*)insert;

	nm[4].name = "find";
	nm[4].signature = "(Lcom/atteo/jello/Record;)Z";
	nm[4].fnPtr = (void*)find;

	nm[5].name = "update";
	nm[5].signature = "(Lcom/atteo/jello/Record;)V";
	nm[5].fnPtr = (void*)update;

	nm[6].name = "init";
	nm[6].signature = "(ISS)V";
	nm[6].fnPtr = (void*)init;

	env->RegisterNatives(klass,nm,7);

	return JNI_VERSION_1_4;
}

