#ifndef _BTREELEAF_H
#define _BTREELEAF_H

#include "BTreeNode.h"

struct RecordInfo;
template<typename T> class AVLTree;

class BTreeLeaf : public BTreeElement {
	private:
		int count;
		short freeSpace;
		BTreeLeaf *left;
		BTreeLeaf *right;
	
		AVLTree<RecordInfo> *records;

	public:
		BTreeLeaf(short freeSpace);
		~BTreeLeaf();
		short getFreeSpace();
		bool add(int id, RecordInfo *record);
		bool update(int id, RecordInfo *record);
		void remove(int id);
		void split(BTreeLeaf *leaf);
		void join(BTreeLeaf *leaf);
		RecordInfo *get(int id);
		BTreeLeaf *getRight();
		BTreeLeaf *getLeft();
		BTreeNode *getParent();
		void setRight(BTreeLeaf *leaf);
		void setLeft(BTreeLeaf *leaf);
		void setParent(BTreeNode *parent);

};

#endif
