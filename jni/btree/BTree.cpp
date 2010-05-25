#include "BTree.h"
#include "BTreeElement.h"
#include "BTreeNode.h"
#include "BTreeLeaf.h"
#include "RecordInfo.h"

#include <android/log.h>
#include <stdlib.h>

BTree::BTree(short leafCapacity, short nodeCapacity) {
	root = NULL;
	this->leafCapacity = leafCapacity;
	this->nodeCapacity = nodeCapacity;
}

void BTree::add(int id, RecordInfo *record) {
	if (root == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "new leaf as root: %d", leafCapacity);
		root = new BTreeLeaf(leafCapacity);
	}

	BTreeElement *e = root;

	while (e->type == BTreeElement::ELEMENT_NODE) {
		e = ((BTreeNode*)e)->getSubNodeFor(id);
		if (e == NULL)
			return;
	}

	BTreeLeaf *leaf = (BTreeLeaf*)e;

	if (!leaf->add(id, record)) {
		BTreeLeaf *newLeaf = new BTreeLeaf(leafCapacity);
		int oldMinId = leaf->getMinId();
		leaf->split(newLeaf);
		if (!leaf->add(id, record)) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Leaf add FAILED!");
			return;
		}

		if (leaf->getParent() == NULL) {
			leaf->setParent(new BTreeNode(nodeCapacity));
			leaf->getParent()->addChild(leaf);
		} else
			leaf->getParent()->updateChild(oldMinId,leaf->getMinId());

		leaf->getParent()->addChild(newLeaf);

	}

}

void BTree::update(int id, RecordInfo *record) {
	BTreeElement *e = root;

	if (e == NULL)
		return;

	while (e->type == BTreeElement::ELEMENT_NODE) {
		e = ((BTreeNode*)e)->getSubNodeFor(id);
		if (e == NULL)
			return;
	}

	BTreeLeaf *leaf = (BTreeLeaf*)e;

	if (!leaf->update(id, record)) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "update failed, splitting node");
		BTreeLeaf *newLeaf = new BTreeLeaf(leafCapacity);
		int oldMinId = leaf->getMinId();
		leaf->split(newLeaf);
		if (id < leaf->getMinId()) {
			if (!newLeaf->update(id, record)) {
				__android_log_print(ANDROID_LOG_INFO, "Jello",  "Leaf add FAILED!");
				return;
			}
		} else {
			if (!leaf->update(id, record)) {
				__android_log_print(ANDROID_LOG_INFO, "Jello",  "Leaf add FAILED!");
				return;
			}

		}

		if (leaf->getParent() == NULL) {
			leaf->setParent(new BTreeNode(nodeCapacity));
			leaf->getParent()->addChild(leaf);
		} else
			if (oldMinId != leaf->getMinId())
				leaf->getParent()->updateChild(oldMinId, leaf->getMinId());

		leaf->getParent()->addChild(newLeaf);
	}

	mergeLeaf(leaf);

}

RecordInfo *BTree::find(int id) {
	BTreeElement *e = root;

	if (e == NULL)
		return NULL;

	while (e->type == BTreeElement::ELEMENT_NODE) {
		e = ((BTreeNode*)e)->getSubNodeFor(id);
		if (e == NULL)
			return NULL;
	}

	return ((BTreeLeaf*)e)->get(id);

}

void BTree::remove(int id) {
	BTreeElement *e = root;

	if (e == NULL)
		return;

	while (e->type == BTreeElement::ELEMENT_NODE) {
		e = ((BTreeNode*)e)->getSubNodeFor(id);
		if (e == NULL)
			return;
	}


	BTreeLeaf *leaf = ((BTreeLeaf*)e);
	int oldMinId = leaf->getMinId();

	leaf->remove(id);

	if (leaf->getParent() != NULL && oldMinId != leaf->getMinId()) {
		leaf->getParent()->updateChild(oldMinId, leaf->getMinId());
	}

	mergeLeaf(leaf);
}

void BTree::mergeLeaf(BTreeLeaf *leaf) {
	if (leaf->getLeft() != NULL && leaf->getLeft()->getFreeSpace() + leaf->getFreeSpace() >= leafCapacity) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with left");
		leaf->getParent()->removeChild(leaf);
		int oldMinId = leaf->getLeft()->getMinId();
		leaf->getLeft()->join(leaf);
		leaf->getParent()->updateChild(oldMinId, leaf->getLeft()->getMinId());
		delete leaf;
	}

	if (leaf->getRight() != NULL && leaf->getRight()->getFreeSpace() + leaf->getFreeSpace() >= leafCapacity) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with right");
		leaf->getParent()->removeChild(leaf);
		int oldMinId = leaf->getRight()->getMinId();
		leaf->getRight()->join(leaf);
		leaf->getParent()->updateChild(oldMinId, leaf->getRight()->getMinId());
		delete leaf;
	}

}
