#include "BTreeElement.h"

#include <android/log.h>

BTreeElement::BTreeElement(BTree *btree) {
	this->btree = btree;
}

BTreeElement::~BTreeElement() {

}

int BTreeElement::getMinId() {
	return minId;
}

BTreeNode *BTreeElement::getParent() {
	return parent;
}

void BTreeElement::setParent(BTreeNode *node) {
	parent = node;
}

int BTreeElement::getFreeSpace() {
	return freeSpace;
}

int BTreeElement::getCount() {
	return count;
}

void BTreeElement::setCount(int count) {
	this->count = count;
}

void BTreeElement::setMinId(int minId) {
	this->minId = minId;
}
void BTreeElement::setFreeSpace(int freeSpace) {
	this->freeSpace = freeSpace;
}

void BTreeElement::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "DUMMY");
}

