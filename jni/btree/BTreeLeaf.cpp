#include "BTreeLeaf.h"

#include <android/log.h>
#include <stdlib.h>

#include "AVLTree.h"
#include "RecordInfo.h"

BTreeLeaf::BTreeLeaf(short freeSpace) {
	count = 0;
	type = BTreeElement::ELEMENT_LEAF;
	minId = -1;
	records = new AVLTree<RecordInfo>();
	this->freeSpace = freeSpace;
	parent = NULL;
}

BTreeLeaf::~BTreeLeaf() {
	delete records;
}

bool BTreeLeaf::add(int id, RecordInfo *record) {
	if (freeSpace < record->length)
		return false;

	count++;
	
	if (id < minId || minId == -1) {
		if (parent != NULL)
			parent->updateChild(minId, id);
		minId = id;
	}

//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	freeSpace -=  8 + record->length;
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d to leaf, new count: %d, new freeSpace: %d", id, 
//			count, freeSpace);
	records->add(id,record);
	return true;
}

bool BTreeLeaf::update(int id, RecordInfo *record) {
	AVLTreeNode<RecordInfo> *node = records->findNode(id);
	if (freeSpace < record->length - node->content->length)
		return false;

	freeSpace += node->content->length;
	freeSpace -= record->length;
	records->update(id,record,node);

	return true;
}

void BTreeLeaf::remove(int id) {
	int len = records->find(id)->length;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d (remove)", minId);
	if (records->remove(id)) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "remove succeded");
		count--;
		freeSpace += 8 + len;
		if (id == minId) {
			AVLTreeNode<RecordInfo> *smallest = records->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;
			if (parent != NULL)
				parent->updateChild(id , minId);
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d (remove)", minId);
		}
	}
}

RecordInfo *BTreeLeaf::get(int id) {
	return records->find(id);

}

void BTreeLeaf::split(BTreeLeaf *leaf) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Splitting leaf");
	int free = leaf->getFreeSpace();
	int target = free/2;

	int oldMinId = minId;

	while(free > target) {
		AVLTreeNode<RecordInfo> *n = records->extractSmallest();
		count--;
		freeSpace += 8 + n->content->length;
		free -= 8 + n->content->length;
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moving: %d", n->recordId);
		leaf->add(n->recordId, n->content);

	}

	AVLTreeNode<RecordInfo> *n = records->getSmallest();

	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;

	if (parent != NULL)
		parent->updateChild(oldMinId, minId);


	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");
}

void BTreeLeaf::join(BTreeElement *leaf) {
	AVLTree<RecordInfo> *r = ((BTreeLeaf*)leaf)->getAVLTree();
	AVLTreeNode<RecordInfo> *n = r->extractSmallest();

	while (n != NULL) {
		this->add(n->recordId, n->content);
		n = r->extractSmallest();
	}
}

AVLTree<RecordInfo> *BTreeLeaf::getAVLTree() {
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

