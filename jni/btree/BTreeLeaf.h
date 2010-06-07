#ifndef _BTREELEAF_H
#define _BTREELEAF_H

#include "BTreeNode.h"
#include <stdlib.h>

struct RecordInfo;
class AVLTree;
class AVLTreeNode;
class PageIds;

class BTreeLeaf : public BTreeElement {
	private:
	
		AVLTree *records;

	public:
		BTreeLeaf(BTree *btree, short freeSpace);
		~BTreeLeaf();
		AVLTree *getAVLTree();
		bool add(int id, RecordInfo *record);
		bool update(int id, RecordInfo *record);
		void remove(int id);
		RecordInfo *get(int id);
		void split(BTreeLeaf *leaf);
		void join(BTreeElement *leaf);
		void debug();
		AVLTreeNode *getNext(int id);
		AVLTreeNode *getFirst();

		static BTreeLeaf *fromBytes(uint8_t *bytes, int leafCapacity, BTree *btree);
		int commit();

};

#endif
