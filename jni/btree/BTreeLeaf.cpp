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
	right = left = NULL;
	parent = NULL;
}

BTreeLeaf::~BTreeLeaf() {
	delete records;
}

bool BTreeLeaf::add(int id, RecordInfo *record) {
	if (freeSpace < record->length)
		return false;

	count++;
	
	if (id < minId || minId == -1)
		minId = id;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	freeSpace -=  8 + record->length;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding %d to leaf, new count: %d, new freeSpace: %d", id, 
			count, freeSpace);
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
	if (records->remove(id)) {
		count--;
		freeSpace += 8 + len;
		if (id == minId) {
			AVLTreeNode<RecordInfo> *smallest = records->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
		}
	}
}

RecordInfo *BTreeLeaf::get(int id) {
	return records->find(id);

}

void BTreeLeaf::split(BTreeLeaf *leaf) {
	int free = leaf->getFreeSpace();
	int target = free/2;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split target: %d", target);
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
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");
}

void BTreeLeaf::join(BTreeLeaf *leaf) {
	AVLTreeNode<RecordInfo> *n = records->extractSmallest();

	while (n != NULL) {
		this->add(n->recordId, n->content);
		n = records->extractSmallest();
	}
}

BTreeLeaf *BTreeLeaf::getRight() {
	return right;
}

BTreeLeaf *BTreeLeaf::getLeft() {
	return left;
}


void BTreeLeaf::setRight(BTreeLeaf *leaf) {
	this->right = leaf;
}

void BTreeLeaf::setLeft(BTreeLeaf *leaf) {
	this->left = leaf;
}

void BTreeLeaf::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== Leaf (%d), minId: %d, parent: %d", this, minId, parent);
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "   left: %d, right: %d", left, right);

	records->debug(false);
}
