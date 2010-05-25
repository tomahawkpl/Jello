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
}

BTreeLeaf::~BTreeLeaf() {
	delete records;
}

short BTreeLeaf::getFreeSpace() {
	return freeSpace;
}

bool BTreeLeaf::add(int id, RecordInfo *record) {
	if (freeSpace < record->length)
		return false;

	count++;
	
	if (id < minId || minId == -1)
		minId = id;

	freeSpace -=  8 + record->length;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "adding to leaf, new count: %d, new freeSpace: %d",
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
		}
	}
}

RecordInfo *BTreeLeaf::get(int id) {
	return records->find(id);

}

void BTreeLeaf::split(BTreeLeaf *leaf) {
	int free = leaf->getFreeSpace();
	int target = free/2;
	while(free > target) {
		AVLTreeNode<RecordInfo> *n = records->extractSmallest();
		freeSpace += 8 + n->content->length;
		free -= 8 + n->content->length;
		leaf->add(n->recordId, n->content);
	}

	AVLTreeNode<RecordInfo> *n = records->extractSmallest();

	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;
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

BTreeNode *BTreeLeaf::getParent() {
	return parent;
}

void BTreeLeaf::setRight(BTreeLeaf *leaf) {
	this->right = leaf;
}

void BTreeLeaf::setLeft(BTreeLeaf *leaf) {
	this->left = leaf;
}

void BTreeLeaf::setParent(BTreeNode *parent) {
	this->parent = parent;
}
