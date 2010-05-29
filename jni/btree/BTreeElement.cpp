#include "BTreeElement.h"

#include <android/log.h>

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

short BTreeElement::getFreeSpace() {
	return freeSpace;
}

int BTreeElement::getCount() {
	return count;
}

void BTreeElement::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "DUMMY");
}

