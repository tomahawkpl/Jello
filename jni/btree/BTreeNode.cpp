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
	count = 0;
}

BTreeNode::~BTreeNode() {
	delete children;
}

BTreeElement *BTreeNode::getSmallest() {
	AVLTreeNode<ChildInfo> *node = children->getSmallest();
	if (node == NULL)
		return NULL;
	return node->content->child;
}

BTreeElement *BTreeNode::getSubNodeFor(int id) {
	ChildInfo *info = children->findHigher(id);

	if (info != NULL)
		return info->child;
	else
		return NULL;
}

bool BTreeNode::addChild(int id, BTreeElement *child) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) BTree node add %d child:%d", this, id, child);
	if (freeSpace < 8)
		return false;

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId0: %d", this,  minId);

	if (id < minId || minId == -1) {
		if (parent != NULL)
			parent->updateChild(minId, id);
		minId = id;
	}


	freeSpace -= 8;
	count++;

	child->setParent(this);

	ChildInfo *info = new ChildInfo();
	info->child = child;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId1: %d", this,  minId);
	children->add(id, info);
}

void BTreeNode::removeChild(int id) {
	if (children->remove(id)) {
		freeSpace += 8;
		count--;

		if (id == minId) {
			AVLTreeNode<ChildInfo> *smallest = children->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;

			__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId2: %d", this,  minId);
			if (parent != NULL)
				parent->updateChild(id, minId);
		}
	}


}

void BTreeNode::updateChild(int oldMinId, int newMinId) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) updating child: %d -> %d", this, oldMinId, newMinId);
	ChildInfo *info = children->find(oldMinId);

	if (info == NULL)
		return;

	ChildInfo *copy = (ChildInfo*)malloc(sizeof(ChildInfo));
	memcpy(copy, info, sizeof(ChildInfo));

	if (!children->remove(oldMinId))
		return;

	children->add(newMinId, copy);

	oldMinId = minId;
	AVLTreeNode<ChildInfo> *smallest = children->getSmallest();
	if (smallest == NULL)
		minId = -1;
	else
		minId = smallest->recordId;


	if (oldMinId != minId && parent != NULL) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId3: %d", this, minId);
		parent->updateChild(oldMinId, minId);
	}

}

void BTreeNode::join(BTreeElement *node) {
	AVLTree<ChildInfo> *r = ((BTreeNode*)node)->getAVLTree();
	AVLTreeNode<ChildInfo> *n = r->extractSmallest();

	while (n != NULL) {
		this->addChild(n->recordId, n->content->child);
		n = r->extractSmallest();
	}

}

AVLTree<ChildInfo> *BTreeNode::getAVLTree() {
	return children;
}

void BTreeNode::split(BTreeNode *node) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Splitting node");
	int free = node->getFreeSpace();
	int target = free/2;

	int oldMinId = minId;

	while(free > target) {
		AVLTreeNode<ChildInfo> *n = children->extractSmallest();
		freeSpace += 8;
		free -= 8;
		count--;
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moving %d", n->recordId);
		node->addChild(n->recordId, n->content->child);
	}

	AVLTreeNode<ChildInfo> *n = children->getSmallest();

	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;

	if (parent != NULL)
		parent->updateChild(oldMinId, minId);

	__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");

}

void BTreeNode::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== Node (%d), minId: %d, parent: %d, freeSpace %d",
			this, minId, parent, freeSpace);
	children->debug(true);
}
