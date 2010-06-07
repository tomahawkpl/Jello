#ifndef _BTREENODE_H
#define _BTREENODE_H

#include "BTreeElement.h"
#include <stdlib.h>

struct ChildInfo;
class BTreeElement;
class AVLTree;
class PageIds;
class BTree;

class BTreeNode : public BTreeElement {
	private:
		AVLTree *children;
	public:
		BTreeNode(BTree *btree, int nodeCapacity);
		~BTreeNode();
		BTreeElement *getSubNodeFor(int id);
		AVLTree *getAVLTree();
		bool addChild(int id, BTreeElement *child);
		void updateChild(int oldMinId, int minId);
		void removeChild(int id);
		BTreeElement *getSmallest();
		void split(BTreeNode *node);
		void join(BTreeElement *node);
		void debug();
		BTreeElement *getRight(int id);
		BTreeElement *getFirst();

		static BTreeNode *fromBytes(uint8_t *bytes, int bTreeNodeCapacity, BTree *btree);
		int commit();
		
};

#endif
