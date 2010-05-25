#ifndef _BTREENODE_H
#define _BTREENODE_H

#include "BTreeElement.h"

struct ChildInfo;
class BTreeElement;
template<typename T> class AVLTree;

class BTreeNode : public BTreeElement {
	private:
		int freeSpace;
		AVLTree<ChildInfo> *children;
	public:
		BTreeNode(int nodeCapacity);
		~BTreeNode();
		BTreeElement *getSubNodeFor(int id);
		void addChild(BTreeElement *child);
		void updateChild(int oldMinId, int minId);
		void removeChild(BTreeElement *child);
		
};

#endif
