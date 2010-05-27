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
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== BTree add id: %d", id);
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
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Not enough space in leaf");
		BTreeLeaf *newLeaf = new BTreeLeaf(leafCapacity);
		int oldMinId = leaf->getMinId();
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Splitting");
		leaf->split(newLeaf);
		BTreeLeaf *addTo;
		if (leaf->getMinId() == -1 || id > leaf->getMinId())
			addTo = leaf;
		else
			addTo = newLeaf;

		if (!addTo->add(id, record)) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Leaf add FAILED!");
			return;
		}

		if (leaf->getParent() == NULL) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "This leaf's parent is NULL, creating new node");
			BTreeNode *node = new BTreeNode(nodeCapacity);
			node->addChild(leaf->getMinId(), leaf);
		} else if (oldMinId != leaf->getMinId()) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Updating parent");
			leaf->getParent()->updateChild(oldMinId,leaf->getMinId());
		}

		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Adding new node to parent");
		addToNode(leaf->getParent(), newLeaf);
	}

}

void BTree::addToNode(BTreeNode *node, BTreeElement *child) {
	if (!node->addChild(child->getMinId(), child)) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Node is full");
		BTreeNode *newNode = new BTreeNode(nodeCapacity);
		int oldMinId = node->getMinId();
		node->split(newNode);

		BTreeNode *addTo;
		if (node->getMinId() == -1 || child->getMinId() > node->getMinId())
			addTo = node;
		else
			addTo = newNode;

		if (!addTo->addChild(child->getMinId(), child)) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Node add FAILED!");
			return;
		}
		
		if (node->getParent() == NULL) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "This node's parent is null");
			BTreeNode *parent = new BTreeNode(nodeCapacity);
			parent->addChild(node->getMinId(), node);
			root = parent;
		} else if (oldMinId != node->getMinId()) {
			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Updating parent");
			node->getParent()->updateChild(oldMinId,node->getMinId());
		}

		addToNode(node->getParent(), newNode);
	}
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "Added new node to parent");
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
			BTreeNode *node = new BTreeNode(nodeCapacity);
			node->addChild(leaf->getMinId(), leaf);
		} else
			if (oldMinId != leaf->getMinId())
				leaf->getParent()->updateChild(oldMinId, leaf->getMinId());

		leaf->getParent()->addChild(leaf->getMinId(), newLeaf);
	}

	mergeLeaf(leaf);

}

RecordInfo *BTree::find(int id) {
//	root->debug();
//	return NULL;
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== BTree find id: %d", id);

	BTreeElement *e = root;

	if (e == NULL)
		return NULL;

	while (e->type == BTreeElement::ELEMENT_NODE) {
		e = ((BTreeNode*)e)->getSubNodeFor(id);
		if (e == NULL)
			return NULL;
	}

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "found leaf: %d", e->getMinId());
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

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "pre merge");

	mergeLeaf(leaf);
}

void BTree::mergeLeaf(BTreeLeaf *leaf) {
	if (leaf->getLeft() != NULL && leaf->getLeft()->getFreeSpace() + leaf->getFreeSpace() >= leafCapacity) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with left");
		leaf->getParent()->removeChild(leaf->getMinId());
		int oldMinId = leaf->getLeft()->getMinId();
		leaf->getLeft()->join(leaf);
		leaf->getParent()->updateChild(oldMinId, leaf->getLeft()->getMinId());
		delete leaf;
	}

	if (leaf->getRight() != NULL && leaf->getRight()->getFreeSpace() + leaf->getFreeSpace() >= leafCapacity) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with right");
		leaf->getParent()->removeChild(leaf->getMinId());
		int oldMinId = leaf->getRight()->getMinId();
		leaf->getRight()->join(leaf);
		leaf->getParent()->updateChild(oldMinId, leaf->getRight()->getMinId());
		delete leaf;
	}

}
