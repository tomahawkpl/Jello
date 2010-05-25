#include "BTreeNode.h"

#include "AVLTree.h"
#include "ChildInfo.h"

BTreeNode::BTreeNode(int nodeCapacity) {
	type = BTreeElement::ELEMENT_NODE;
	freeSpace = nodeCapacity;
	children = new AVLTree<ChildInfo>();

}

BTreeNode::~BTreeNode() {
	delete children;
}

BTreeElement *BTreeNode::getSubNodeFor(int id) {

}

void BTreeNode::addChild(BTreeElement *child) {

}

void BTreeNode::removeChild(BTreeElement *child) {

}

void BTreeNode::updateChild(int oldMinId, int minId) {

}
