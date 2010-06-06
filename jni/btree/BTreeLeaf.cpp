#include "BTreeLeaf.h"

#include <android/log.h>
#include <stdlib.h>

#include "BTree.h"
#include "AVLTree.h"
#include "misc.h"
#include "PageIds.h"
#include "RecordInfo.h"
#include "RecordInfoFactory.h"

BTreeLeaf::BTreeLeaf(BTree *btree, short freeSpace) : BTreeElement(btree) {
	count = 0;
	type = BTreeElement::ELEMENT_LEAF;
	minId = -1;
	records = new AVLTree(new RecordInfoFactory(btree));
	this->freeSpace = freeSpace - 16;
	parent = NULL;
}

BTreeLeaf::~BTreeLeaf() {
	delete records;
}

bool BTreeLeaf::add(int id, RecordInfo *record) {
	if (freeSpace < 20 + record->length)
		return false;

	count++;
	
	if (id < minId || minId == -1) {
		if (parent != NULL)
			parent->updateChild(minId, id);
		minId = id;
	}

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	freeSpace -= 20 + record->length;
	if (count == 1)
		freeSpace -= 4;
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d to leaf, new count: %d, new freeSpace: %d", id, 
	//		count, freeSpace);
	records->add(id,record);
	return true;
}

bool BTreeLeaf::update(int id, RecordInfo *record) {
	AVLTreeNode *node = records->findNode(id);
	if (freeSpace < record->length - node->content->getLength())
		return false;

	freeSpace += node->content->getLength();
	freeSpace -= record->length;
	records->update(id,record,node);

	return true;
}

void BTreeLeaf::remove(int id) {
	int len = records->find(id)->getLength();
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d (remove)", minId);
	if (records->remove(id)) {
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "remove succeded");

		count--;
		freeSpace += 20 + len;
		if (count == 0)
			freeSpace += 4;

		if (id == minId) {
			AVLTreeNode *smallest = records->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;
			if (parent != NULL)
				parent->updateChild(id , minId);
			//__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d (remove)", minId);
		}
	}
}

RecordInfo *BTreeLeaf::get(int id) {
	return (RecordInfo*)records->find(id);

}

void BTreeLeaf::split(BTreeLeaf *leaf) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Splitting leaf");
	int free = leaf->getFreeSpace();
	int target = free/2;

	int oldMinId = minId;

	while(leaf->getFreeSpace() > target) {
		AVLTreeNode *n = records->extractSmallest();
		count--;
		freeSpace += 20 + n->content->getLength();
		if (count == 0)
			freeSpace += 4;

		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moving: %d", n->recordId);
		leaf->add(n->recordId, (RecordInfo*)n->content);

	}

	AVLTreeNode *n = records->getSmallest();

	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;

	if (parent != NULL)
		parent->updateChild(oldMinId, minId);


	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");
}

void BTreeLeaf::join(BTreeElement *leaf) {
	AVLTree *r = ((BTreeLeaf*)leaf)->getAVLTree();
	AVLTreeNode *n = r->extractSmallest();

	while (n != NULL) {
		this->add(n->recordId, (RecordInfo*)n->content);
		n = r->extractSmallest();
	}
}

AVLTree *BTreeLeaf::getAVLTree() {
	return records;
}

void BTreeLeaf::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== Leaf (%d), minId: %d, parent: %d, freeSpace: %d",
			this, minId, parent, freeSpace);
	if (parent != NULL)
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "   left: %d, right: %d",
				parent->getAVLTree()->findLeft(minId), parent->getAVLTree()->findRight(minId));

	records->debug(false);
}

BTreeLeaf *BTreeLeaf::fromBytes(uint8_t *bytes, int leafCapacity, BTree *btree) {
	BTreeLeaf *leaf = new BTreeLeaf(btree, leafCapacity);
	int  minId, freeSpace, count;
	bytesToInt(minId, bytes + 4);
	bytesToInt(freeSpace, bytes + 8);
	bytesToInt(count, bytes + 12);

	leaf->setMinId(minId);
	leaf->setFreeSpace(freeSpace);
	leaf->setCount(count);

	leaf->getAVLTree()->fromBytes(bytes + 16, leaf);

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "leaf read");

	return leaf;
}

int BTreeLeaf::commit() {
	jobject page = BTree::env->CallObjectMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyAcquire);
	PageIds *pageIds = btree->getPageIds();
	int pageId = pageIds->get();

	jboolean isCopy;
	uint8_t *bytes;
	jbyteArray buffer;

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "commiting leaf on page %d", pageId);
	BTree::env->SetIntField(page, BTree::fidPageId, pageId);
	buffer = (jbyteArray)BTree::env->GetObjectField(page, BTree::fidPageData);
	bytes = (uint8_t*) BTree::env->GetByteArrayElements(buffer, &isCopy);
	intToBytes(type, bytes);
	intToBytes(minId, bytes + 4);
	intToBytes(freeSpace, bytes + 8);
	intToBytes(count, bytes + 12);
	records->commit(bytes + 16);

	BTree::env->ReleaseByteArrayElements(buffer, (jbyte*)bytes, JNI_ABORT);
	BTree::env->CallVoidMethod(BTree::pagedFile, BTree::midPagedFileWritePage, page);

	BTree::env->CallVoidMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyRelease, page);

	return pageId;
}

