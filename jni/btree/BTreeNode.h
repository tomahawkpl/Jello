#ifndef _BTREENODE_H
#define _BTREENODE_H

#include "BTreeElement.h"

struct ChildInfo;
class BTreeElement;
template<typename T> class AVLTree;

class BTreeNode : public BTreeElement {
	private:
		AVLTree<ChildInfo> *children;
	public:
		BTreeNode(int nodeCapacity);
		~BTreeNode();
		BTreeElement *getSubNodeFor(int id);
		bool addChild(int id, BTreeElement *child);
		void updateChild(int oldMinId, int minId);
		void removeChild(int id);
		void split(BTreeNode *node);
		void merge(BTreeNode *node);
		void debug();
		
};

#endif
