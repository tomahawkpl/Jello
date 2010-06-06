#include "BTreeNode.h"

#include <android/log.h>

#include <stdlib.h>

#include <jni.h>
#include "BTree.h"
#include "AVLTree.h"
#include "ChildInfo.h"
#include "ChildInfoFactory.h"
#include "misc.h"
#include "PageIds.h"

BTreeNode::BTreeNode(BTree *btree, int nodeCapacity) : BTreeElement (btree) {
	type = BTreeElement::ELEMENT_NODE;
	freeSpace = nodeCapacity - 16;
	children = new AVLTree(new ChildInfoFactory(btree));
	parent = NULL;
	minId = -1;
	count = 0;
}

BTreeNode::~BTreeNode() {
	delete children;
}

BTreeElement *BTreeNode::getSmallest() {
	AVLTreeNode *node = children->getSmallest();
	if (node == NULL)
		return NULL;
	return ((ChildInfo*)node->content)->child;
}

BTreeElement *BTreeNode::getSubNodeFor(int id) {
	ChildInfo *info = (ChildInfo*)children->findHigher(id);

	if (info != NULL)
		return info->child;
	else
		return NULL;
}

bool BTreeNode::addChild(int id, BTreeElement *child) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) BTree node add %d child:%d", this, id, child);
	if (freeSpace < 24)
		return false;

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId0: %d", this,  minId);

	if (id < minId || minId == -1) {
		if (parent != NULL)
			parent->updateChild(minId, id);
		minId = id;
	}


	freeSpace -= 24;
	count++;

	if (count == 1)
		freeSpace -= 4;

	child->setParent(this);

	ChildInfo *info = new ChildInfo(btree);
	info->child = child;
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId1: %d", this,  minId);
	children->add(id, info);

	return true;
}

void BTreeNode::removeChild(int id) {
	if (children->remove(id)) {
		freeSpace += 24;
		count--;

		if (count == 0)
			freeSpace += 4;

		if (id == minId) {
			AVLTreeNode *smallest = children->getSmallest();
			if (smallest == NULL)
				minId = -1;
			else
				minId = smallest->recordId;

			//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId2: %d", this,  minId);
			if (parent != NULL)
				parent->updateChild(id, minId);
		}
	}


}

void BTreeNode::updateChild(int oldMinId, int newMinId) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) updating child: %d -> %d", this, oldMinId, newMinId);
	ChildInfo *info = (ChildInfo*)children->find(oldMinId);

	if (info == NULL)
		return;

	ChildInfo *copy = (ChildInfo*)malloc(sizeof(ChildInfo));
	memcpy(copy, info, sizeof(ChildInfo));

	if (!children->remove(oldMinId))
		return;

	children->add(newMinId, copy);

	oldMinId = minId;
	AVLTreeNode *smallest = children->getSmallest();
	if (smallest == NULL)
		minId = -1;
	else
		minId = smallest->recordId;


	if (oldMinId != minId && parent != NULL) {
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "(%d) minId3: %d", this, minId);
		parent->updateChild(oldMinId, minId);
	}

}

void BTreeNode::join(BTreeElement *node) {
	AVLTree *r = ((BTreeNode*)node)->getAVLTree();
	AVLTreeNode *n = r->extractSmallest();

	while (n != NULL) {
		this->addChild(n->recordId, ((ChildInfo*)n->content)->child);
		n = r->extractSmallest();
	}

}

AVLTree *BTreeNode::getAVLTree() {
	return children;
}

void BTreeNode::split(BTreeNode *node) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Splitting node, this freeSpace: %d", freeSpace);
	int free = node->getFreeSpace();
	int target = free/2;

	int oldMinId = minId;

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "target %d", target);
	while(node->getFreeSpace() > target) {
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "freeSpace %d", node->getFreeSpace());
		AVLTreeNode *n = children->extractSmallest();

		freeSpace += 24;
		count--;

		if (count == 0)
			freeSpace += 4;
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moving %d", n->recordId);
		node->addChild(n->recordId, ((ChildInfo*)n->content)->child);
		//__android_log_print(ANDROID_LOG_INFO, "Jello",  "Moved");
	}

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "here");
	AVLTreeNode *n = children->getSmallest();

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "here");
	if (n == NULL)
		minId = -1;
	else
		minId = n->recordId;

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "here");
	if (parent != NULL)
		parent->updateChild(oldMinId, minId);

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  " - Split done");

}

void BTreeNode::debug() {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== Node (%d), minId: %d, parent: %d, freeSpace %d",
			this, minId, parent, freeSpace);
	children->debug(true);
}

BTreeNode *BTreeNode::fromBytes(uint8_t *bytes, int nodeCapacity, BTree *btree) {
	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "node fromBytes");
	BTreeNode *node = new BTreeNode(btree, nodeCapacity);
	int  minId, freeSpace, count;
	bytesToInt(minId, bytes + 4);
	bytesToInt(freeSpace, bytes + 8);
	bytesToInt(count, bytes + 12);

	node->setMinId(minId);
	node->setFreeSpace(freeSpace);
	node->setCount(count);

	node->getAVLTree()->fromBytes(bytes + 16, node);

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "node read");

	return node;

}

int BTreeNode::commit() {
	jobject page = BTree::env->CallObjectMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyAcquire);
	PageIds *pageIds = btree->getPageIds();
	int pageId = pageIds->get();

	jboolean isCopy;
	uint8_t *bytes;
	jbyteArray buffer;

	//__android_log_print(ANDROID_LOG_INFO, "Jello",  "commiting node on page %d", pageId);
	BTree::env->SetIntField(page, BTree::fidPageId, pageId);
	buffer = (jbyteArray)BTree::env->GetObjectField(page, BTree::fidPageData);
	bytes = (uint8_t*) BTree::env->GetByteArrayElements(buffer, &isCopy);
	intToBytes(type, bytes);
	intToBytes(minId, bytes + 4);
	intToBytes(freeSpace, bytes + 8);
	intToBytes(count, bytes + 12);
	children->commit(bytes + 16);

	BTree::env->ReleaseByteArrayElements(buffer, (jbyte*)bytes, JNI_ABORT);
	BTree::env->CallVoidMethod(BTree::pagedFile, BTree::midPagedFileWritePage, page);
	BTree::env->CallVoidMethod(BTree::pagePoolProxy, BTree::midPagePoolProxyRelease, page);

	return pageId;

}
