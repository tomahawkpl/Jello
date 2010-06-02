#include "ChildInfo.h"

#include "misc.h"
#include <android/log.h>
#include "BTreeElement.h"
#include "BTreeNode.h"
#include "BTree.h"
#include "BTreeLeaf.h"
#include "PageIds.h"
#include <stdlib.h>

ChildInfo::~ChildInfo() {
	//delete child;
}

int ChildInfo::getLength() {
	return 4;
}

void ChildInfo::toBytes(uint8_t *bytes) {
	int pageId = child->commit();
	intToBytes(pageId, bytes);
}

void ChildInfo::fromBytes(uint8_t *bytes, int length, BTreeElement *node) {
	jobject page = BTree::env->CallObjectMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyAcquire);
	int pageId;
	bytesToInt(pageId, bytes);
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "%d", pageId);
	BTree::pageIds->add(pageId);

	jboolean isCopy;
	uint8_t *bytes2;
	jbyteArray buffer;

	BTree::env->SetIntField(page, BTree::fidPageId, pageId);
	BTree::env->CallVoidMethod(BTree::pagedFile, BTree::midPagedFileReadPage, page);

	buffer = (jbyteArray)BTree::env->GetObjectField(page, BTree::fidPageData);
	bytes2 = (uint8_t*)BTree::env->GetByteArrayElements(buffer, &isCopy);

	int type;
	bytesToInt(type, bytes2);
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "type: %d", type);


	if (type == BTreeElement::ELEMENT_NODE)
		child = BTreeNode::fromBytes(bytes2, BTree::nodeCapacity);
	else if (type == BTreeElement::ELEMENT_LEAF)
		child = BTreeLeaf::fromBytes(bytes2, BTree::leafCapacity);

	child->setParent((BTreeNode*)node);

	BTree::env->ReleaseByteArrayElements(buffer, (jbyte*)bytes2, JNI_ABORT);
	BTree::env->CallVoidMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyRelease, page);


}
