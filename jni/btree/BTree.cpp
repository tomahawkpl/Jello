#include "BTree.h"
#include "BTreeElement.h"
#include "BTreeNode.h"
#include "BTreeLeaf.h"
#include "RecordInfo.h"
#include "ChildInfo.h"
#include "AVLTree.h"

#include <android/log.h>
#include <stdlib.h>

BTree::BTree(short leafCapacity, short nodeCapacity) {
	root = NULL;
	this->leafCapacity = leafCapacity;
	this->nodeCapacity = nodeCapacity;
}

BTree::~BTree() {
	delete root;
}

void BTree::add(int id, RecordInfo *record) {
//	__android_log_print(ANDROID_LOG_INFO, "Jello",  "== BTree add id: %d", id);
	if (root == NULL) {
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "new leaf as root: %d", leafCapacity);
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
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Not enough space in leaf");
		BTreeLeaf *newLeaf = new BTreeLeaf(leafCapacity);
		int oldMinId = leaf->getMinId();
//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Splitting");
		leaf->split(newLeaf);
		BTreeLeaf *addTo;
		if (leaf->getMinId() == -1 || id > leaf->getMinId())
			addTo = leaf;
		else
			addTo = newLeaf;

		if (!addTo->add(id, record)) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "Leaf add FAILED!");
			return;
		}

		if (leaf->getParent() == NULL) {
//			__android_log_print(ANDROID_LOG_INFO, "Jello",  "This leaf's parent is NULL, creating new node");
			BTreeNode *node = new BTreeNode(nodeCapacity);
			node->addChild(leaf->getMinId(), leaf);
			root = node;
		}

//		__android_log_print(ANDROID_LOG_INFO, "Jello",  "Adding new node to parent");
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
		}
		addToNode(node->getParent(), newNode);
	}
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "Added new node to parent");
}

void BTree::update(int id, RecordInfo *record) {
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "Update: %d", id);
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
		}

		addToNode(leaf->getParent(), newLeaf);
	} else
		mergeNode(leaf);

}

void BTree::debug() {
	if (root != NULL)
		root->debug();
}

RecordInfo *BTree::find(int id) {
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
	__android_log_print(ANDROID_LOG_INFO, "Jello",  "Remove: %d", id);
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

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "found leaf: %d", leaf->getMinId());
	leaf->remove(id);

	__android_log_print(ANDROID_LOG_INFO, "Jello",  "pre merge");

	mergeNode(leaf);

	//	removeNode(leaf);
}

void BTree::mergeNode(BTreeElement *node) {
	if (node->getCount() == 0) {
		__android_log_print(ANDROID_LOG_INFO, "Jello",  "node %d is empty", node);
		if (node->getParent() != NULL) {
			node->getParent()->removeChild(node->getMinId());
			mergeNode(node->getParent());
		} else
			root = NULL;

		delete node;
		return;
	}

	if (node == root && node->getCount() == 1 && node->type == BTreeElement::ELEMENT_NODE) {
		root = ((BTreeNode*)node)->getSmallest();
		root->setParent(NULL);
		delete node;
		return;
	}

	/*
	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merge1 %d", node);
	   if (node == root) {
	   if (node->getCount() == 0) {
	   root = NULL;
	   delete node;
	   return;
	   }
	   if (node->getCount() == 1 && node->type == BTreeElement::ELEMENT_NODE) {
	   root = ((BTreeNode*)node)->getSmallest();
	   root->setParent(NULL);
	   delete node;
	   return;
	   }
	   return;
	   }

	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merge2 %d", node->getMinId());
	   BTreeElement *right = NULL;
	   BTreeElement *left= NULL;

	   ChildInfo *i = ((BTreeNode*)node->getParent())->getAVLTree()->findLeft(node->getMinId());
	   if (i != NULL)
	   left = i->child;

	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merge3 %d", i);

	   i = ((BTreeNode*)node->getParent())->getAVLTree()->findRight(node->getMinId());
	   if (i != NULL)
	   right = i->child;
	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merge4");


	   if (left != NULL && left->getFreeSpace() + node->getFreeSpace() >= nodeCapacity) {
	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with left");
	   node->getParent()->removeChild(node->getMinId());
	   int oldMinId = left->getMinId();
	   left->join(node);
	   node->getParent()->updateChild(oldMinId, left->getMinId());
	   if (node->getParent() != NULL)
	   mergeNode(node->getParent());
	   delete node;
	   return;
	   }

	   if (right != NULL && right->getFreeSpace() + node->getFreeSpace() >= nodeCapacity) {
	   __android_log_print(ANDROID_LOG_INFO, "Jello",  "merging with right");
	   node->getParent()->removeChild(node->getMinId());
	   int oldMinId = right->getMinId();
	   right->join(node);
	   node->getParent()->updateChild(oldMinId, right->getMinId());
	   if (node->getParent() != NULL)
	   mergeNode(node->getParent());
	   delete node;
	   return;
	   }
	   */
}

	void BTree::removeNode(BTreeElement *node) {
		if (node->getCount())
			return;

		if (node == root)
			root = NULL;

		if (node->getParent() != NULL) {
			node->getParent()->removeChild(node->getMinId());
			removeNode(node->getParent());
		}


		delete node;
	}
