#ifndef _BTREENODE_H
#define _BTREENODE_H

#include "BTreeElement.h"
#include <stdlib.h>

struct ChildInfo;
class BTreeElement;
class AVLTree;

class BTreeNode : public BTreeElement {
	private:
		AVLTree *children;
	public:
		BTreeNode(int nodeCapacity);
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

		static BTreeNode *fromBytes(uint8_t *bytes, int bTreeNodeCapacity);
		int commit();
		
};

#endif
