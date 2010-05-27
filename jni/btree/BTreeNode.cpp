#include "BTreeNode.h"

#include <android/log.h>

#include <stdlib.h>

#include "AVLTree.h"
#include "ChildInfo.h"

BTreeNode::BTreeNode(int nodeCapacity) {
	type = BTreeElement::ELEMENT_NODE;
	freeSpace = nodeCapacity;
	children = new AVLTree<ChildInfo>();
	parent = NULL;
	minId = -1;
}

BTreeNode::~BTreeNode() {
	delete children;
}

BTreeElement *BTreeNode::getSubNodeFor(int id) {
	ChildInfo *info = children->findHigher(id);

	if (info != NULL)
		return info->child;
	else
		return NULL;
}

bool BTreeNode::addChild(int id, BTreeElement *child) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "BTree node add %d child:%d", id, child);
	if (freeSpace < 8)
		return false;

	if (id < minId || minId == -1)
		minId = id;

	freeSpace -= 8;

	child->setParent(this);

	ChildInfo *info = new ChildInfo();
	info->child = child;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "minId: %d", minId);
	children->add(id, info);
}

void BTreeNode::removeChild(int id) {
	if (children->remove(id)) {
		freeSpace += 8;

		if (id == minId) {
			AVLTreeNode<ChildInfo> *smallest = children->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;
		}
	}


}

void BTreeNode::updateChild(int oldMinId, int minId) {
	ChildInfo *info = children->find(oldMinId);

	if (info == NULL)
		return;

	ChildInfo *copy = (ChildInfo*)malloc(sizeof(ChildInfo));
	memcpy(copy, info, sizeof(ChildInfo));

	if (!children->remove(oldMinId))
		return;

	children->add(minId, copy);
}

void BTreeNode::split(BTreeNode *node) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Splitting node");
	int free = node->getFreeSpace();
	int target = free/2;
	while(free > target) {
		AVLTreeNode<ChildInfo> *n = children->extractSmallest();
		freeSpace += 8;
		free -= 8;
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moving %d", n->recordId);
		node->addChild(n->recordId, n->content->child);
	}

	AVLTreeNode<ChildInfo> *n = children->getSmallest();

	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");

}

void BTreeNode::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== Node (%d), minId: %d, parent: %d", this, minId, parent);

	children->debug(true);
	
}
