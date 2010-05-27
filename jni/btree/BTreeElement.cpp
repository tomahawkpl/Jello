#include "BTreeElement.h"

#include <android/log.h>

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

void BTreeElement::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "DUMMY");
}
