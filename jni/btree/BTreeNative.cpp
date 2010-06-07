#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#include "BTree.h"
#include "RecordInfo.h"

#include "misc.h"

int freeSpaceInfoSize;

jfieldID fidRecordId, fidRecordPagesUsed, fidRecordSchemaVersion;
jmethodID midRecordGetPageUsage, midRecordSetPagesUsed;
jfieldID fidPageUsagePageId, fidPageUsageUsage;


int bTreeLeafCapacity, bTreeNodeCapacity;

void convertRecordToRecordInfo(JNIEnv *env, jobject record, RecordInfo *recordInfo) {
	int pagesUsed = env->GetIntField(record, fidRecordPagesUsed);
	int schemaVersion = env->GetIntField(record, fidRecordSchemaVersion);
	recordInfo->length = 8 + pagesUsed * (4 + freeSpaceInfoSize);
	recordInfo->data = (uint8_t*)malloc(recordInfo->length);
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
	env->SetIntField(record, fidRecordSchemaVersion, schemaVersion);
	env->SetIntField(record, fidRecordPagesUsed, pagesUsed);
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

jint JNICALL init(JNIEnv *env, jclass dis, jobject pagedFile, jobject pagePoolProxy, jobject spaceManagerPolicy,
		int freeSpaceInfoSizeArg, int bTreeLeafCapacityArg, int bTreeNodeCapacityArg, int klassIndexPageId) {
	initIDs(env);
	freeSpaceInfoSize = freeSpaceInfoSizeArg;

	bTreeLeafCapacity = bTreeLeafCapacityArg;
	bTreeNodeCapacity = bTreeNodeCapacityArg;

	return (int)new BTree(bTreeLeafCapacity, bTreeNodeCapacity, pagedFile, pagePoolProxy, spaceManagerPolicy, env,
			klassIndexPageId);

}


jboolean JNICALL load(JNIEnv *env, jclass dis, jint tree) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "using btree %d ", tree);
	return ((BTree*)tree)->load();
}

void JNICALL commit(JNIEnv *env, jclass dis, jint tree) {
	((BTree*)tree)->commit();
}

void JNICALL remove(JNIEnv *env, jclass dis, jint tree, jint id) {
	((BTree*)tree)->remove(id);
}

void JNICALL insert(JNIEnv *env, jclass dis, jint tree, jobject record) {
	RecordInfo *recordInfo = new RecordInfo((BTree*)tree);

	int id = env->GetIntField(record, fidRecordId);
	convertRecordToRecordInfo(env, record, recordInfo);

	((BTree*)tree)->add(id, recordInfo);
}

jboolean JNICALL find(JNIEnv *env, jclass dis, jint tree, jobject record) {
	int id = env->GetIntField(record, fidRecordId);
	RecordInfo *recordInfo = ((BTree*)tree)->find(id);
	if (recordInfo == NULL)
		return JNI_FALSE;

	convertRecordInfoToRecord(env, record, recordInfo);

	return JNI_TRUE;
}

void JNICALL update(JNIEnv *env, jclass dis, jint tree, jobject record) {
	RecordInfo *recordInfo = new RecordInfo((BTree*)tree);

	int id = env->GetIntField(record, fidRecordId);
	convertRecordToRecordInfo(env, record, recordInfo);

	((BTree*)tree)->update(id, recordInfo);
}

void JNICALL debug(JNIEnv *env, jclass dis, jint tree) {
	((BTree*)tree)->debug();
}

void JNICALL iterate(JNIEnv *env, jclass dis, jint tree) {
	((BTree*)tree)->iterate();
}

jint JNICALL nextId(JNIEnv *env, jclass dis, jint tree) {
	((BTree*)tree)->getNextId();
}


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	JNINativeMethod nm[10];
	jclass klass;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	/* get class with (*env)->FindClass */
	klass = env->FindClass("com/atteo/jello/index/BTree");
	/* register methods with (*env)->RegisterNatives */

	nm[0].name = "loadNative";
	nm[0].signature = "(I)Z";
	nm[0].fnPtr = (void*)load;

	nm[1].name = "commitNative";
	nm[1].signature = "(I)V";
	nm[1].fnPtr = (void*)commit;

	nm[2].name = "removeNative";
	nm[2].signature = "(II)V";
	nm[2].fnPtr = (void*)remove;

	nm[3].name = "insertNative";
	nm[3].signature = "(ILcom/atteo/jello/Record;)V";
	nm[3].fnPtr = (void*)insert;

	nm[4].name = "findNative";
	nm[4].signature = "(ILcom/atteo/jello/Record;)Z";
	nm[4].fnPtr = (void*)find;

	nm[5].name = "updateNative";
	nm[5].signature = "(ILcom/atteo/jello/Record;)V";
	nm[5].fnPtr = (void*)update;

	nm[6].name = "init";
	nm[6].signature = "(Lcom/atteo/jello/store/PagedFile;Lcom/atteo/jello/index/PagePoolProxy;Lcom/atteo/jello/space/SpaceManagerPolicy;IIII)I";
	nm[6].fnPtr = (void*)init;

	nm[7].name = "debugNative";
	nm[7].signature = "(I)V";
	nm[7].fnPtr = (void*)debug;

	nm[8].name = "iterateNative";
	nm[8].signature = "(I)V";
	nm[8].fnPtr = (void*)iterate;

	nm[9].name = "nextIdNative";
	nm[9].signature = "(I)I";
	nm[9].fnPtr = (void*)nextId;

	env->RegisterNatives(klass,nm,10);

	return JNI_VERSION_1_4;
}

